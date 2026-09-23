package com.jwch.gwyt_project.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.adapter.SatelliteAdapter
import com.jwch.gwyt_project.databinding.PageSatelliteBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.model.OVERVIEW_GROUPS
import com.jwch.gwyt_project.model.SatelliteModel
import com.jwch.gwyt_project.model.constellationGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 卫星信号检测页
 * 通过 GnssStatus.Callback 实时获取每颗可见/在用卫星，展示卫星伴侣式概览与明细。
 */
class SatelliteActivity : BaseActivity<PageSatelliteBinding>() {

    private companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private val COLOR_GREEN = Color.parseColor("#4CAF50")
        private val COLOR_AMBER = Color.parseColor("#F0A020")
        private const val MSG_SEARCHING = "正在搜索卫星信号…\n建议到开阔地带，冷启动约需数秒到数十秒"
        private const val MSG_GPS_OFF = "系统定位(GPS)未开启\n请在系统设置中开启定位后返回本页"
    }

    private val locationManager: LocationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)

    private lateinit var satelliteAdapter: SatelliteAdapter
    private lateinit var pillNameViews: Array<TextView>
    private lateinit var pillUseViews: Array<TextView>
    private lateinit var pillVisViews: Array<TextView>

    private var gnssCallback: GnssStatus.Callback? = null
    private var hasData = false
    private var askedPermission = false

    override fun getPageTitle(): String = "卫星信号检测"

    override fun initView() {
        satelliteAdapter = SatelliteAdapter()
        vb.rvSatellites.layoutManager = LinearLayoutManager(this)
        vb.rvSatellites.adapter = satelliteAdapter

        // 概览分组：全称 + 星座专属色；每格显示 可用/可见
        pillNameViews = arrayOf(vb.tvSatSysName0, vb.tvSatSysName1, vb.tvSatSysName2, vb.tvSatSysName3, vb.tvSatSysName4)
        pillUseViews = arrayOf(vb.tvSatSysUse0, vb.tvSatSysUse1, vb.tvSatSysUse2, vb.tvSatSysUse3, vb.tvSatSysUse4)
        pillVisViews = arrayOf(vb.tvSatSysVis0, vb.tvSatSysVis1, vb.tvSatSysVis2, vb.tvSatSysVis3, vb.tvSatSysVis4)
        OVERVIEW_GROUPS.forEachIndexed { index, (name, color) ->
            pillNameViews[index].text = name
            pillNameViews[index].setTextColor(color)
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasFineLocationPermission()) {
            startGnss()
        } else {
            if (!askedPermission) {
                askedPermission = true
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            } else {
                showEmpty("未获得定位权限\n请在系统设置中授予「定位」权限")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopGnss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGnss()
            } else {
                showEmpty("未获得定位权限\n请在系统设置中授予「定位」权限")
            }
        }
    }

    private fun hasFineLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun isProviderOn(): Boolean = try {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    } catch (e: Exception) {
        false
    }

    /** 注册 GNSS 卫星状态监听（回调在主线程，约 1Hz） */
    @SuppressLint("MissingPermission")
    private fun startGnss() {
        if (gnssCallback != null) return

        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val items = ArrayList<SatelliteModel>(status.satelliteCount)
                val groupVis = IntArray(OVERVIEW_GROUPS.size)
                val groupUse = IntArray(OVERVIEW_GROUPS.size)
                var usedCount = 0
                for (i in 0 until status.satelliteCount) {
                    val type = status.getConstellationType(i)
                    val usedInFix = status.usedInFix(i)
                    val group = constellationGroup(type)
                    groupVis[group]++
                    if (usedInFix) {
                        usedCount++
                        groupUse[group]++
                    }
                    items.add(
                        SatelliteModel(
                            constellationType = type,
                            svid = status.getSvid(i),
                            cn0 = status.getCn0DbHz(i),
                            elevation = status.getElevationDegrees(i),
                            azimuth = status.getAzimuthDegrees(i),
                            carrierFreqHz = if (status.hasCarrierFrequencyHz(i)) {
                                status.getCarrierFrequencyHz(i).toDouble()
                            } else 0.0,
                            usedInFix = usedInFix,
                            hasEphemeris = status.hasEphemerisData(i)
                        )
                    )
                }
                // 排序：参与定位的在前，其次按信号强度从强到弱
                items.sortWith(
                    compareByDescending<SatelliteModel> { it.usedInFix }
                        .thenByDescending { it.cn0 }
                        .thenBy { it.svid }
                )
                // logSnapshot(items, usedCount) // 每秒打印卫星明细，验收时按需开启
                render(items, usedCount, groupVis, groupUse)
            }
        }

        val supported = try {
            locationManager.registerGnssStatusCallback(callback, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            false
        }
        gnssCallback = callback

        if (!supported) {
            showEmpty("当前设备不支持读取卫星状态")
            return
        }
        updateEmptyState()
    }

    private fun stopGnss() {
        val callback = gnssCallback ?: return
        try {
            locationManager.unregisterGnssStatusCallback(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        gnssCallback = null
    }

    // /**
    //  * 把这一秒的卫星快照打到 logcat（TAG=GnssStatus）
    //  * 供户外验收时核对每颗星的原始信号质量
    //  */
    // private fun logSnapshot(list: List<SatelliteModel>, usedCount: Int) {
    //     Log.i(TAG, "GNSS 更新: 可见=${list.size} 在用=$usedCount")
    //     list.forEach { s ->
    //         val freq = if (s.carrierFreqHz > 0) {
    //             String.format(Locale.CHINA, "%.1fM", s.carrierFreqHz / 1e6)
    //         } else "—"
    //         Log.i(
    //             TAG,
    //             "  星座=${constellationName(s.constellationType)} svid=${s.svid} " +
    //                     "C/N0=${String.format(Locale.CHINA, "%.1f", s.cn0)}dB-Hz " +
    //                     "仰角=${s.elevation.toInt()}° 方位=${s.azimuth.toInt()}° " +
    //                     "频率=$freq 星历=${s.hasEphemeris} ${if (s.usedInFix) "在用" else "可见"}"
    //         )
    //     }
    // }

    /** 刷新卫星数据到界面 */
    private fun render(list: MutableList<SatelliteModel>, usedCount: Int, groupVis: IntArray, groupUse: IntArray) {
        hasData = true
        vb.tvUsedCount.text = usedCount.toString()
        vb.tvVisibleCount.text = list.size.toString()

        for (i in groupVis.indices) {
            pillUseViews[i].text = "可用 ${groupUse[i]}"
            pillVisViews[i].text = "可见 ${groupVis[i]}"
        }

        // 状态行
        val hasFix = usedCount > 0
        vb.tvSatState.text = if (hasFix) {
            "已定位：正在使用 $usedCount 颗卫星解算"
        } else {
            "未定位：正在接收卫星信号（可见 ${list.size} 颗）"
        }
        vb.tvSatState.setTextColor(if (hasFix) COLOR_GREEN else COLOR_AMBER)

        // 更新时间
        vb.tvUpdateTime.text = "更新 " + timeFormat.format(Date())

        // 空态
        if (list.isEmpty()) {
            showEmpty(MSG_SEARCHING)
        } else {
            vb.tvEmpty.gone()
        }

        satelliteAdapter.setNewData(list)
    }

    /** 根据 GPS 开关/是否有数据 更新空态提示 */
    private fun updateEmptyState() {
        if (hasData) {
            vb.tvEmpty.gone()
            if (!isProviderOn()) {
                vb.tvSatState.text = "GPS 定位已关闭，信号停止刷新"
                vb.tvSatState.setTextColor(COLOR_AMBER)
            }
        } else {
            showEmpty(if (isProviderOn()) MSG_SEARCHING else MSG_GPS_OFF)
        }
    }

    private fun showEmpty(message: String) {
        vb.tvEmpty.text = message
        vb.tvEmpty.show()
    }
}
