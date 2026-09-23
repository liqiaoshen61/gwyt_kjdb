package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.AccessoryInfo
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.PoltSpinnerAdapter
import com.jwch.gwyt_project.databinding.ViewSaveGeoDialogBinding
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.util.MarkerUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import java.io.File
import com.jwch.gwyt_project.util.OperationLogger

/**
 * 保存标绘
 */

class SaveGeometyDialog(
    context: Context,
    listener: ActionListener,
    markerUtil: MarkerUtil?,
    drawType: Int,
    _markerInfo: MarkerInfo? = null
) : JameniBaseDialog(context) {

    var vb: ViewSaveGeoDialogBinding? = null

    private var listener1: ActionListener? = null
    var isEdit: Boolean = false //编辑模式
    var drawType = 0//标绘的图形类型

    private var markerUtil: MarkerUtil? = null//标绘工具对象
    private var geometry: Geometry? = null//标绘的图形对象（从markerUtil中获取）
    private var centerPoint: Point? = null//标绘的图形的中心点（从geometry中获取）

    private lateinit var createFolderDialog: CreateFolderDialog
    private lateinit var adapter: PoltSpinnerAdapter

    private var markerInfo: MarkerInfo? = null

    init {
        this.context = context
        this.listener1 = listener1
        this.markerUtil = markerUtil
        this.drawType = drawType



        if (_markerInfo != null) {
            isEdit = true
            this.markerInfo = _markerInfo

            geometry =  Geometry.fromJson(markerInfo!!.geometryJson)
            centerPoint = geometry!!.extent.center
        } else {
            isEdit = false
            geometry = markerUtil!!.getGeometry()
            centerPoint = geometry!!.extent.center
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_save_geo_dialog, null)
        vb = ViewSaveGeoDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)

        adapter = PoltSpinnerAdapter(context)
        vb!!.includePDV.spType.adapter = adapter
        markerInfo?.let {
            isEdit.yes {
                //赋值
                vb!!.includePDV.etName.setText(it.name)
                vb!!.includePDV.etRemark.setText(it.remark)
                //匹配文件夹
                adapter.list?.forEachIndexed { index, folderInfo ->
                    if (folderInfo.id == it.folderId) {
                        vb!!.includePDV.spType.setSelection(index)
                        return@forEachIndexed
                    }
                }
            }
        }

        //新建文件夹
        vb!!.includePDV.bhAddType.onClick {
            addFolder()
        }

        //相册
        vb!!.includePDV.btnCamera.onClick {
            createSaveInfo()
            saveKV("linkId", markerInfo?.id.toString())
            saveKV("dataType", ImageInfo.IMAGE_MARKER)
            saveKV("enableSelection", false)
            OperationLogger.logOperation(context, "[PhotoDialogActivity] SaveGeometyDialog 发送GO_PHOTO事件 linkId=${markerInfo?.id}, dataType=${ImageInfo.IMAGE_MARKER}")
            EventBus.getDefault().post(DataEvent(DataEvent.GO_PHOTO))//打开相册页面对话框
        }

        //多媒体附件
        vb!!.includePDV.btnLookFile.onClick {
            createSaveInfo()
            saveKV("linkId", markerInfo!!.id.toString())
            saveKV("dataType", AccessoryInfo.ACCESSORY_MARKER)
            EventBus.getDefault().post(DataEvent(DataEvent.GO_MEDIA))//打开附件页面对话框
        }

        //取消保存
        vb!!.tvCancle.onClick {
            cancelSave()
            dismiss()
        }

        //确认保存
        vb!!.tvOk.onClick {
            confirmSave()
            dismiss()
        }
    }


    private fun confirmSave() {

        val saveName = vb!!.includePDV.etName.text.toString()
        if (saveName.isBlank()) {
            tip(context, context.resources.getString(R.string.name_can_not_empty))
            return
        }

        //判断名字是否重复
        val isDuplicate = db.checkMarkerNameDuplicate(saveName)
        //只有新增才判断名称重复 编辑就不用了
        if (isDuplicate && !isEdit) {
            Toast.makeText(context, "该名称已经存在，不可重复", Toast.LENGTH_SHORT).show()
            return
        }

        val strGeometryJson = geometry?.toJson()
        val strCenterPointJson = centerPoint?.toJson()

        createSaveInfo()

        markerInfo?.apply {
            name = saveName
            folderId = adapter.list[vb!!.includePDV.spType.selectedItemPosition].id
            remark = vb!!.includePDV.etRemark.text.toString()
            geometryJson = strGeometryJson.self()
            centerPointJson = strCenterPointJson.self()

            isEdit.no {
                //新增情况
                geoType = drawType
                createTimeStamp = TimeUtil.getCurrentStamp()
            }

        }

        //更新标绘信息
        updateMarkerInfo()

        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
        EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_MARKER_DATA))
    }

    //取消保存
    private fun cancelSave() {
        if (!isEdit) {
            markerInfo?.let {
                //删除已添加的附件
                db.deleteAccessoryInfoByLinkIdAndDataType(it.id.toString(), AccessoryInfo.ACCESSORY_MARKER)
//            val accessoryList = db.queryAccessoryInfoByLinkIdAndDataType(it.id.toString(), AccessoryInfo.ACCESSORY_MARKER)
//            if (CommonUtil.matchList(accessoryList))
//                "附件还没删干净".printMsg()
//            else
//                "附件还删干净了".printMsg()

                //删除已添加的图片
                //先查出来 图片列表，然后遍历列表删除图片文件，文件删完后，再删除数据库数据
                val imagelist = db.queryImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)
                CommonUtil.matchList(imagelist).yes {
                    imagelist!!.forEach { img ->
                        val file = File(img.filePath)
                        if (file.exists() && file.isFile) {
                            file.delete()
                        }
                    }
                }

                //删除数据库中的图片记录
                db.deleteImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)

//            if (CommonUtil.matchList(db.queryImageInfoByLinkIdAndDataType(it.id.toString(), ImageInfo.IMAGE_MARKER)))
//                "图片还没删干净".printMsg()
//            else
//                "图片还删干净了".printMsg()

                //删掉当前这条数据
                db.deleteMarker(it)

            }
        }


    }


    /**
     * 新建文件夹
     */
    private fun addFolder() {

        if (!::createFolderDialog.isInitialized) {
            createFolderDialog = CreateFolderDialog(context, object : ActionListener {
                override fun onAction(obj: Any?, flag: Int) {
                    adapter = PoltSpinnerAdapter(context)
                    vb!!.includePDV.spType.setAdapter(adapter)
                    vb!!.includePDV.spType.setSelection(adapter.count - 1)
                    EventBus.getDefault().post(DataEvent(DataEvent.UPDATE_MARKER_DATA))
                }
            })
        }
        if (!createFolderDialog.isShowing) {
            createFolderDialog.show()
        }
    }

    override fun dismiss() {
        super.dismiss()
        isEdit.no { markerInfo = null }

    }


    /**
     * 创建一个待保存对象
     */
    private fun createSaveInfo() {
        if (markerInfo == null) {
            markerInfo = MarkerInfo()
            db.saveMarker(markerInfo!!)
            markerInfo = db.queryLastMarkerInfo()
        }
    }

    /**
     * 更新保存标绘信息
     */
    private fun updateMarkerInfo() = markerInfo?.let { db.updateMarkerInfo(it) }
}
