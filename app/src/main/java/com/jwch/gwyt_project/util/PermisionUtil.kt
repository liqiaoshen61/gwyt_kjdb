package com.jwch.gwyt_project.util


import android.app.Activity
import android.content.Context
import android.os.Build
import com.jameni.allutillib.app.SystemUtil
import com.jameni.basepage_lib.util.PageManager
import com.jameni.jamenidialoglib.JameniDialog
import com.jameni.jamenidialoglib.i.SingleDialogListener
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.toJson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

class PermisionUtil(val activity: Activity, block: (Boolean) -> Unit = { _ -> }) : SingleDialogListener {

    val actionBlock = block
    val isSDK29  = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    var doing = false

    var permisionList: Array<String> = if (isSDK29) arrayOf(
        Permission.READ_EXTERNAL_STORAGE,
        Permission.WRITE_EXTERNAL_STORAGE,
        Permission.ACCESS_COARSE_LOCATION,
        Permission.ACCESS_FINE_LOCATION,
        Permission.CAMERA,
//        Permission.READ_PHONE_STATE,
    ) else arrayOf(
        Permission.READ_EXTERNAL_STORAGE,
        Permission.WRITE_EXTERNAL_STORAGE,
        Permission.ACCESS_COARSE_LOCATION,
        Permission.ACCESS_FINE_LOCATION,
        Permission.CAMERA,
//        Permission.READ_PHONE_STATE,
    )


    fun checkPermision() {

        AndPermission
            .with(activity)
            .runtime()
            .permission(permisionList)
            .onGranted { permissions ->
                if (!doing){
                    doing = true
                    if (AndPermission.hasPermissions(activity as Context, permisionList)) {
                        //权限被通过 再去申请后台定位权限
                        "1 获取成功".printMsg()
                        actionBlock(true)
                    } else {
                        //权限被标记为 不再提示
                        activity.runOnUiThread { showDialog() }
                    }
                }
            }
            .onDenied { permissions ->
                "1-onDenied   ${ permissions.toJson()}".printMsg()

                if (!AndPermission.hasPermissions(activity as Context, permisionList)) {
                    permissions?.let {
                        activity.runOnUiThread { showDialog() }
                    }
                }

            }.start()

    }

    //Manifest.permission.ACCESS_BACKGROUND_LOCATION 需要单独申请
    //argetSdk>=29时，后台权限需要前台权限至少有一个权限申请且被同意了，才可以单独申请后台权限
    //如果没有获得ACCESS_COARSE_LOCATION 或ACCESS_FINE_LOCATION至少一种权限
    // 而直接只申请ACCESS_BACKGROUND_LOCATION则系统不会展示权限申请框，回调权限标为拒绝
    fun checkBackgroundLocation() {

        AndPermission
            .with(activity)
            .runtime()
            .permission(Permission.ACCESS_BACKGROUND_LOCATION)
            .onGranted { permissions ->
                if (AndPermission.hasPermissions(activity as Context, Permission.ACCESS_BACKGROUND_LOCATION)) {
                    //权限被通过
                    actionBlock(true)
                } else {
                    //权限被标记为 不再提示
                    activity.runOnUiThread { showDialog() }
                }
            }
            .onDenied { permissions ->
                "2-onDenied   ${ permissions.toJson()}".printMsg()
                if (!AndPermission.hasPermissions(activity as Context, Permission.ACCESS_BACKGROUND_LOCATION)) {
                    permissions?.let {
                        if (permissions.size == 1 && (permissions[0].equals("android.permission.ACCESS_BACKGROUND_LOCATION") || permissions[0].equals("android.permission.CAMERA"))) {
                            actionBlock(true)
                        } else {
                            activity.runOnUiThread { showDialog() }
                        }

                    }
                }

            }.start()

    }



    fun showDialog() {
        JameniDialog.Builder(activity)
            .setMsg("您拒绝了部分权限，无法进入应用，请到设置中打开权限。")
            .setSingleBtnText("去设置")
            .setSingleDialogListener(this)
            .showSingleDialog()
    }

    override fun onSingleOk(obj: Any?, actionTag: Int) {
        SystemUtil.goSystemSettingPage(activity)
        PageManager.exit()
    }
}