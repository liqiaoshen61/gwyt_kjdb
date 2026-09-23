package com.jwch.gwyt_project.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.MainActivity
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.util.LocationCacheManager

/**
 * 后台定位服务
 *
 * 功能：
 * - 在后台持续监听GPS位置更新
 * - 自动缓存有效位置到 LocationCacheManager
 * - 通过前台服务保持后台运行（避免被系统杀死）
 * - 支持定位状态变化通知
 *
 * 使用场景：
 * - 纯离线地图应用，需要提前获取GPS位置
 * - 用户点击定位按钮时能快速返回结果
 */
class BackgroundLocationService : Service(), LocationListener {

    companion object {
        private const val TAG = "BackgroundLocationService"

        // 通知相关
        private const val NOTIFICATION_CHANNEL_ID = "location_service_channel"
        private const val NOTIFICATION_ID = 1001

        // 定位参数
        private const val MIN_TIME_INTERVAL = 10_000L // 最小定位间隔：10秒
        private const val MIN_DISTANCE_INTERVAL = 10f // 最小位移距离：10米

        // 定位精度阈值
        private const val ACCEPTABLE_ACCURACY = 200f // 接受的精度：200米
        private const val GOOD_ACCURACY = 50f // 良好精度：50米

        // 缓存更新最小间隔（避免频繁写入）
        private const val MIN_CACHE_UPDATE_INTERVAL = 5_000L // 5秒

        // Actions
        const val ACTION_START = "com.jwch.gwyt_project.action.START_LOCATION"
        const val ACTION_STOP = "com.jwch.gwyt_project.action.STOP_LOCATION"

        // 状态广播
        const val ACTION_LOCATION_UPDATE = "com.jwch.gwyt_project.action.LOCATION_UPDATE"
        const val ACTION_STATUS_CHANGE = "com.jwch.gwyt_project.action.STATUS_CHANGE"

        // 广播额外数据
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_STATUS = "status"
        const val EXTRA_MESSAGE = "message"

        // 服务状态
        enum class Status {
            IDLE,           // 空闲
            SEARCHING,      // 搜索卫星中
            LOCATION_FOUND, // 已找到位置
            ERROR           // 错误
        }

        // 当前状态
        private var currentStatus: Status = Status.IDLE

        // 单例引用
        @Volatile
        private var instance: BackgroundLocationService? = null

        fun getInstance(): BackgroundLocationService? = instance

        fun isRunning(): Boolean = instance != null

        fun getCurrentStatus(): Status = currentStatus
    }

    // Binder
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundLocationService = this@BackgroundLocationService
    }

    // 系统服务
    private lateinit var locationManager: LocationManager
    private var notificationManager: NotificationManager? = null

    // Handler
    private val handler = Handler(Looper.getMainLooper())

    // 上次缓存时间
    private var lastCacheTime = 0L

    // 最佳位置（用于精度收敛）
    private var bestLocation: Location? = null

    // 是否正在定位
    private var isLocating = false

    /**
     * 更新状态并发送通知
     */
    private fun updateStatus(newStatus: Status) {
        if (currentStatus != newStatus) {
            currentStatus = newStatus
            notifyStatusChange(newStatus)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        "BackgroundLocationService: 服务创建".printMsg()
        Log.d(TAG, "服务创建")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startLocationUpdates()
            ACTION_STOP -> stopLocationUpdates()
        }
        return START_STICKY // 被杀死后自动重启
    }

    override fun onDestroy() {
        stopLocationUpdates()
        instance = null
        currentStatus = Status.IDLE
        "BackgroundLocationService: 服务销毁".printMsg()
        Log.d(TAG, "服务销毁")
        super.onDestroy()
    }

    /**
     * 启动定位更新
     */
    fun startLocationUpdates() {
        if (isLocating) {
            Log.d(TAG, "定位已在运行中")
            return
        }

        try {
            // 启动前台服务
            startForeground(NOTIFICATION_ID, createNotification(Status.SEARCHING))

            // 检查GPS是否开启
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                updateStatus(Status.ERROR)
                updateNotification(Status.ERROR, "GPS未开启")
                Log.w(TAG, "GPS未开启")
                return
            }

            // 请求位置更新（仅使用GPS，离线场景）
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_INTERVAL,
                MIN_DISTANCE_INTERVAL,
                this
            )

            isLocating = true
            updateStatus(Status.SEARCHING)
            "BackgroundLocationService: 开始定位".printMsg()
            Log.d(TAG, "开始GPS定位")

            // 同时尝试获取最后已知位置
            tryGetLastKnownLocation()

        } catch (e: SecurityException) {
            Log.e(TAG, "定位权限缺失: ${e.message}")
            updateStatus(Status.ERROR)
            updateNotification(Status.ERROR, "缺少定位权限")
        } catch (e: Exception) {
            Log.e(TAG, "启动定位失败: ${e.message}")
            updateStatus(Status.ERROR)
            updateNotification(Status.ERROR, "启动定位失败")
        }
    }

    /**
     * 停止定位更新
     */
    fun stopLocationUpdates() {
        if (!isLocating) return

        try {
            locationManager.removeUpdates(this)
            isLocating = false
            updateStatus(Status.IDLE)
            "BackgroundLocationService: 停止定位".printMsg()
            Log.d(TAG, "停止GPS定位")
        } catch (e: Exception) {
            Log.e(TAG, "停止定位失败: ${e.message}")
        }
    }

    /**
     * 尝试获取最后已知位置
     */
    private fun tryGetLastKnownLocation() {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null && isBetterLocation(location, bestLocation)) {
                    bestLocation = location
                    onLocationUpdate(location)
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "获取最后已知位置失败: ${e.message}")
        }
    }

    // ===== LocationListener 回调 =====

    override fun onLocationChanged(location: Location) {
        "BackgroundLocationService: 收到位置更新 accuracy=${location.accuracy}m".printMsg()
        Log.d(TAG, "位置更新: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")

        if (isBetterLocation(location, bestLocation)) {
            bestLocation = location
            onLocationUpdate(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "Provider状态变化: $provider, status=$status")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Provider启用: $provider")
        if (provider == LocationManager.GPS_PROVIDER && !isLocating) {
            startLocationUpdates()
        }
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "Provider禁用: $provider")
        if (provider == LocationManager.GPS_PROVIDER) {
            updateStatus(Status.ERROR)
            updateNotification(Status.ERROR, "GPS已关闭")
        }
    }

    // ===== 私有方法 =====

    private fun onLocationUpdate(location: Location) {
        val accuracy = if (location.hasAccuracy()) location.accuracy else 1000f

        // 更新状态
        val newStatus = when {
            accuracy <= GOOD_ACCURACY -> Status.LOCATION_FOUND
            accuracy <= ACCEPTABLE_ACCURACY -> Status.LOCATION_FOUND
            else -> Status.SEARCHING
        }
        updateStatus(newStatus)

        // 检查是否需要缓存（节流）
        val now = System.currentTimeMillis()
        if (now - lastCacheTime >= MIN_CACHE_UPDATE_INTERVAL) {
            // 只缓存精度符合要求的位置
            if (accuracy <= ACCEPTABLE_ACCURACY) {
                LocationCacheManager.saveLocation(location)
                lastCacheTime = now
                Log.d(TAG, "位置已缓存: accuracy=${accuracy}m")
            }
        }

        // 更新通知
        updateNotification(currentStatus, "精度: ${String.format("%.0f", accuracy)}米")

        // 发送广播
        broadcastLocation(location)
    }

    /**
     * 判断新位置是否比旧位置更好
     */
    private fun isBetterLocation(newLocation: Location?, currentBestLocation: Location?): Boolean {
        if (newLocation == null) return false
        if (currentBestLocation == null) return true

        // 检查时间
        val timeDelta = newLocation.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > 120_000 // 2分钟
        val isSignificantlyOlder = timeDelta < -120_000
        val isNewer = timeDelta > 0

        if (isSignificantlyNewer) return true
        if (isSignificantlyOlder) return false

        // 检查精度
        val accuracyDelta = (newLocation.accuracy - currentBestLocation.accuracy).toInt()
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 100

        if (isMoreAccurate) return true
        if (isNewer && !isSignificantlyLessAccurate) return true

        return false
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "定位服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台定位服务通知"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * 创建通知
     */
    private fun createNotification(status: Status): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = when (status) {
            Status.IDLE -> "定位服务" to "等待启动"
            Status.SEARCHING -> "正在定位" to "搜索卫星中..."
            Status.LOCATION_FOUND -> "定位成功" to "已获取位置"
            Status.ERROR -> "定位异常" to "请检查GPS设置"
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 更新通知
     */
    private fun updateNotification(status: Status, message: String) {
        val notification = createNotification(status).let {
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(
                    when (status) {
                        Status.IDLE -> "定位服务"
                        Status.SEARCHING -> "正在定位"
                        Status.LOCATION_FOUND -> "定位成功"
                        Status.ERROR -> "定位异常"
                    }
                )
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 发送位置广播
     */
    private fun broadcastLocation(location: Location) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LATITUDE, location.latitude)
            putExtra(EXTRA_LONGITUDE, location.longitude)
            putExtra(EXTRA_ACCURACY, if (location.hasAccuracy()) location.accuracy else 1000f)
        }
        sendBroadcast(intent)
    }

    /**
     * 通知状态变化
     */
    private fun notifyStatusChange(status: Status) {
        val intent = Intent(ACTION_STATUS_CHANGE).apply {
            putExtra(EXTRA_STATUS, status.name)
            putExtra(EXTRA_MESSAGE, getStatusMessage(status))
        }
        sendBroadcast(intent)
    }

    private fun getStatusMessage(status: Status): String {
        return when (status) {
            Status.IDLE -> "定位服务空闲"
            Status.SEARCHING -> "正在搜索卫星"
            Status.LOCATION_FOUND -> "已获取位置"
            Status.ERROR -> "定位异常"
        }
    }

    // ===== 公开方法供外部调用 =====

    /**
     * 获取当前最佳位置
     */
    fun getBestLocation(): Location? = bestLocation

    /**
     * 获取当前定位状态描述
     */
    fun getStatusDescription(): String {
        return when (currentStatus) {
            Status.IDLE -> "定位服务未启动"
            Status.SEARCHING -> "正在搜索卫星..."
            Status.LOCATION_FOUND -> {
                val cached = LocationCacheManager.getCachedLocation()
                if (cached != null) {
                    "已定位 (精度: ${String.format("%.0f", cached.accuracy)}米, ${LocationCacheManager.getCacheAgeDescription()})"
                } else {
                    "已定位"
                }
            }
            Status.ERROR -> "定位异常，请检查GPS设置"
        }
    }
}
