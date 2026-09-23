package com.jwch.gwyt_project.db

import android.database.Cursor
import android.util.Log
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.*
import com.jwch.gwyt_project.config.ThemeConfigLoader
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.isNotNullObj
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import org.xutils.DbManager
import org.xutils.db.sqlite.WhereBuilder
import org.xutils.ex.DbException
import java.io.File

class DbUtil {
    companion object {
        var db = DbUtil()
    }

    var appDb: DbManager
    var ptDb: DbManager
    private val dbVersion = 7

    init {
        // 迁移数据库到应用专用目录（Android 12+ 权限限制）
        migrateDatabases()

        //专题图、poi分类、地区信息 数据库（应用专用目录）
        var appDbConfig = getDbConfig("AppDb.db", usePrivateDir = true)
        //标绘数据库、兴趣点收藏数据库（应用专用目录，读写）
        var ptDbConfig = getDbConfig("ptAppDb.db", usePrivateDir = true)

        appDb = org.xutils.x.getDb(appDbConfig)
        ptDb = org.xutils.x.getDb(ptDbConfig)

        createTableIfNotExists()
    }

    /**
     * 迁移数据库到应用专用目录
     * 解决 Android 12+ 无法在公共目录访问数据库的问题
     */
    private fun migrateDatabases() {
        try {
            // 确保目标目录存在
            File(Config.PTDB_PATH).mkdirs()

            // 迁移 AppDb.db（支持自动更新）
            migrateDatabase("AppDb.db", autoUpdate = true)

            // ptAppDb.db 的迁移已在 AppContext 中完成，这里不再重复

        } catch (e: Exception) {
            Log.e("DbUtil", "数据库迁移失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 迁移单个数据库文件
     * @param dbName 数据库文件名
     * @param autoUpdate 是否自动更新（当公共目录文件比专用目录新时覆盖）
     */
    private fun migrateDatabase(dbName: String, autoUpdate: Boolean = false) {
        val oldDbPath = Config.APPDB_PATH + dbName
        val newDbPath = Config.PTDB_PATH + dbName

        val oldDbFile = File(oldDbPath)
        val newDbFile = File(newDbPath)

        // 情况1: 新位置没有数据库，但旧位置有，则复制
        if (!newDbFile.exists() && oldDbFile.exists()) {
            try {
                oldDbFile.copyTo(newDbFile, overwrite = false)
                Log.d("DbUtil", "成功迁移 $dbName: $oldDbPath -> $newDbPath")

                // 同时复制相关的临时文件（-shm, -wal）
                copyRelatedFiles(oldDbPath, newDbPath)
            } catch (e: Exception) {
                Log.e("DbUtil", "迁移 $dbName 失败: ${e.message}")
                e.printStackTrace()
            }
        }
        // 情况2: 两个位置都有数据库，且启用了自动更新
        else if (autoUpdate && newDbFile.exists() && oldDbFile.exists()) {
            try {
                val oldLastModified = oldDbFile.lastModified()
                val newLastModified = newDbFile.lastModified()

                // 如果公共目录的文件比专用目录新，则更新
                if (oldLastModified > newLastModified) {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    Log.d("DbUtil", "检测到 $dbName 有更新:")
                    Log.d("DbUtil", "  公共目录: ${dateFormat.format(java.util.Date(oldLastModified))}")
                    Log.d("DbUtil", "  专用目录: ${dateFormat.format(java.util.Date(newLastModified))}")

                    oldDbFile.copyTo(newDbFile, overwrite = true)
                    Log.d("DbUtil", "成功更新 $dbName")

                    // 同时复制相关的临时文件（-shm, -wal）
                    copyRelatedFiles(oldDbPath, newDbPath)
                } else {
                    Log.d("DbUtil", "$dbName 无需更新（公共目录文件未更新）")
                }
            } catch (e: Exception) {
                Log.e("DbUtil", "更新 $dbName 失败: ${e.message}")
                e.printStackTrace()
            }
        }
        // 情况3: 新位置已有数据库，旧位置没有
        else if (newDbFile.exists() && !oldDbFile.exists()) {
            Log.d("DbUtil", "$dbName 已存在于应用专用目录，公共目录不存在")
        }
        // 情况4: 两个位置都没有数据库
        else if (!newDbFile.exists() && !oldDbFile.exists()) {
            Log.w("DbUtil", "$dbName 不存在于任何位置")
        }
        // 情况5: 新位置有数据库，但没有启用自动更新
        else {
            Log.d("DbUtil", "$dbName 已存在于应用专用目录，跳过迁移")
        }
    }

    /**
     * 复制数据库相关的临时文件（-shm, -wal）
     */
    private fun copyRelatedFiles(oldDbPath: String, newDbPath: String) {
        listOf("-shm", "-wal").forEach { suffix ->
            val oldFile = File(oldDbPath + suffix)
            if (oldFile.exists()) {
                try {
                    oldFile.copyTo(File(newDbPath + suffix), overwrite = true)
                    Log.d("DbUtil", "成功复制临时文件: ${oldFile.name}")
                } catch (e: Exception) {
                    Log.e("DbUtil", "复制临时文件失败: ${e.message}")
                }
            }
        }
    }

    private fun createTableIfNotExists() {
        val table = ptDb.getTable(PhotoInfo::class.java)
        table.createTableIfNotExists()

        ptDb.getTable(CollectPoiInfo::class.java).createTableIfNotExists()
        ptDb.getTable(GraphicInfo::class.java).createTableIfNotExists()
        ptDb.getTable(FolderInfo::class.java).createTableIfNotExists()

        ptDb.getTable(MarkerInfo::class.java).createTableIfNotExists()
        ptDb.getTable(CollecPatchInfo::class.java).createTableIfNotExists()
        ptDb.getTable(ImageInfo::class.java).createTableIfNotExists()
        ptDb.getTable(AccessoryInfo::class.java).createTableIfNotExists()
        ptDb.getTable(ReviewRecordInfo::class.java).createTableIfNotExists()
        ptDb.getTable(WatermarkPasswordInfo::class.java).createTableIfNotExists()
//        "标绘收藏表id:${ptDb.getTable(GraphicInfo::class.java).id}".printMsg()
    }

    private fun getDbConfig(dbName: String, usePrivateDir: Boolean = false): DbManager.DaoConfig {
        val dbDir = if (usePrivateDir) {
            File(Config.PTDB_PATH)
        } else {
            File(Config.APPDB_PATH)
        }

        return DbManager.DaoConfig()
            .setDbName(dbName)
            .setDbDir(dbDir)
            .setDbVersion(dbVersion)
            .setDbOpenListener {
//                it.database.enableWriteAheadLogging()

            }.setDbUpgradeListener { db, oldVersion, newVersion ->

                "oldVersion = $oldVersion ;newVersion=$newVersion".printMsg()

                val tb = db.getTable(MarkerInfo::class.java)
                if (!tb.columnMap.keys.contains("themeId")) {
                    db.addColumn(MarkerInfo::class.java, "themeId")
                } else if (!tb.columnMap.keys.contains("themeName")) {
                    db.addColumn(MarkerInfo::class.java, "themeName")
                }

                if(dbName.contains("ptAppDb")){
                    if (oldVersion < 7) {
                        try {
                            db.execNonQuery("ALTER TABLE CollectPatch ADD COLUMN centerPointJson TEXT")
                            db.execNonQuery("UPDATE CollectPatch SET centerPointJson = '' WHERE centerPointJson IS NULL")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
            .setTableCreateListener { _, table -> "onTableCreated: ${table.name}".printMsg() }
    }



    //======================以下是各种数据库语句============================

    /**
     *  查询App信息
     */
    fun queryAppInfo(): AppsInfo? {
        return appDb.selector(AppsInfo::class.java).findFirst()
    }

    //通过名称 查询兴趣点
    fun queryPoisByName(name: String, pageIndex: Int): Cursor {
        return appDb.execQuery("select * from Pois where PoiName like '" + name + "' order by length(PoiName) limit 10 offset " + 10 * pageIndex)
    }

    //通过名称、类型 查询兴趣点
    fun queryPoisByNameAndType(
        name: String,
        typeList: MutableList<String>,
        pageIndex: Int
    ): Cursor {
        return appDb.execQuery("select * from Pois where PoiName like '" + name + "' and TypeName in ('" + typeList[0] + "') order by length(PoiName) limit 10 offset " + 10 * pageIndex)
    }

    //通过名称 查询兴趣点总数
    fun queryPoisCountByName(name: String): Int {
        return appDb.selector(PoisInfo::class.java).where("PoiName", "like", name).findAll().size
    }

    //通过名称、类型 查询兴趣点总数
    fun queryPoisCountByNameAndType(name: String, typeList: MutableList<String>): Int {
        return appDb.selector(PoisInfo::class.java).where("PoiName", "like", name)
            .and("TypeName", "in", typeList).findAll().size
    }

    //查询 兴趣点分类
    fun queryPoiTypesInfo(): MutableList<PoiTypesInfo> {
        return appDb.selector(PoiTypesInfo::class.java).findAll()
    }

    //通过id 查询 ThemeFields
    fun queryThemeFieldsById(themeId: String): List<ThemeFieldsInfo> {
        return appDb.selector(ThemeFieldsInfo::class.java).where("ThemeId", "=", themeId).findAll()
    }

    //查询 收藏的兴趣点
    fun queryCollectPois(pageIndex: Int): MutableList<CollectPoiInfo>? {
        return ptDb.selector(CollectPoiInfo::class.java).limit(10).offset(10 * pageIndex).findAll()
    }

    //查询 收藏的兴趣点总数
    fun queryCollectPoisCount(): Int {
        return ptDb.selector(CollectPoiInfo::class.java).count().toInt()
    }

    //通过名称 查询 收藏的兴趣点
    fun queryCollectPoisByName(name: String, pageIndex: Int): MutableList<CollectPoiInfo> {
        return ptDb.selector(CollectPoiInfo::class.java).where("name", "like", "%" + name + "%")
            .limit(10).offset(10 * pageIndex).findAll() //只查询10条记录
    }

    //通过名称 查询 收藏的兴趣点总数
    fun queryCollectPoisCountByName(name: String): Int {
        return ptDb.selector(CollectPoiInfo::class.java).where("name", "like", "%" + name + "%")
            .count().toInt()
    }

    /**
     * 通过id 查询 兴趣点
     */
    fun queryPoisById(id: String): PoisInfo? {
        return appDb.selector(PoisInfo::class.java).where("Id", "=", id).findFirst();
    }

    /**
     *     删除 收藏兴趣点
     */
    fun deleteCollectPoi(obj: CollectPoiInfo?) {
        try {
            ptDb.delete(obj)
        } catch (e: DbException) {
            e.printStackTrace()
        }
    }


//    //保存图片
//    fun savePhoto(obj: PhotoInfo?) {
//        ptDb.save(obj)
//    }
//
//    //通过pid和type 查询图片
//    fun queryPhotoByPid(pid: String, type: Int): MutableList<PhotoInfo> {
//        return ptDb.selector(PhotoInfo::class.java).where("Pid", "=", pid).and("Type", "=", type).findAll()
////        return ptDb.selector(PhotoInfo::class.java).findAll()
//    }


    //通过pid 查询图片
    fun queryPhotoByPid(pid: Int?): MutableList<PhotoInfo> {
        return ptDb.selector(PhotoInfo::class.java).where("Pid", "=", pid).findAll()
    }

//    //删除 图片
//    fun deletePhoto(obj: PhotoInfo?) {
//        try {
//            ptDb.delete(obj)
//        } catch (e: DbException) {
//            e.printStackTrace()
//        }
//    }

    //获取最后一个标绘收藏项 （通过id降序排列取第一个）
    fun getLatestGraphicInfo(): GraphicInfo? {
        return ptDb.selector(GraphicInfo::class.java).orderBy("id", true).findFirst()
    }

    //通过title 查询标绘收藏总数
    fun queryGraphicCountByTitle(title: String): Long {
        return ptDb.selector(GraphicInfo::class.java).where("title", "=", title).and("pid", "=", -1)
            .count()
    }

    //保存标绘收藏
    fun saveGraphic(graphicInfo: GraphicInfo) {
        db.ptDb.saveOrUpdate(graphicInfo)
    }

    /**
     * 按名称查询标绘个数
     */
    fun queryGraphicCountByTitle2(title: String): Long {
        return db.ptDb.selector(GraphicInfo::class.java).where("title", "=", title).count()
    }

    //保存标绘
    fun saveGraphicInfo(obj: GraphicInfo?) {
        ptDb.save(obj)
    }

    //查询标绘收藏
    fun queryGraphicInfo(): MutableList<GraphicInfo>? {
        return db.ptDb.selector(GraphicInfo::class.java).orderBy("id", true).findAll()
    }

    //通过id 查询标绘收藏
    fun queryGraphicInfoById(id: Int?): GraphicInfo? {
        return db.ptDb.selector(GraphicInfo::class.java).where("pid", "!=", -1).and("id", "=", id)
            .findFirst()
    }

    //通过id 删除标绘收藏 (仅删除标绘)
    fun deleteGraphicById(id: Int?) {
        //删除数据
        val whereBuilder = WhereBuilder.b()
        whereBuilder.and("id", "=", id).and("pid", "!=", -1)
        db.ptDb.delete(GraphicInfo::class.java, whereBuilder)
    }

    //通过id 删除标绘收藏 (删除标绘文件夹)
    fun deleteGraphicFloderById(id: Int?) {
        //删除数据
        val whereBuilder = WhereBuilder.b()
        whereBuilder.and("pid", "=", id).or("id", "=", id)
        db.ptDb.delete(GraphicInfo::class.java, whereBuilder)
    }

    //通过pid 删除图片/多媒体
    fun deletePhotoInfoByPid(pid: Int?) {
        val whereBuilder = WhereBuilder.b()
        whereBuilder.and("Pid", "=", pid)
        db.ptDb.delete(PhotoInfo::class.java, whereBuilder)
    }

    //通过id 删除图片/多媒体
    fun deletePhotoInfoById(id: Int?) {
        val whereBuilder = WhereBuilder.b()
        whereBuilder.and("Pid", "=", id)
        db.ptDb.delete(PhotoInfo::class.java, whereBuilder)
    }

    //获取标绘文件夹
    fun queryGraphicFolder(): MutableList<GraphicInfo>? {
        try {
            return db.ptDb.selector(GraphicInfo::class.java).where("pid", "=", -1).findAll()
        } catch (e: DbException) {
            e.printStackTrace()
            return null
        }
    }

    //通过文件夹名称 查询标绘文件夹数量
    fun queryFolderCountByTitle(folderName: String?): Long {
        return ptDb.selector(GraphicInfo::class.java).where("title", "=", folderName)
            .and("pid", "=", -1).count()
    }

    //获取最新的一个GraphicInfo
    fun queryLastGraphic(): GraphicInfo? {

        val list = ptDb.selector(GraphicInfo::class.java).orderBy("id", true)
        if (CommonUtil.isNotNull(list)) {
            return list.findFirst()
        } else {
            return null
        }

    }

    //获取标绘文件夹下的所有子标绘
    fun queryGraphicInFolder(id: Int?): MutableList<GraphicInfo>? {
        return db.ptDb.selector(GraphicInfo::class.java).where("pid", "=", id).findAll()
    }

    //更新标绘信息
    fun saveOrUpdateGraphicIn(obj: GraphicInfo?) {
        db.ptDb.saveOrUpdate(obj)
    }

    //获取 绘文件夹和在外部的标绘结果
    fun queryGraphicAndFolderOutside(): MutableList<GraphicInfo>? {
        return db.ptDb.selector(GraphicInfo::class.java).where("pid", "=", -1)
            .or("pid", "=", -2).findAll()
    }

    //通过pid列表 查询文件夹内的标绘
    fun queryGraphicInFolderList(folderList: MutableList<Int>): MutableList<GraphicInfo>? {
        return db.ptDb.selector(GraphicInfo::class.java).where("pid", "IN", folderList).findAll()
    }

    //通过id列表 查询不在文件夹内的标绘
    fun queryGraphicInGeoList(geoList: MutableList<Int>): MutableList<GraphicInfo>? {
        return db.ptDb.selector(GraphicInfo::class.java).where("id", "IN", geoList).findAll();
    }

    //通过pid 获取多媒体
    fun queryGraphicMediaByPid(pid: String?): MutableList<PhotoInfo>? {
        return db.ptDb.selector(PhotoInfo::class.java).where("Pid", "=", pid)
            .and("Type", "=", PhotoInfo.MEDIA).findAll()
    }


    /**
     * 获取 专题数据 这个类型的 Id
     */
    fun queryThemeMapTypeId(): String {

        // JSON优先
        val jsonResult = ThemeConfigLoader.queryThemeMapTypeId()
        if (!jsonResult.isNullOrEmpty()) {
            "专题数据 id(JSON):$jsonResult".printMsg()
            return jsonResult
        }

        var themeId = ""
        val typeInfo = db.appDb.selector(CatTypeInfo::class.java)
            .where("ParentId", "=", "0")
            .and("TypeName", "like", "专题数据").findFirst()

        isNotNullObj(typeInfo) {
            themeId = typeInfo.id.self()
        }

        "专题数据 id:$themeId".printMsg()

        return themeId
    }

    /**
     * 获取类型分类列表数据
     */
    fun queryCatTypeListLevel(parentId: String = "0"): MutableList<CatTypeInfo>? {
        // JSON优先
        val jsonResult = ThemeConfigLoader.queryCatTypeListLevel(parentId)
        if (jsonResult != null) {
            return jsonResult
        }
        return db.appDb.selector(CatTypeInfo::class.java)
            .where("ParentId", "=", parentId)
            .orderBy("SortCode", false).findAll()
    }

    fun queryThemesById(id: String = ""): ThemesInfo? {
        return db.appDb.selector(ThemesInfo::class.java).where("Id", "=", id).findFirst()
    }

    fun queryCatType2ByPid(): MutableList<CatType2Info>? {
        return db.appDb.selector(CatType2Info::class.java).where("PTypeID", "=", 3)
            .orderBy("OrderNo", true).findAll()
    }

    fun queryCatType3ByPid(PTypeID: Int?): MutableList<CatType3Info>? {
        return db.appDb.selector(CatType3Info::class.java).where("PTypeID", "=", PTypeID)
            .orderBy("OrderNo", true).findAll()
    }

    fun queryThemesByPid(PTypeID: Int?): MutableList<ThemesInfo>? {
        return db.appDb.selector(ThemesInfo::class.java).where("CatType2ID", "=", PTypeID).findAll()
    }

    /**
     * 通过分类id获取专题图层信息
     */
    fun queryThemesByTypeId(typeId: String = ""): MutableList<ThemesInfo>? {
        // JSON优先
        val jsonResult = ThemeConfigLoader.queryThemesByTypeId(typeId)
        if (jsonResult != null) {
            return jsonResult
        }
        return db.appDb.selector(ThemesInfo::class.java).where("CatTypeID", "=", typeId).findAll()
    }

    /** 获取延迟注入的 region 直连 themes（排在子分类之后） */
    fun queryInjectedDirectThemes(parentId: String): MutableList<ThemesInfo>? {
        return ThemeConfigLoader.queryInjectedDirectThemes(parentId)
    }

    //通过ThemeId和FieldName 查询专题图字段
    fun queryThemeFieldsByThemeIdAndFieldName(themeId: String?, fieldName: String): String {
        return db.appDb.selector(ThemeFieldsInfo::class.java).where("ThemeId", "=", themeId)
            .and("FieldName", "=", fieldName).findAll()[0].dispName
    }

    //
    fun queryThemeFields(ti: ThemesInfo?): String {
        return db.appDb.selector(ThemeFieldsInfo::class.java).where("ThemeId", "=", ti?.id)
            .and("FieldName", "=", ti?.getListInfoField(0)).findFirst().dispName
    }

    fun queryThemeFields2(ti: ThemesInfo?, filedName: String = "SM"): String {
        return db.appDb.selector(ThemeFieldsInfo::class.java).where("ThemeId", "=", ti?.id)
            .and("FieldName", "=", filedName).findFirst().dispName
    }

    fun queryAllThemeFieldsByThemeIdAndFieldName(
        themeId: String?,
        FieldName: String?
    ): MutableList<ThemeFieldsInfo> {
        return db.appDb.selector(ThemeFieldsInfo::class.java).where("ThemeId", "=", themeId)
            .and("FieldName", "=", FieldName).findAll()
    }

    /**
     * 查询密码
     */
    fun queryPassword(): PasswordInfo? {
        return db.ptDb.selector(PasswordInfo::class.java).findFirst()
    }

    /**
     * 更新密码
     */
    fun updatePassword(password: PasswordInfo) {
        db.ptDb.saveOrUpdate(password)
    }

    /**
     * 清空所有密码
     */
    fun deleteAllPassword() {
        db.ptDb.delete(PasswordInfo::class.java)
    }

    /**
     * 查询水印密码
     */
    fun queryWatermarkPassword(): WatermarkPasswordInfo? {
        return db.ptDb.selector(WatermarkPasswordInfo::class.java).findFirst()
    }

    /**
     * 更新水印密码
     */
    fun updateWatermarkPassword(password: WatermarkPasswordInfo) {
        db.ptDb.saveOrUpdate(password)
    }

    /**
     * 清空水印密码
     */
    fun deleteWatermarkPassword() {
        db.ptDb.delete(WatermarkPasswordInfo::class.java)
    }

    /**
     * 通过id 查询Themes
     */
    fun queryThemesById(id: Int?): MutableList<ThemesInfo>? {
        return db.appDb.selector(ThemesInfo::class.java).where("Id", "=", id).findAll()
    }


    /**
     * 创建文件夹
     */
    fun craeteFolder(name: String): Int {

        if (CommonUtil.isNotEmpty(name)) {

            val list = db.ptDb.selector(FolderInfo::class.java).where("name", "=", name)

            val count = if (CommonUtil.isNotNull(list)) list.count() else 0

            if (count > 0) {
                //已经存在相同名字的文件夹
                return -2
            }

            db.ptDb.save(FolderInfo(name))
            db.ptDb.selector(FolderInfo::class.java).orderBy("id", true).findAll()
            return 0
        }
        //文件名不能为空
        return -1

    }

    /**
     * 查询所有的文件夹
     */
    fun queryAllFolderList(): MutableList<FolderInfo>? =
        db.ptDb.selector(FolderInfo::class.java).findAll()

    /**
     * 获取到最新添加的文件夹信息
     */
    fun queryLastFolder(): FolderInfo {
        return db.ptDb.selector(FolderInfo::class.java).orderBy("id", true).findFirst()
    }

    /**
     * 删除文件夹
     */
    fun deleteFolder(folderInfo: FolderInfo) = db.ptDb.delete(folderInfo)


    /**
     * 保存标绘
     */
    fun saveMarker(marker: MarkerInfo) = db.ptDb.save(marker)

    /**
     * 删除标绘
     */
    fun deleteMarker(marker: MarkerInfo) = db.ptDb.delete(marker)

    fun deletReviewRecord(info: ReviewRecordInfo) = db.ptDb.delete(info)

    /**
     * 通过文件夹id删除该文件夹下所有的标绘
     */
    fun deleteMarkerByFolderId(folderId: Int) {
        db.ptDb.delete(MarkerInfo::class.java, WhereBuilder.b("folderId", "=", folderId))
    }

    /**
     * 获取到最新添加的标绘信息
     */
    fun queryLastMarkerInfo(): MarkerInfo {
        return db.ptDb.selector(MarkerInfo::class.java).orderBy("id", true).findFirst()
    }

    /**
     * 查询所有的标绘信息
     */
    fun queryAllMarkerList(): MutableList<MarkerInfo>? =
        db.ptDb.selector(MarkerInfo::class.java).findAll()

    /**
     * 通过文件夹id查询所有的标绘信息
     */
    fun queryAllMarkerListByFolderId(folderId: Int): MutableList<MarkerInfo>? =
        db.ptDb.selector(MarkerInfo::class.java).where("folderId", "=", folderId).findAll()

    /**
     * 更新标绘信息
     */
    fun updateMarkerInfo(marker: MarkerInfo) = db.ptDb.saveOrUpdate(marker)


    /**
     * 判断是否有相同或重复名字的标绘
     */
    fun checkMarkerNameDuplicate(name: String = ""): Boolean {


        val list = db.ptDb.selector(MarkerInfo::class.java).where("name", "=", name)

        val count = if (CommonUtil.isNotNull(list)) list.count() else 0

        return count > 0


    }

    /**
     * 通过关联id和图片的数据类型查询图片列表数据
     */
    fun queryImageInfoByLinkIdAndDataType(
        linkId: String = "",
        dataType: Int
    ): MutableList<ImageInfo>? {


        return db.ptDb.selector(ImageInfo::class.java).where("linkId", "=", linkId)
            .and("dataType", "=", dataType)?.findAll()
    }

    /**
     * 删除关联id和图片类型的图片数据
     */
    fun deleteImageInfoByLinkIdAndDataType(linkId: String = "", dataType: Int) {
        db.ptDb.delete(
            ImageInfo::class.java,
            WhereBuilder.b("linkId", "=", linkId).and("dataType", "=", dataType)
        )
    }


    /**
     * 保存图片
     */
    fun saveImageInfo(imageInfo: ImageInfo) = db.ptDb.save(imageInfo)

    /**
     * 删除图片
     */
    fun deleteImageInfo(imageInfo: ImageInfo) = db.ptDb.delete(imageInfo)

    /**
     * 更新图片信息
     */
    fun updateImageInfo(imageInfo: ImageInfo) = db.ptDb.saveOrUpdate(imageInfo)

    /**
     * 查询照片数据库是否已同名数据
     */
    fun isExistImageInfo(linkId: String, name: String, suffx: String): Boolean {
        return try {
            return ptDb.selector(ImageInfo::class.java).where("linkId", "=", linkId)
                .where("name", "=", "$name$suffx").count() > 0
        } catch (e: Exception) {
            false
        }
    }


    /**
     * 通过关联id和附件的数据类型查询附件列表数据
     */
    fun queryAccessoryInfoByLinkIdAndDataType(
        linkId: String = "",
        dataType: Int
    ): MutableList<AccessoryInfo>? {
        return db.ptDb.selector(AccessoryInfo::class.java).where("linkId", "=", linkId)
            .and("dataType", "=", dataType)?.findAll()
    }

    /**
     * 删除关联id的附件数据
     */
    fun deleteAccessoryInfoByLinkIdAndDataType(linkId: String = "", dataType: Int) {
        db.ptDb.delete(
            AccessoryInfo::class.java,
            WhereBuilder.b("linkId", "=", linkId).and("dataType", "=", dataType)
        )
    }


    /**
     * 保存附件
     */
    fun saveAccessoryInfo(accessoryInfo: AccessoryInfo) = db.ptDb.save(accessoryInfo)

    /**
     * 删除附件
     */
    fun deleteAccessoryInfo(accessoryInfo: AccessoryInfo) = db.ptDb.delete(accessoryInfo)

    /**
     * 更新附件信息
     */
    fun updateAccessoryInfo(accessoryInfo: AccessoryInfo) = db.ptDb.saveOrUpdate(accessoryInfo)

    /**
     * 通过关联id 查询收藏列表
     */
    fun queryCollectionListByLinkId(linkId: String = ""): MutableList<CollectPoiInfo>? {
        return db.ptDb.selector(CollectPoiInfo::class.java).where("linkId", "=", linkId)?.findAll()
    }

    /**
     * 通过关联id 查询兴趣点收藏数量
     */
    fun queryPoiCollectionCountByLinkId(linkId: String = ""): Long? {
        return db.ptDb.selector(CollectPoiInfo::class.java).where("linkId", "=", linkId)?.count()
    }


    /**
     * 保存兴趣点收藏
     */
    fun saveCollectPoiInfo(info: CollectPoiInfo) = db.ptDb.save(info)

    /**
     *  查询 所有收藏的兴趣点
     */
    fun queryAllCollectPoisList(): MutableList<CollectPoiInfo>? {
        return ptDb.selector(CollectPoiInfo::class.java).findAll()
    }


    /**
     * 通过关联id和收藏的数据类型 查询图斑收藏数量
     */
    fun queryPatchCollectionCountByLinkId(linkId: String = ""): Long? {
        return db.ptDb.selector(CollecPatchInfo::class.java).where("linkId", "=", linkId)?.count()
    }

    /**
     *  查询 所有收藏的图斑
     */
    fun queryAllCollectPatchList(): MutableList<CollecPatchInfo>? {
        return ptDb.selector(CollecPatchInfo::class.java).orderBy("createTimeStamp", true).findAll()
    }

    /**
     * 保存图斑收藏
     */
    fun saveCollectPatchInfo(info: CollecPatchInfo) = db.ptDb.save(info)

    /**
     * 删除 收藏兴趣点
     */
    fun deleteCollectPatch(obj: CollecPatchInfo?) {
        try {
            ptDb.delete(obj)
        } catch (e: DbException) {
            e.printStackTrace()
        }
    }

    /**
     * 删除 收藏兴趣点 通过id
     */
    fun deleteCollectPatchById(linkId: String?) {
        try {
            ptDb.delete(CollecPatchInfo::class.java, WhereBuilder.b("linkId", "=", linkId))
        } catch (e: DbException) {
            e.printStackTrace()
        }
    }


    /**
     * 通过行政区划登记查询
     * level  3 区县级  4 乡镇  5村庄
     */
    fun queryDistrictListByLevel(level: Int): MutableList<DistrictsInfo>? {
        var list =
            appDb.selector(DistrictsInfo::class.java).where("DistLevel", "=", level).findAll()

        return list


    }

    /**
     * 通过行政区划登记查询
     * level  3 区县级  4 乡镇  5村庄
     */
    fun queryDistrictByCode(code: String): DistrictsInfo? {
        var item =
            appDb.selector(DistrictsInfo::class.java).where("DistCode", "=", code).findFirst()
        return item
    }

    /**
     * 通过行政区划登记查询
     * level  3 区县级  4 乡镇  5村庄
     */
    fun queryDistrictByName(name: String): DistrictsInfo? {
        var item =
            appDb.selector(DistrictsInfo::class.java).where("distName", "=", name).findFirst()
        return item


    }

    /**
     * 查询 图层类型 TypeType 表示类型级别  0 1 2
     */
    fun queryLayerTypeByTypeType(TypeType: String = ""): MutableList<LayerType>? {
        return db.appDb.selector(LayerType::class.java).where("TypeType", "=", TypeType).findAll()
    }

    /**
     * 获取到最新添加的复核记录信息
     */
    fun queryLastReviewRecordInfo(): ReviewRecordInfo {
        return db.ptDb.selector(ReviewRecordInfo::class.java).orderBy("id", true).findFirst()
    }


    /**
     * 保存复核记录
     */
    fun saveReviewRecord(info: ReviewRecordInfo) = db.ptDb.save(info)

    /**
     * 删除复核记录
     */
    fun deleteReviewRecord(info: ReviewRecordInfo) = db.ptDb.delete(info)

    /**
     * 更新复核记录
     */
    fun updateReviewRecordInfo(info: ReviewRecordInfo) = db.ptDb.saveOrUpdate(info)


    /**
     * 通过文件夹id查询所有的标绘信息
     */
    fun queryReviewRecordListById(linkId: String): MutableList<ReviewRecordInfo>? =
        db.ptDb.selector(ReviewRecordInfo::class.java).where("linkId", "=", linkId).findAll()

    //通过名称 查询 收藏的图斑
    fun queryCollectPatchByName(name: String): MutableList<CollecPatchInfo> {
        return ptDb.selector(CollecPatchInfo::class.java).where("name", "like", "%$name%").findAll()
    }

    //通过名称 查询 收藏的标绘 （工作记录）
    fun queryCollectGeoByName(name: String): MutableList<MarkerInfo> {
        return ptDb.selector(MarkerInfo::class.java).where("riverName", "like", "%$name%").findAll()
    }

    fun queryDistrictByDistName(name: String): DistrictsInfo? {
        var item =
            appDb.selector(DistrictsInfo::class.java).where("DistName", "=", name).findFirst()
        return item
    }


}