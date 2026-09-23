package com.jwch.gwyt_project.fragment

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.PoisInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.DetailParamsAdapter
import com.jwch.gwyt_project.adapter.GridVerticalDividerItemDecoration
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragDetailListBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.selfTempNo
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.model.KVData
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.MapConverter
import com.jwch.gwyt_project.util.OperationLogger
import com.jwch.gwyt_project.util.QueryGisUtil
import com.jwch.gwyt_project.util.TPKDecryptedStream
import com.jwch.gwyt_project.view.CollectDialog
import com.jwch.gwyt_project.view.GridAddImage
import com.jwch.gwyt_project.view.TextLine
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale


/**
 * 兴趣点详情、图斑详情
 */
class DetailListFragment : BaseFragment<FragDetailListBinding>() {

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")

        /** 行政区+经纬度拼装相关的字段 key，在遍历时跳过，单独处理 */
        private val ADMIN_AND_COORD_KEYS = setOf("所在市", "所在县", "所在镇", "所在村", "经度", "纬度")

        /** 在详情列表中不展示的字段 key */
        private val SKIP_KEYS = setOf("图斑处理流程")
    }

    var infoId = "" //兴趣点id 或 图斑id

    private var datalist: List<Map<String, String>>? = null
    private lateinit var feature: Feature
    private var detailType = 0 //0 Poi详情，1 图斑详情

    var map = mutableMapOf<String, Any>()

    private var linkId = "" //兴趣点id 或者 图斑id 作为关联id
    private var displayName = "" //展示 兴趣点类名称 或 图斑类型名称
    var selectThemes: ThemesInfo? = null//专题图对象（数据库）

    private var detailParamsAdapter: DetailParamsAdapter? = null
    var kvDataList = mutableListOf<KVData>()

    var tempFileList = mutableListOf<File>() //临时解密文件列表

    override fun initView() {
        EventBus.getDefault().register(this)
        initList()
    }

    override fun initPageData(data: Any?) {
    }

    override fun initViewListener() {

        vb.llDetail.onClick { } //防止点击穿透

        vb.btnColse.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_POI_DETAIL))
            vb.llDetailContainer.removeAllViews()
        }

        vb.llGoHanldeStatusFrag.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.HIDE_POI_DETAIL))
            vb.llDetailContainer.removeAllViews()

            EventBus.getDefault().post(DataEvent(DataEvent.SHOW_GEOMETRY_DETAIL2, map))
        }

        //收藏
        vb.llCollect.onClick {

            if (checkKeyword()) {
                showNormalDialog(requireContext(), "是否取消收藏？") {
                    it.yes {
                        db.deleteCollectPatchById(linkId)
                        CommonUtil.tip(context, "操作成功")
                        EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_COLLECTION_DATA, 1))
                        OperationLogger.logOperation(requireContext(), "取消图斑收藏：${displayName}")
                    }
                }
            } else {
                val dialog = CollectDialog(context)
                dialog.show()

                if (getCenterPoint() == null) {
                    dialog.setPatchData(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName))
                } else {
                    val centerPoint = GeometryEngine.project(getCenterPoint(), Config.sp4490) as Point
                    dialog.setPatchData2(displayName, linkId, CommonUtil.getSelfValue(selectThemes!!.id), CommonUtil.getSelfValue(selectThemes!!.themeName), centerPoint)
                }
            }
        }
    }

    fun getCenterPoint(): Point? {
        if (feature != null && feature!!.geometry.extent.center != null) {
            return feature!!.geometry.extent.center
        } else {
            return null
        }
    }

    val spanCount = 2 //列数
    fun initList() {
        vb.lvMain.layoutManager = GridLayoutManager(context, spanCount)
        detailParamsAdapter = DetailParamsAdapter()
        vb.lvMain.adapter = detailParamsAdapter

        val divider = GridVerticalDividerItemDecoration(
            color = ContextCompat.getColor(requireContext(), R.color.statis_year_unselect),
            heightPx = 1,
            columnCount = spanCount
        )

        vb.lvMain.addItemDecoration(divider)
    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {
            DataEvent.SHOW_POI_DETAIL -> {
                //兴趣点详情
                detailType = 0
                map = event.data as HashMap<String, Any>
                val info = map["info"] as PoisInfo
                infoId = info.id

                datalist = info.changeToDetail()
                createForm(datalist)
            }

            DataEvent.SHOW_GEOMETRY_DETAIL -> {
                kvDataList.clear()
                vb.llDetailContainer.removeAllViews() //清理之前的视图
                tempFileList.clear() //清理临时文件列表
                //图斑详情
                detailType = 1
                map = event.data as HashMap<String, Any>
                feature = castObject(map["feature"])
                linkId = castObject(map["linkId"])
                displayName = castObject(map["displayName"])

                updateCollectUI()

                OperationLogger.logOperation(requireContext(), "查看图斑详情：${displayName}")

                isNotNull(feature).yes {
                    selectThemes = castObject(map["theme"]) as ThemesInfo
                    val themeName = selectThemes!!.themeName

                    val attrs = feature.attributes //图斑属性
                    QueryGisUtil.queryUtil.printAttrbuite(feature)
                    attrs?.let {

                        vb.llGoHanldeStatusFrag.visiable(attrs["问题编"] != null)

                        if (themeName.contains("水利部") || themeName.contains("自查自纠")) {
                            vb.llGoHanldeStatusFrag.gone()
                        }
                        if (themeName.contains("乱占")
                            || themeName.contains("乱堆")
                            || themeName.contains("乱建")
                            || themeName.contains("乱采")
                            || themeName.contains("其他")
                            || themeName.contains("水利部")
                            || themeName.contains("自查自纠")
                        ) {
                            vb.llCollect.show()
                        } else {
                            vb.llCollect.gone()
                        }

                        // 直接从 attrs 取值，避免遍历全部字段后逐个 when 比较
                        val city = attrs["所在市"]?.toString().orEmpty()
                        val county = attrs["所在县"]?.toString().orEmpty()
                        val town = attrs["所在镇"]?.toString().orEmpty()
                        val village = attrs["所在村"]?.toString().orEmpty()
                        val lng = attrs["经度"]?.toString().orEmpty()
                        val lat = attrs["纬度"]?.toString().orEmpty()

                        val skipKeys = ADMIN_AND_COORD_KEYS + SKIP_KEYS
                        MapConverter.convert(attrs, includeUnmapped = false).forEach { item ->
                            when {
                                item.key.startsWith("OBJECTID") -> { /* 跳过系统字段 */ }
                                item.value is GregorianCalendar -> {
                                    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.US)
                                    val v = sdf.format((item.value as GregorianCalendar).time)
                                    createForm("${item.key}", v)
                                }
                                item.key in skipKeys -> { /* 跳过，已单独处理或不展示 */ }
                                else -> {
                                    createForm("${item.key}", "${item.value}".selfTempNo())
                                }
                            }
                        }

                        val xingzhengqu = "${city}${county}${town}${village}"
                        if (xingzhengqu.isNotBlank()) {
                            createForm("所在行政区", xingzhengqu)
                        }

                        val jingweidu = "${lng}  ${lat} "
                        if (jingweidu.isNotBlank()) {
                            createForm("经纬度", jingweidu)
                        }
                    }

                    // 业务图片展示
                    addBusinessImages(selectThemes?.themeName, attrs)
                }

                detailParamsAdapter?.update(kvDataList)
            }
            DataEvent.UPDATE_COLLECTION_DATA -> {
                updateCollectUI()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        // 清理临时解密文件
        tempFileList.forEach {
            it.delete()
        }
        tempFileList.clear()
        super.onDestroyView()
    }

    //将Map中的数据 添加到TextLine
    private fun createForm(datalist: List<Map<String, String>>?) {
        datalist?.forEach {
            val text = LayoutInflater.from(context).inflate(R.layout.view_form_text, null) as TextLine
            vb.llDetailContainer.addView(text)
            text.setTitle(it["key"].self())
            text.setValueText(it["values"].self())
            text.show()
        }
    }

    private fun createForm(key: String, value: String) {
        kvDataList.add(KVData(key, value))
        // adapter.update 在 handlePageView 末尾统一调用，避免每加一条就刷新一次
    }

    fun checkKeyword(): Boolean {
        //判断是否已经收藏过了
        if (linkId.isBlank()) return false
        val count = db.queryPatchCollectionCountByLinkId(linkId)
        return count != null && count > 0
    }

    fun updateCollectUI() {
        if (checkKeyword()) {
            vb.tvCollect.text = "已收藏"
            vb.ivCollect.setImageResource(R.mipmap.icon_star_yellow)
        } else {
            vb.tvCollect.text = "收  藏"
            vb.ivCollect.setImageResource(R.mipmap.icon_star)
        }
    }

    /**
     * 添加业务图片
     * 根据 themeName 匹配业务配置，查找对应文件夹内的图片
     */
    private fun addBusinessImages(themeName: String?, attrs: Map<String, Any>?) {
        if (themeName.isNullOrBlank() || attrs == null) return

        // 1. 根据 themeName 匹配业务配置
        val config = Config.BUSINESS_IMAGE_CONFIGS.find { themeName.contains(it.themeKey) }
            ?: return

        // 2. 获取配置的字段值（对应文件夹名称）
        val folderName = attrs[config.attrField]?.toString()?.trim()
        if (folderName.isNullOrBlank()) {
            println("业务图片：attrs[\"${config.attrField}\"] 为空，不展示图片")
            return
        }

        // 3. 构建图片文件夹路径
        val dirPath = Config.HEAD_FILE_PATH + config.folderName + "/$folderName/"
        val dir = File(dirPath)

        println("业务图片：themeName=$themeName, ${config.attrField}=$folderName，查找路径=$dirPath")

        // 4. 检查文件夹是否存在
        if (!dir.exists() || !dir.isDirectory) {
            println("业务图片：文件夹不存在或不是目录 -> $dirPath")
            return
        }

        // 5. 过滤图片文件
        val imageFiles = dir.listFiles()?.filter { isImageFile(it) }
        if (imageFiles.isNullOrEmpty()) {
            println("业务图片：文件夹内无图片文件 -> $dirPath")
            return
        }

        println("业务图片：找到 ${imageFiles.size} 张图片")

        // 6. 使用 GridAddImage 控件展示
        val gridImage = LayoutInflater.from(context)
            .inflate(R.layout.view_grid_image, null) as android.widget.LinearLayout
        val viewAddImage = gridImage.findViewById<GridAddImage>(R.id.viewAddImage)

        imageFiles.forEach { file ->
            FunctionControlUtil.instances.FILE_NEED_DECRYPTED.yes {
                val decryptedFile = decryptFile(file.absolutePath)
                if (decryptedFile != null) {
                    viewAddImage.addImage(ImageModel(decryptedFile.absolutePath, file.name))
                } else {
                    viewAddImage.addImage(ImageModel(file.absolutePath, file.name))
                }
            }.no {
                viewAddImage.addImage(ImageModel(file.absolutePath, file.name))
            }
        }

        viewAddImage.setShowOnly()
        viewAddImage.setImageSize(240, 170)

        // 7. 添加到详情容器顶部
        vb.llDetailContainer.addView(gridImage, 0)
    }

    /**
     * 判断是否是图片文件
     */
    private fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in IMAGE_EXTENSIONS
    }

    /**
     * 解密文件
     */
    private fun decryptFile(filePath: String): File? {
        return try {
            val stream = TPKDecryptedStream(filePath, 0x55)
            val cacheFile = stream.toCacheFile(requireContext())
            if (cacheFile != null) {
                tempFileList.add(cacheFile)
                println("解密文件成功: ${cacheFile.absolutePath}")
            }
            cacheFile
        } catch (e: Exception) {
            println("解密文件失败: ${e.message}")
            null
        }
    }
}