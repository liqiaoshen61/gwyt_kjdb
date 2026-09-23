package  com.jwch.gwyt_project.util

import android.app.Activity
import android.os.Build
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.showSingleDialog

class PermisionNewUtil(val activity: Activity, block: (Boolean) -> Unit = { _ -> }) {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    val actionBlock = block

    fun checkMyPermision(){
        val builder = XXPermissions.with(activity)

        // Android 13+ 申请媒体权限和照片选择器权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.permission(Permission.READ_MEDIA_IMAGES)
                .permission(Permission.READ_MEDIA_VIDEO)
                .permission(Permission.READ_MEDIA_AUDIO)
        }

        // Android 11+ 申请文件管理权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.permission(Permission.MANAGE_EXTERNAL_STORAGE)
        }

        // 定位和相机权限
        builder.permission(Permission.ACCESS_COARSE_LOCATION)
            .permission(Permission.ACCESS_FINE_LOCATION)
            .permission(Permission.CAMERA)

        builder.request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                if (all) {
                    "onGranted-获取权限成功".printMsg()
                    actionBlock(true)
                }
            }

            override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                if (never) {
                    "onDenied-授权被永久拒绝，请手动授予权限".printMsg()
                    activity.runOnUiThread { showDialog(permissions) }
                } else {
                    "onDenied-授权被拒绝，请手动授予权限".printMsg()
                    activity.runOnUiThread { showDialog(permissions) }
                }
            }
        })
    }


    fun showDialog(permissions: MutableList<String>) {
        showSingleDialog(activity, "您拒绝了部分权限，无法进入应用，请到设置中打开权限。"){_, _ ->
            XXPermissions.startPermissionActivity(activity, permissions, PERMISSION_REQUEST_CODE)
        }
    }


}
