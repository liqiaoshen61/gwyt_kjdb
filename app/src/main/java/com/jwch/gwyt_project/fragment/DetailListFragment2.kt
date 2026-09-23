package com.jwch.gwyt_project.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.Feature
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.ReviewRecordInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.BigImageActivity
import com.jwch.gwyt_project.activity.PDFActivity
import com.jwch.gwyt_project.adapter.FileAdapter
import com.jwch.gwyt_project.adapter.ReviewAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragDetailList2Binding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.FileModel
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.util.QueryGisUtil
import com.jwch.gwyt_project.util.TPKDecryptedStream
import com.jwch.gwyt_project.util.WpsUtils2
import com.jwch.gwyt_project.view.CollectDialog
import com.jwch.gwyt_project.view.ReviewRecordOutPutDialog
import com.jwch.gwyt_project.view.SaveReviewRecordDialog
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.support.v4.startActivity
import java.io.File
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.fondesa.recyclerviewdivider.addDivider
import com.jwch.gwyt_project.adapter.HandleFlowAdapter
import com.jwch.gwyt_project.model.ProcessRecord


/**
 *  详情 处理情况
 */
class DetailListFragment2 : BaseFragment<FragDetailList2Binding>() {


    private lateinit var feature: Feature
    private var detailType = 0 //0 Poi详情，1 图斑详情

    var fileDataList = mutableListOf<FileModel>()
    private var fileAdapter: FileAdapter? = null

    var handleFlowList = mutableListOf<ProcessRecord>()
    private var handleFlowAdapter: HandleFlowAdapter? = null

    var processingStatusList = mutableListOf<ProcessRecord>()
    private var processingStatusAdapter: HandleFlowAdapter? = null

    var reviewDatList: MutableList<ReviewRecordInfo>? = null
    private var reviewAdapter: ReviewAdapter? = null

    private var selectThemes: ThemesInfo? = null
    private var linkId = "" //兴趣点id 或者 图斑id 作为关联id
    private var displayName = "" //展示 兴趣点类名称 或 图斑类型名称

    lateinit var outPutDialog: ReviewRecordOutPutDialog



    var city: String? = null //
    var county: String? = null
    var questionCode = "" //问题编号

    var map = mutableMapOf<String, Any>()

    var tempFileList = mutableListOf<File>()

    override fun initView() {
        EventBus.getDefault().register(this)
        initList()

    }

    fun initList() {
        //整改后附件 list
        vb.lvFile.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        fileAdapter = FileAdapter()
        vb.lvFile.adapter = fileAdapter
        vb.lvFile.addDivider()
        fileAdapter!!.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as FileModel
            when (getFileType(item.name)) {
                0 -> {
                    val picList = mutableListOf<ImageModel>()

                    picList.add(ImageModel(item.filePath))
                    AppContext.map[Keys.IMG_MODEL_LIST] = picList
                    AppContext.map[Keys.INDEX] = 0
                    AppContext.map[Keys.IMAGE_DES_VISIABLE] = false
                    startActivity<BigImageActivity>()
                }

                1 -> {
                    AppContext.map["pdf_info"] = item
                    startActivity<PDFActivity>()
                }

                2 -> {
                    val authority =
                        "com.jwch.gwyt_unify.util.FileProvider"  // 与 AndroidManifest 中的配置一致
                    WpsUtils2.openFileWithWps(requireContext(), item.filePath, authority)
                }

                -1 -> {
                    "该文件格式不支持浏览".tip()
                }
            }
        }

        //图斑处理流程 list
        vb.lvSpotHandleFlow.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        handleFlowAdapter = HandleFlowAdapter()
        vb.lvSpotHandleFlow.adapter = handleFlowAdapter
        vb.lvSpotHandleFlow.addDivider()

        //办理情况 list
        vb.lvProcessingStatus.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        processingStatusAdapter = HandleFlowAdapter()
        vb.lvProcessingStatus.adapter = processingStatusAdapter
        vb.lvProcessingStatus.addDivider()

        //复核记录 list
        vb.lvReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        reviewAdapter = ReviewAdapter()
        vb.lvReviewList.adapter = reviewAdapter
        vb.lvReviewList.addDivider()
        reviewAdapter!!.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as ReviewRecordInfo
            when (view.id) {
                R.id.imgDelete -> {
                    showNormalDialog(requireContext(), "是否删除复核记录？") {
                        it.yes {
                            db.deleteReviewRecord(item)
                            OperationLogger.logOperation(
                                requireContext(),
                                "删除复核记录：${displayName}"
                            )
                            loadReviwRecordData()
                        }
                    }
                }

                R.id.imgEdit -> {
                    val dialog = SaveReviewRecordDialog(
                        requireContext(),
                        feature.geometry,
                        displayName,
                        item
                    )
                    dialog.show()
                    dialog.setData(
                        linkId,
                        selectThemes!!.id.self(),
                        selectThemes?.themeName.self(),
                        questionCode
                    )
                }
            }
        }

    }

    //
    fun loadReviwRecordData() {
        reviewDatList = db.queryReviewRecordListById(linkId)
        reviewAdapter?.linkId = linkId
        reviewAdapter?.update(reviewDatList)
    }

    override fun initPageData(data: Any?) {
    }

    override fun initViewListener() {

        vb.llDetail.onClick { } //防止点击传穿透

        vb.btnColse.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_GEOMETRY_DETAIL2))
        }

        vb.tvAddReviewRecord.onClick {

            if (checkKeyword(linkId)) {
                val dialog = SaveReviewRecordDialog(requireContext(), feature.geometry, displayName)
                dialog.show()
                dialog.setData(
                    linkId,
                    selectThemes!!.id.self(),
                    selectThemes?.themeName.self(),
                    questionCode,
                    city,
                    county
                )
            } else {
                showNormalDialog(requireContext(), "新增记录前需要收藏该图斑？") {
                    it.yes {
                        vb.llCollect.performClick()
                    }
                }
            }

        }

        vb.llGoDetailFrag.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_GEOMETRY_DETAIL2))

            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_GEOMETRY_DETAIL, map))
        }
        //收藏
        vb.llCollect.onClick {

            if (checkKeyword(linkId)) {

                showNormalDialog(requireContext(), "是否取消收藏？") {
                    it.yes {
                        db.deleteCollectPatchById(linkId)
                        CommonUtil.tip(context, "操作成功")

                        EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 1))
                        OperationLogger.logOperation(
                            requireContext(),
                            "取消收藏图斑：${displayName}"
                        )
                    }
                }

            } else {
                val dialog = CollectDialog(context)
                dialog.show()

                if (getCenterPoint() == null) {
                    dialog.setPatchData(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName))
                }else{
                    val centerPoint = GeometryEngine.project(getCenterPoint(), Config.sp4490) as Point
                    dialog.setPatchData2(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName), centerPoint)
                }
            }

        }

        vb.tvOutPutReviewRecord.onClick {
            showOutPutDialog()
        }

    }

    fun getCenterPoint() : Point?{
        if(feature !=null && feature!!.geometry.extent.center != null){
            return feature!!.geometry.extent.center
        } else{
            return null
        }
    }


    private fun showOutPutDialog() {

        if (!::outPutDialog.isInitialized) {
            outPutDialog = ReviewRecordOutPutDialog(requireContext())
        }
        if (!outPutDialog.isShowing) {
            outPutDialog.show(linkId)
        }
    }

    fun clearData() {
        vb.viewAddImage.clearDataList()
        vb.lvFile
    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {

            DataEvent.SHOW_GEOMETRY_DETAIL2 -> {

                clearData()
                //图斑详情
                detailType = 1
                map = event.data as HashMap<String, Any>
                feature = castObject(map["feature"])
                linkId = castObject(map["linkId"])
                displayName = castObject(map["displayName"])
                loadReviwRecordData()

                updateCollectUI()

                OperationLogger.logOperation(requireContext(), "查看图斑处理情况：${displayName}")

                isNotNull(feature).yes {

                    selectThemes = castObject(map["theme"]) as ThemesInfo

                    val attrs = feature.attributes //图斑属性
                    QueryGisUtil.queryUtil.printAttrbuite(feature)

                    attrs?.let {

                        questionCode = attrs["问题编"].toString()
                        addPictrueToTopLeft(questionCode)

                        addFile(questionCode)

                        AppContext.app.getJsonBySpotId(questionCode)?.let {

                            val spotHandleFolw = it.optString("图斑处理流程")
                            val processingStatus = it.optString("办理情况")

                            handleFlowList = parseType1String(spotHandleFolw)
                            processingStatusList = parseType2String(processingStatus)
                            handleFlowAdapter?.update(handleFlowList)
                            processingStatusAdapter?.update(processingStatusList)
                        }

                        city = attrs["所在市"] as String?
                        county = attrs["所在县"] as String?


                    }
                }
            }

            DataEvent.UPDATE_REVIEW_RECORD_DATA -> {
                loadReviwRecordData()
            }

            DataEvent.UPDATE_COLLECTION_DATA -> {
                updateCollectUI()
            }
        }
    }


    // 第一种字符串解析方法
    fun parseType1String(type1String: String): MutableList<ProcessRecord> {
        val records = mutableListOf<ProcessRecord>()

        // 移除字符串开头和结尾的空格
        val trimmedString = type1String.trim()

        // 使用正则表达式匹配每条记录
        val pattern = Regex("处理内容:([^ ]+)\\s*处理人:([^ ]+)\\s*处理时间:([^,]+)")
        val matches = pattern.findAll(trimmedString)

        matches.forEach { matchResult ->
            val (content, processor, time) = matchResult.destructured
            records.add(
                ProcessRecord(
                    processor = processor.trim(),
                    processTime = time.trim(),
                    content = content.trim()
                )
            )
        }

        return records
    }

    // 第二种字符串解析方法
    fun parseType2String(type2String: String): MutableList<ProcessRecord> {
        val records = mutableListOf<ProcessRecord>()

        // 移除字符串开头和结尾的空格
        val trimmedString = type2String.trim()

        // 使用正则表达式匹配每条记录
        val pattern = Regex("处理人:\\s*([^\\s]+[\\s\\S]*?)\\s*处理时间:\\s*([^\\s]+\\s+[^\\s]+)\\s*处理状态:\\s*([^,]+)")
        val matches = pattern.findAll(trimmedString)

        matches.forEach { matchResult ->
            val (processor, time, status) = matchResult.destructured
            records.add(
                ProcessRecord(
                    processor = processor.trim(),
                    processTime = time.trim(),
                    status = status.trim()
                )
            )
        }

        return records
    }


    fun checkKeyword(mLink: String?): Boolean {
        //判断是否已经收藏过了
        if (!CommonUtil.isNotEmpty(mLink)) return false
        val count = db.queryPatchCollectionCountByLinkId(linkId)
        return count != null && count > 0

    }

    fun updateCollectUI() {
        if (checkKeyword(linkId)) {
            vb.tvCollect.text = "已收藏"
            vb.ivCollect.setImageResource(R.mipmap.icon_star_yellow)

        } else {
            vb.tvCollect.text = "收  藏"
            vb.ivCollect.setImageResource(R.mipmap.icon_star)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    //整改前图片
    private fun addPictrueToTopLeft(value: String) {
        vb.viewAddImage.clearDataList()

        val fileList1 = findFilesWithTimePhaseImage(Config.PHOTO_PATH, value)
        val fileList2 = findFilesWithPrefixNew(Config.PHOTO_PATH, value + "_整改前图片")

        fileList1.forEach {
            isImageFile(it).yes {
                vb.viewAddImage.addImage(ImageModel(it.absolutePath, it.name))
            }
        }

        fileList2.forEach {
            isImageFile(it).yes {
                vb.viewAddImage.addImage(ImageModel(it.absolutePath, it.name))
            }
        }

        vb.viewAddImage.setShowOnly()
        vb.viewAddImage.setImageSize(240, 170)

    }


    //整改后的附件列表
    private fun addFile(value: String) {
        fileDataList.clear()
        fileAdapter?.update(fileDataList)

        val list = value.split("_")
        CommonUtil.matchList(list).yes {

            val id = list[0]
            val fileList2 = findFilesWithPrefixNew(Config.PHOTO_PATH, id + "_整改后图片")
            val fileList =
                findFilesWithPrefixNew(Config.PHOTO_PATH, id + "_可能存在的不认领文档_图片")


            fileList?.forEach {
                fileDataList.add(FileModel(it.absolutePath, it.name))
            }
            fileList2?.forEach {
                fileDataList.add(FileModel(it.absolutePath, it.name))
            }
            fileDataList.sortBy { it.name }
            fileAdapter?.update(fileDataList)
        }
    }


    fun isImageFile(file: File): Boolean {
        // 定义支持的图片格式集合（可自行扩展）
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        // 获取文件扩展名（自动处理大小写）
        val extension = file.extension.lowercase()
        return extension in imageExtensions
    }


    fun findFilesWithTimePhaseImage(directoryPath: String, fileNamePrefix: String): List<File> {
        val result = mutableListOf<File>()
        val directory = File(directoryPath)
        val suffixes = listOf(
            "_前时相图片.png",
            "_前时相图片.jpg",
            "_前时相图片.webp",
            "_后时相图片.png",
            "_后时相图片.jpg",
            "_后时相图片.webp"
        ) // 固定后缀列表

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory) {
            println("警告：目录不存在或不是文件夹: $directoryPath")
            return result
        }

        // 遍历所有可能的文件名组合
        suffixes.forEach { suffix ->
            val fileName = fileNamePrefix + suffix
            val file = File(directory, fileName)

            if (file.exists() && file.isFile) {
                FunctionControlUtil.instances.FILE_NEED_DECRYPTED.yes {
                    val jmFile = decryptFile(file.absolutePath)
                    if (jmFile != null) {
                        result.add(jmFile)
                        tempFileList.add(jmFile)
                    } else {
                        result.add(file)
                    }
                }.no {
                    result.add(file)
                }
            }
        }

        return result
    }


    fun findFilesWithPrefixNew(
        directoryPath: String,
        fileNamePrefix: String,
        allowedExtensions: List<String> = listOf(".jpg", ".png", ".pdf", ".webp")
    ): List<File> {
        val result = mutableListOf<File>()
        val directory = File(directoryPath)

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory) {
            println("目录不存在或不是文件夹: $directoryPath")
            return result
        }

        // 遍历 1..10 和后缀组合
        for (i in 1..10) {
            for (ext in allowedExtensions) {
                val fileName = "${fileNamePrefix}_$i$ext"

                val file = File(directory, fileName)

                if (file.exists() && file.isFile) {

                    FunctionControlUtil.instances.FILE_NEED_DECRYPTED.yes {
                        val jmFile = decryptFile(file.absolutePath)
                        if (jmFile != null) {
                            result.add(jmFile)
                            tempFileList.add(jmFile)
                        } else {
                            result.add(file)
                        }
                    }.no {
                        result.add(file)
                    }

                }
            }
        }

        return result
    }

    inline fun getFileType(fileName: String?): Int {
        var fileType = -1
        fileName.isNullOrBlank().no {
            val suffix = fileName!!.substringAfterLast(".")
            suffix.toLowerCase(Locale.ROOT)
            fileType = when (suffix) {
                "jpg", "jpeg", "png", "bmp", "webp" -> 0
                "pdf" -> 1
                "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> 2
                else -> -1
            }
        }
        return fileType
    }

    fun decryptFile(filePath: String): File? {
        val stream = TPKDecryptedStream(filePath, 0x55)

        // 在Android缓存目录创建文件
        val cacheFile = stream.toCacheFile(requireContext())

        if (cacheFile != null) {
            println("缓存文件路径: ${cacheFile.absolutePath}")
            return cacheFile
        } else {
            return null
        }
    }




    override fun onDestroyView() {

        tempFileList.forEach {
            it.delete()
        }
        super.onDestroyView()
    }


}