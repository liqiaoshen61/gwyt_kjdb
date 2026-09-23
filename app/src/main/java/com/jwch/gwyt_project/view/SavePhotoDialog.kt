package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.RadioButton
import com.jameni.allutillib.common.CommonUtil.tip
import com.jameni.allutillib.common.SoftKey
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.PhotoDialogActivity
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.databinding.ViewSavePhotoDialogBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.util.image_selector.PicSelectUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.find


/**
 * 保存图片对话框
 */
class SavePhotoDialog(context: Context, var listener1: ActionListener) : JameniBaseDialog(context) {

    var vb: ViewSavePhotoDialogBinding? = null

    var mLinkId: String = ""//关联id
    var mDataType = 0//图片类型id
    var picUrl: String? = null
    var isScreenShot = false //是否同步截图保存


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_save_photo_dialog, null)
        vb = ViewSavePhotoDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)


        vb!!.tvOk.onClick {
            val photoName =  vb!!.etName.text.toString()

            if (photoName.isBlank()) {
                tip(context, context.resources.getString(R.string.name_can_not_empty))
                return@onClick
            } else if (db.isExistImageInfo(mLinkId, photoName, ".png")) {  //是否存在相同名字的图片
                tip(context, context.resources.getString(R.string.name_is_exisit))
                return@onClick
            }

            try {
                val fileNameTime = "${photoName}_${TimeUtil.getCurrentDate("yyyyMMdd HHmmss")}"

                val file_path: String = Tools.getPhotoOnlyPath(fileNameTime)//最终的图片地址
//                Tools.renameFile(picUrl, file_path) //更改名称
                Tools.copyFileModern(picUrl, file_path) //复制文件


                val data = ImageInfo(mLinkId, photoName, picUrl,  vb!!.etRemark.text.toString(), TimeUtil.getCurrentStamp(), mDataType)
                db.saveImageInfo(data)//存入数据库

                SoftKey.closeSoftKeyboard( vb!!.etName, context)
                SoftKey.closeSoftKeyboard( vb!!.etRemark, context)
                tip(context, context.resources.getString(R.string.save_success))
//                "图片：${db.queryImageInfoByLinkIdAndDataType(mLinkId,mDataType).toJson()}".printMsg()

                if(isScreenShot){
                    listener1.onAction(data, PhotoDialogActivity.ACTION_SAVE_WITH_SCREENSHOT)
                } else {
                    listener1.onAction(data, PhotoDialogActivity.ACTION_SAVE)
                }
                dismiss()
            } catch (e: Exception) {
                "图片保存失败：${e.message}".printMsg()
                tip(context, context.resources.getString(R.string.save_failed))
            }

        }

        vb!!.rgScreenshot.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.cbScreenshot1 -> {
                    find<RadioButton>(i).isChecked.yes {
                        isScreenShot = false
                    }
                }
                R.id.cbScreenshot2 -> {
                    find<RadioButton>(i).isChecked.yes {
                        isScreenShot = true
                    }
                }
                else -> {
                }
            }
        }

        vb!!.tvCancle.onClick {
            dismiss()
            vb!!.etName.setText("")
        }

    }




    fun setData(linkId: String, dataType: Int, picUrl: String) {
        setData(linkId, dataType, picUrl, PicSelectUtil.SOURCE_CAMERA)
    }

    /**
     * @param source 图片来源：[PicSelectUtil.SOURCE_CAMERA] 拍照 / [PicSelectUtil.SOURCE_ALBUM] 相册。
     * 相册图片不携带当时定位/地图上下文，无法同步截图地图，故隐藏"同时保存截屏地图"选项并强制为否。
     */
    fun setData(linkId: String, dataType: Int, picUrl: String, source: Int) {
        this.mLinkId = linkId
        this.mDataType = dataType
        this.picUrl = picUrl

        vb!!.etName.setText(this.mLinkId)

        val fromAlbum = source == PicSelectUtil.SOURCE_ALBUM
        //相册来源：隐藏截图选项行，且强制不截图
        vb!!.llScreenshot.visibility = if (fromAlbum) View.GONE else View.VISIBLE
        if (fromAlbum) {
            isScreenShot = false
            vb!!.cbScreenshot1.isChecked = true
            vb!!.cbScreenshot2.isChecked = false
        }
    }






}
