package com.jwch.gwyt_project.activity

import android.content.Intent
import com.jameni.allutillib.app.SystemUtil
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config.START_PAGE_DELAY_TIME
import com.jwch.gwyt_project.databinding.PageStartBinding
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.PermisionResultListener
import com.jwch.gwyt_project.util.FileCheckUtil
import com.jwch.gwyt_project.util.PermisionNewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity

class AppStartActivity : BaseActivity<PageStartBinding>(), PermisionResultListener {

    var appName = "公务用图系统"


    override fun initView() {

        vb.tvAppName.text = appName

//        isNotEmpty(appName).yes {
//            tvAppName.text = appName
//        }.no {
//            val appInfo = DbUtil.db.queryAppInfo()
//            appInfo?.let {
//                tvAppName.text = it.appName.self()
//            }
//        }


        setHeadVisible(false)
        vb.tvVersion.text = "版本：${SystemUtil.getVersionName(context)} (${SystemUtil.getVersionCode(context)}})"


        PermisionNewUtil(this) { isGrant ->
            isGrant.yes {
                fileManagerPermission()

            }
        }.checkMyPermision()

    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPermisionResultListener(isGrant: Boolean) {
        if (isGrant) goMain()

    }

    fun goMain() {


        // 检查必要的本地文件是否存在，如果缺失则弹窗提示后退出
        if (!checkRequiredFiles()) {
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            delay(START_PAGE_DELAY_TIME)
            startActivity<MainActivity>()
//            startActivity<TestMapActivity>()
//            startActivity<MeasureCameraActivity>()


//            if (AppContext.app.islogin()) {
//                startActivity<MainActivity>()
//            } else {
//                startActivity<LoginActivity>()
//            }
            finish()

        }

    }


    /**
     * 检查必要的本地文件是否存在
     */
    private fun checkRequiredFiles(): Boolean {
        // 先检查应用数据目录
        if (!FileCheckUtil.checkAppDataDir()) {
            showRequiredFileDialog("未找到应用数据目录 /sdcard/OfficeMap/，请确保已将OfficeMap文件夹复制到手机存储根目录。")
            return false
        }

        // 检查数据库文件
        val missingFiles = FileCheckUtil.checkRequiredFiles()
        if (missingFiles.isNotEmpty()) {
            val message = FileCheckUtil.getMissingFilesDescription()
            showRequiredFileDialog(message)
            return false
        }


        return true
    }

    /**
     * 显示缺少必要文件的提示对话框
     */
    private fun showRequiredFileDialog(message: String) {
        // 在消息前加上标题
        val fullMessage = "文件缺失\n\n$message"
        showSingleDialog(this, fullMessage) { _, _ ->
            // 用户点击确定后退出应用
            finish()
            // 杀掉进程确保完全退出
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }


    fun fileManagerPermission() {
        // MANAGE_EXTERNAL_STORAGE 权限已在 PermisionNewUtil 中统一申请
        // 这里直接进入主页即可
        goMain()
    }

}
