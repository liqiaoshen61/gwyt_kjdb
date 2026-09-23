package com.jwch.gwyt_project.activity


import android.widget.TextView
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.GetWindowSize
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewPhotoBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.UnifiedLocationManager
import com.jwch.gwyt_project.util.MapUtil
import com.jwch.gwyt_project.util.watermark.WatermarkUtils
import com.jwch.gwyt_project.view.SavePhotoDialog
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xutils.ex.DbException
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 照片的Activity (Dialog样式)
 */
class PhotoDialogActivity : FullScreenActivity<ViewPhotoBinding>() {

    var mLinkId: String = ""//数据关联id
    var mDataType = 0//图片类型

    var savePhotoData : ImageInfo? = null

    companion object {
        const val ACTION_SAVE = 0 //保存
        const val ACTION_DELETE = 1 //删除
        const val ACTION_SAVE_WITH_SCREENSHOT = 2 //保存 并截图
    }

    var photoList: MutableList<ImageInfo>? = null

    private lateinit var locationManager: UnifiedLocationManager

    var latitude = 0.0
    var longitude = 0.0

    var enableSelection =  false //选择照片

    override fun initView() {
        dialogFitScreen()
        locationManager = UnifiedLocationManager(context)

        mLinkId = getKV("linkId", "")
        mDataType = getKV("dataType", 0)
        enableSelection = getKV("enableSelection", false)

        vb.viewAddImage.activity = this
        vb.viewAddImage.setEnableSelectionModel(enableSelection)

        vb.llBottom.visiable(enableSelection)

        photoList = mutableListOf()
        EventBus.getDefault().register(this)

        updatePhotoList()

        val actionListener = object : ActionListener {
            override fun onAction(obj: Any?, flag: Int) {
                when (flag) {
                    //弹窗保存后 更新数据
                    ACTION_SAVE -> {
                        savePhotoData = obj as ImageInfo
                        updatePhotoList()
                    }
                    //弹窗保存后 更新数据 并通知地图去截图
                    ACTION_SAVE_WITH_SCREENSHOT -> {
                        savePhotoData = obj as ImageInfo
                        //拍照图片保存后 去执行 定位截图
//                        EventBus.getDefault().post(MapEvent(MapEvent.SCREEN_SHOT_WHTI_LOACTION, savePhotoData))


                        EventBus.getDefault().post(MapEvent(MapEvent.QUERY_LOCATION_AREAINFO, savePhotoData))
                        updatePhotoList()
                    }
                    //点击图片右上角x后 下面的代码进行删除操作
                    ACTION_DELETE -> {
                        val photoIndex = obj as Int
                        val item = photoList?.get(photoIndex)

                        photoList?.removeAt(photoIndex)
                        //删除数据库图片
                        db.deleteImageInfo(item!!)
                        //删除本地图片
                        val imageFile = File(item?.filePath)
                        if (imageFile.exists()) {
                            imageFile.delete()
                        }
                        ToastUtils.show("删除成功！")
                    }
                }
            }
        }

        vb.viewAddImage.resultPicBlockWithSource = { url, source ->
            //保存操作 弹窗输入文件名和备注
            val dialog = SavePhotoDialog(context, actionListener)
            dialog.show()
            //相册来源不展示"同时保存截屏地图"，由 dialog 内部按 source 处理
            dialog.setData(mLinkId, mDataType, url, source)

            getLatLng()
        }
        vb.viewAddImage.listener = actionListener
    }

    override fun initViewListener() {
        vb.tvCloseDialogAcitvity.onClick {
            finish()
        }

        vb.tvCancle.onClick {
            finish()
        }

        vb.tvOk.onClick {
            val selectList =  vb.viewAddImage.getSelectedData()
            EventBus.getDefault().post(DataEvent(DataEvent.GO_BACK_REVIEW_RECORD_DIALOG, selectList))//打开相册页面对话框
            finish()
        }
    }


    private fun dialogFitScreen() {
        val util = GetWindowSize(this)
        val width = util.windowWidth / 2 * 1
        val height = util.windowHeight / 2 * 1
        vb.llDialog.layoutParams.width = width
        vb.llDialog.layoutParams.height = height
    }



    /**
     * 获取图片数据
     */
    private fun updatePhotoList() {
        //移除之前展示的图片
        vb.viewAddImage.datalist.removeIf {
            !it.imgUrl.equals("addPic")
        }
        photoList?.clear()

        try {
            //根据pid和type 查询数据库
            photoList = db.queryImageInfoByLinkIdAndDataType(mLinkId, mDataType)
        } catch (e: DbException) {
            e.printStackTrace()
        }
        //列表展示
        matchList(photoList).yes {
            photoList?.forEach {
                vb.viewAddImage.addImage(ImageModel(it.filePath, it.remark.self(), it.name))
            }
        }

    }


    fun addSuffixToFileName(filePath: String, addStr: String): String {
        val file = File(filePath)
        val parent = file.parent
        val nameWithoutExtension = file.nameWithoutExtension
        val extension = file.extension

        val newFileName = buildString {
            append(nameWithoutExtension)
            append(addStr)
            if (extension.isNotEmpty()) append(".$extension")
        }

        return File(parent, newFileName).path
    }


    //操作地图
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: DataEvent) {
        when (event.actionType) {
            //同步绘制
            DataEvent.UPDATE_PHOTO_LIST -> {
                val imageInfo = event.data as ImageInfo
                //给截图增加水印
                getWaterPhotoPath(imageInfo)

                updatePhotoList()
            }
        }
    }


    fun getWaterPhotoPath(imageInfo :ImageInfo) :String?{
        return  WatermarkUtils.addWatermark(
            context = context,
            originalPath = imageInfo.filePath,
            watermarkLayoutId = R.layout.view_watermark
        ) { watermarkView ->
            // 配置水印内容
            val tvTime = watermarkView.findViewById<TextView>(R.id.tvTime)
            val tvLocation = watermarkView.findViewById<TextView>(R.id.tvLocation)
            val tvLatLng = watermarkView.findViewById<TextView>(R.id.tvLatLng)

            // 动态设置内容
            tvTime.text = "拍摄时间：${SimpleDateFormat(Config.timeFormat1).format(Date())}"
            val df = DecimalFormat("#.####")
            val lat = df.format(latitude)
            val lng = df.format(longitude)

            tvLocation.text = "行政区划：${imageInfo.city}${imageInfo.county}${imageInfo.town}"

            tvLatLng.text = "坐标信息：${lat}  ${lng}"
        }
    }

    fun getLatLng(){
        locationManager.requestSingleLocation(false) { province, city, district, address, lat, lng ->
            // 处理定位结果
            val point = MapUtil.mapUtil.get_change_geometry_point(lat, lng, Config.sp4490Int)

            longitude = point.x
            latitude = point.y
        }
    }



    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}
