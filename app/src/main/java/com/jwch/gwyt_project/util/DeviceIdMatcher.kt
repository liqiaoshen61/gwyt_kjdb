package com.jwch.gwyt_project.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.jwch.gwyt_project.ext.printMsg

object DeviceIdMatcher {
    // 本地字符串集合
    private val localStrings = setOf(
        "ac728c441a1eb4c6", //1T (经营部资产)
        "541c5fbc76603297",  //512G (开发部资产-文炽)
        "e0370a103c329058", //512G (开发部资产-乔深)
        "fca48cc05a12cbdd", //1T [鸿蒙5.1] (开发部资产)
        "8bf75a187894d756", //1T [鸿蒙6] (开发部资产) 跟上面是同一个

        //====以下是业主平板====

        "dcc4e279481ac605", //漳州1
        "ac5de5882ddd90ed", //漳州2
        "fd70ecc8d096f172", //漳州3

        "b5a0aa2f71f5ada7", //省厅1
        "47b1cc3869dcf970", //省厅2
        "4485686b3e154fb1", //省厅3
        "1fee2ddceaa157f4", //省厅4
        "2b0b340794bb6993", //省厅5

        "bedf0ced7861ba72", //龙岩1
        "c756f3d522c53bd0", //龙岩2
        "74b63fb2cbee82e2", //龙岩3
        "63857aca05eee2cd", //龙岩4(非业主平板)

        "802672167f72892f", //永春

        "3f5fc74aa6fa0039",  //长汀县1 [哄懵5.1]
        "5af81dfd1e56a463",  //长汀县2 [哄懵5.1]

        "f51f1751ab127255", //我的手机

        "18c339ce2188a0e4", //闽侯县1 [哄懵5.1]
        "31bba32a843a2d72", //闽侯县2 [哄懵5.1]
        "4f2e4d6477e1956c", //闽侯县

        "216cbe7f7a46c76c", //明溪

        "392688aeb7fb91c9", //漳平市（县级）

        "bd16287836b30cc9", //龙海区 [哄懵5.1]

        "11198af486084cd6", //仙游1
        "0a347888e6a48b70", //仙游2
        "238df72b053d96ce", //仙游3

        "905a03f82c887b19", //三明1
        "2e07c042360d568b", //三明2
        "3358f92034e2d8a9", //三明3

        "6ee48b2a610dd8a5", //泰宁县1
        "e854e65573d5b8ce", //泰宁县2

        "5d40354ca1de60d2", //永安市1
        "1839594338cf005e", //永安市2
        "ab10a6dbdbb95d09", //永安市3
        "397e5b7341d01786", //永安市4
        "8bb4558852f2c777", //永安市5
        "7cec7f756f01ed70", //永安市6
        "41290718c00b984b", //永安市7
        "e6bca553c3445d7e", //永安市8
        "97420bd08d03cb92", //永安市9
        "753f36723d040113", //永安市10
        "3592a7cf4bea6f49", //永安市11
        "637240bc5641759e", //永安市12
        "7ca637cfc8b533e7", //永安市13
        "ab2af5f419227f4e", //永安市14
        "55878be9a9f04c25", //永安市15
        "660bbbd82e97029a", //永安市16
        "b647b7ffe650655c", //永安市17

        "130a0ea5e87c0d19", //将乐县 [荣耀平板 安卓15]

    )

    // 使用HashSet提高查找效率
    private val stringSet = HashSet(localStrings)

    /**
     * 检查输入字符串是否存在于本地字符串集合中
     * @param input 要检查的字符串
     * @return 如果存在返回true，否则返回false
     */
    fun contains(input: String): Boolean {
        return stringSet.contains(input)
    }

    /**
     * 添加新字符串到集合（如果需要动态添加）
     */
    fun addString(newString: String) {
        stringSet.add(newString)
    }

    /**
     * 批量添加字符串到集合
     */
    fun addStrings(newStrings: Collection<String>) {
        stringSet.addAll(newStrings)
    }

    //是否为授权设备
    fun isAuthorizedDevice(context: Context) : Boolean{
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        "当前设备deviceId = ${deviceId}".printMsg()
        return  contains(deviceId)
//        return  false
    }

    /**
     * 获取设备序列号
     * 注意：Android 10+ 需要 READ_PHONE_STATE 运行时权限
     */
    fun getSerialNumber(context: Context): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED

                "READ_PHONE_STATE 权限状态: ${if (hasPermission) "已授权" else "未授权"}".printMsg()

                if (hasPermission) {
                    val serial = Build.getSerial()
                    "设备序列号: $serial".printMsg()
                    serial
                } else {
                    "缺少 READ_PHONE_STATE 权限，无法获取序列号".printMsg()
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                val serial = Build.SERIAL
                "设备序列号 (旧API): $serial".printMsg()
                serial
            }
        } catch (e: Exception) {
            "获取序列号失败: ${e.message}".printMsg()
            null
        }
    }

    /**
     * 打印设备所有标识信息（调试用）
     */
    fun printDeviceInfo(context: Context) {
        "====== 设备标识信息 ======".printMsg()
        "Android ID: ${Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)}".printMsg()
        "序列号: ${getSerialNumber(context)}".printMsg()
        "Brand: ${Build.BRAND}".printMsg()
        "Model: ${Build.MODEL}".printMsg()
        "Device: ${Build.DEVICE}".printMsg()
        "Manufacturer: ${Build.MANUFACTURER}".printMsg()
        "SDK版本: ${Build.VERSION.SDK_INT}".printMsg()
        "===========================".printMsg()
    }
}
