package com.jwch.gwyt_project.util

import android.content.Context
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.toJson

class BaiduLocationService(context: Context) {

    val context = context
    var client: LocationClient
    lateinit var mOption: LocationClientOption
    val intervalSecond = 0 //间隔多少秒自动定位一次

    private var objLock: Any
    lateinit var block: (String, String, String) -> Unit

    lateinit var block3: (String, String, String, String, Double, Double) -> Unit


    init {
        LocationClient.setAgreePrivacy(true)
        objLock = Any()
        synchronized(objLock) {
            client = LocationClient(context)
            getDefaultLocationClientOption()
            client.locOption = mOption
        }
    }

    private fun getDefaultLocationClientOption() {

        mOption = LocationClientOption()
        mOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll") // 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        mOption.setScanSpan(1000.times(intervalSecond)) // 可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true) // 可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedLocationDescribe(true) // 可选，设置是否需要地址描述
        mOption.setNeedDeviceDirect(false) // 可选，设置是否需要设备方向结果
        mOption.isLocationNotify = false // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(true) // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop
        mOption.setIsNeedLocationDescribe(true) // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        mOption.setIsNeedLocationPoiList(true) // 可选，默认false，设置是否需要POI结果，可以在BDLocation
        mOption.SetIgnoreCacheException(false) // 可选，默认false，设置是否收集CRASH信息，默认收集
        mOption.isOpenGps = true // 可选，默认false，设置是否开启Gps定位
        mOption.setIsNeedAltitude(true) // 可选，默认false，设置定位时是否需要海拔信息。定位详情页需要海拔，故开启

    }

    fun start() {
//        synchronized(objLock) {
        client?.let {
//            isLocation = true
            client.start()
        }
//        }
    }

    fun stop() {
//        synchronized(objLock) {
        client?.let {
            "是否开启定位  ${client.isStarted}".printMsg()
            if (client.isStarted) {
                client.stop()
            }

        }
//        }

    }

    fun isStart(): Boolean = client.isStarted

    /***
     * 注册定位监听
     *
     * @param listener
     * @return
     */
    fun registerListener(listener: BDAbstractLocationListener?): Boolean {
        var isSuccess = false
        if (listener != null) {
            client.registerLocationListener(listener)
            isSuccess = true
        }
        return isSuccess
    }

    fun registerListener2(block: (String, String, String) -> Unit) {
        this.block = block
        client.registerLocationListener(mListener)
    }
    fun registerListener3(block: (String, String, String, String, Double, Double) -> Unit) {
        this.block3 = block
        client.registerLocationListener(mListener)
    }
    fun registerListener4(block: (String, String, String, String, Double, Double) -> Unit) {
        this.block3 = block
        client.registerLocationListener(mListener2)
    }

    /**
     * 把本次百度定位结果（精度/海拔/速度/方位）写入位置缓存，
     * 供"定位详细信息"页读取。百度返回精度用 getRadius()，提供者记为 baidu。
     */
    private fun saveBaiduDetail(location: BDLocation) {
        try {
            LocationCacheManager.saveLocationDetail(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = if (location.hasRadius()) location.radius else 0f,
                provider = "baidu",
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.direction.toFloat()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val mListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                location?.let {

                    if (it.locType != BDLocation.TypeServerError) {
                        it.toJson().printMsg()
                        val lat = it.latitude
                        val lng = it.longitude
                        var address = it.addrStr.self()
                        if (address.contains("中国")) {
                            address = address.replace("中国","")
                        }
                        val province = it.province.self()
                        val city = it.city.self()
                        val area = it.district.self()

                        saveBaiduDetail(it)

                        if (::block.isInitialized) {
                            block(province, city, area)
                        }

                        if (::block3.isInitialized) {
                            block3(province, city, area,address,lat,lng)
                        }

                    }
                }
                stop()
            }
        }
    }

    //持续定位 不自动关闭
    val mListener2: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                location?.let {

                    if (it.locType != BDLocation.TypeServerError) {
                        it.toJson().printMsg()
                        val lat = it.latitude
                        val lng = it.longitude
                        var address = it.addrStr.self()
                        if (address.contains("中国")) {
                            address = address.replace("中国","")
                        }
                        val province = it.province.self()
                        val city = it.city.self()
                        val area = it.district.self()

                        saveBaiduDetail(it)

                        if (::block.isInitialized) {
                            block(province, city, area)
                        }

                        if (::block3.isInitialized) {
                            block3(province, city, area,address,lat,lng)
                        }

                    }
                }

            }
        }
    }

}