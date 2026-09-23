package com.jwch.gwyt_project.activity


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.jameni.jamenilistlib.i.ItemChildViewClickListener
import com.jameni.jamenilistlib.i.ItemClickListener
import com.jwch.gwyt_project.Info.AccessoryInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.adapter.MediaAdapter
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewMediaDialogBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.util.SoftUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.xutils.ex.DbException
import java.io.File

/**
 * 多媒体的Activity (Dialog样式)
 */
class MediaDialogActivity : FullScreenActivity<ViewMediaDialogBinding>(), ItemClickListener,
    ItemChildViewClickListener {

    var mLinkId: String = ""
    var mDataType = 0

    var adapter: MediaAdapter? = null
    var dataList: MutableList<AccessoryInfo>? = null
    val keyboard = SoftUtil()

    override fun initView() {

        mLinkId = getKV("linkId", "")
        mDataType = getKV("dataType", 0)

        dataList = mutableListOf()
        vb.lvOutputList.setLinearManager()
        adapter = MediaAdapter()
        vb.lvOutputList.adapter = adapter
        vb.lvOutputList.itemClickListener = this
        vb.lvOutputList.itemChildViewClickListener = this
        reloadData()
    }

    fun reloadData() {
        dataList = db.queryAccessoryInfoByLinkIdAndDataType(mLinkId, mDataType)
        vb.lvOutputList.update(dataList)
    }


    override fun initViewListener() {

        vb.llAddFile.onClick {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, Config.SAVE_EXTRA_FILE)
        }

        vb.tvClose.onClick {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (resultCode == RESULT_OK).yes {
            when (requestCode) {
                Config.SAVE_EXTRA_FILE -> {
                    //选择附件返回
                    try {
                        val uri = data!!.data
                        val filePath: String = Tools.getPath(this, uri)

                        val data = AccessoryInfo(mLinkId, filePath, mDataType)
                        db.saveAccessoryInfo(data)
                        keyboard.hideKeyboard(this)
                        tip("保存成功！")
                    } catch (e: DbException) {
                        e.printStackTrace()
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(context, "添加失败！", Toast.LENGTH_SHORT).show()
                    }
                    reloadData()
                }
            }
        }
    }


    override fun onItemClick(itemData: Any?, position: Int) {
        var uri: Uri? = null
        val item = itemData as AccessoryInfo
        val filePath = item.path
        val file = File(filePath)
        if (file.exists()) {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.action = Intent.ACTION_VIEW
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N).yes {
                    uri = FileProvider.getUriForFile(context, "com.jwch.njgtkjgh", file);
                }.no {
                    uri = Uri.fromFile(file)
                }

                intent.setDataAndType(
                    uri,
                    Tools.getFileType(filePath.substring(filePath.lastIndexOf("/") + 1))
                )
                context.startActivity(intent)
                Intent.createChooser(intent, "请选择对应的软件打开该附件！")
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "文件无法打开！", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "文件已被移除！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemChildViewClick(viewId: Int, position: Int) {
        when (viewId) {
            R.id.ivDeleteMedia -> {
                showNormalDialog(context!!, "是否确定删除") {
                    it.yes {
                        try {
                            dataList?.get(position)?.let {
                                db.deleteAccessoryInfo(it)
                                tip("删除成功")
                            }

                            reloadData()
                        } catch (e: DbException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
