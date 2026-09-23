package com.jwch.gwyt_project.util

import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.saveKV
import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 位置缓存管理器
 *
 * 用于持久化存储最后已知的GPS位置，支持：
 * - 快速响应用户定位请求（使用缓存位置）
 * - 应用重启后恢复位置信息
 * - 位置有效性检查（过期自动丢弃）
 */
object LocationCacheManager {

    private const val TAG = "LocationCacheManager"

    // 缓存键名
    private const val KEY_CACHED_LOCATION = "cached_location"
    private const val KEY_LAST_UPDATE_TIME = "location_last_update_time"
    private const val KEY_LOCATION_ACCURACY = "location_accuracy"
    private const val KEY_LOCATION_PROVIDER = "location_provider"

    // 位置有效期（超过此时间的缓存位置视为过期）
    private const val LOCATION_VALID_DURATION_MS = 10 * 60 * 1000L // 10分钟

    // 最小有效精度（超过此精度的位置视为无效）
    private const val MIN_VALID_ACCURACY = 100f // 100米

    // 缓存的位置数据
    private var cachedLocation: CachedLocation? = null

    /**
     * 缓存的位置数据类
     */
    data class CachedLocation(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val provider: String,
        val timestamp: Long,
        val altitude: Double = 0.0,
        val speed: Float = 0f,
        val bearing: Float = 0f
    ) : Parcelable {
        constructor(parcel: android.os.Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readFloat(),
            parcel.readString() ?: "",
            parcel.readLong(),
            parcel.readDouble(),
            parcel.readFloat(),
            parcel.readFloat()
        )

        override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
            parcel.writeFloat(accuracy)
            parcel.writeString(provider)
            parcel.writeLong(timestamp)
            parcel.writeDouble(altitude)
            parcel.writeFloat(speed)
            parcel.writeFloat(bearing)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : android.os.Parcelable.Creator<CachedLocation> {
            override fun createFromParcel(parcel: android.os.Parcel): CachedLocation {
                return CachedLocation(parcel)
            }

            override fun newArray(size: Int): Array<CachedLocation?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * 保存位置到缓存
     */
    fun saveLocation(location: Location) {
        val cached = CachedLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = if (location.hasAccuracy()) location.accuracy else 1000f,
            provider = location.provider ?: "unknown",
            timestamp = location.time,
            altitude = if (location.hasAltitude()) location.altitude else 0.0,
            speed = if (location.hasSpeed()) location.speed else 0f,
            bearing = if (location.hasBearing()) location.bearing else 0f
        )

        saveCachedLocation(cached)
        "$TAG: 保存位置缓存 lat=${cached.latitude}, lng=${cached.longitude}, accuracy=${cached.accuracy}m".also {
            android.util.Log.d(TAG, it)
        }
    }

    /**
     * 保存位置到缓存（手动指定参数）
     *
     * 采用"合并保留"策略：仅当新值非 0（表示本 provider 实际提供了该字段）时才覆盖，
     * 否则回退保留上一次缓存的 altitude/speed/bearing/accuracy。
     * 这样统一定位层用 lat/lng 覆盖写缓存时，不会抹掉 GPS/华为/百度在 provider 层
     * 已写入的海拔、速度、方位角等真实数据（详情页依赖这些字段）。
     */
    /**
     * 保存位置到缓存（全字段，供不提供 android.location.Location 的 provider 使用，如百度）
     * 仅在 0 值表示"无数据"的字段上做合并保留，其余直接覆盖。
     */
    fun saveLocationDetail(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        provider: String,
        altitude: Double,
        speed: Float,
        bearing: Float
    ) {
        val prev = cachedLocation ?: loadCachedLocation()
        val cached = CachedLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = if (accuracy > 0f) accuracy else (prev?.accuracy ?: 0f),
            provider = provider,
            timestamp = System.currentTimeMillis(),
            altitude = if (altitude != 0.0) altitude else 0.0,
            speed = if (speed != 0f) speed else 0f,
            bearing = if (bearing != 0f) bearing else 0f
        )

        saveCachedLocation(cached)
    }

    fun saveLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        provider: String = "gps"
    ) {
        val prev = cachedLocation ?: loadCachedLocation()
        val mergedAccuracy = if (accuracy > 0f) accuracy else (prev?.accuracy ?: 0f)
        val cached = CachedLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = mergedAccuracy,
            provider = provider,
            timestamp = System.currentTimeMillis(),
            altitude = if (prev?.altitude != 0.0) prev!!.altitude else 0.0,
            speed = if (prev?.speed != 0f) prev!!.speed else 0f,
            bearing = if (prev?.bearing != 0f) prev!!.bearing else 0f
        )

        saveCachedLocation(cached)
    }

    /**
     * 获取缓存的位置
     * @param maxAgeMs 最大有效期（毫秒），默认30分钟
     * @param maxAccuracy 最大精度（米），默认300米
     * @return 有效位置，如果过期或不存在则返回null
     */
    fun getCachedLocation(
        maxAgeMs: Long = LOCATION_VALID_DURATION_MS,
        maxAccuracy: Float = MIN_VALID_ACCURACY
    ): CachedLocation? {
        // 先尝试内存缓存
        cachedLocation?.let { cached ->
            if (isLocationValid(cached, maxAgeMs, maxAccuracy)) {
                return cached
            }
        }

        // 从持久化存储加载
        loadCachedLocation()?.let { cached ->
            if (isLocationValid(cached, maxAgeMs, maxAccuracy)) {
                cachedLocation = cached
                return cached
            }
        }

        return null
    }

    /**
     * 获取缓存位置（不检查有效期）
     * 用于显示最后已知位置，即使已过期
     */
    fun getLastKnownLocation(): CachedLocation? {
        cachedLocation?.let { return it }
        return loadCachedLocation()
    }

    /**
     * 检查是否有有效的缓存位置
     */
    fun hasValidCache(): Boolean {
        return getCachedLocation() != null
    }

    /**
     * 获取缓存位置的年龄（毫秒）
     */
    fun getCacheAge(): Long {
        val cached = cachedLocation ?: loadCachedLocation() ?: return Long.MAX_VALUE
        return System.currentTimeMillis() - cached.timestamp
    }

    /**
     * 获取缓存位置的人类可读年龄描述
     */
    fun getCacheAgeDescription(): String {
        val age = getCacheAge()
        return when {
            age == Long.MAX_VALUE -> "无缓存"
            age < 60_000 -> "${age / 1000}秒前"
            age < 3600_000 -> "${age / 60_000}分钟前"
            age < 86400_000 -> "${age / 3600_000}小时前"
            else -> "${age / 86400_000}天前"
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        cachedLocation = null
        MMKV.defaultMMKV().remove(KEY_CACHED_LOCATION)
        MMKV.defaultMMKV().remove(KEY_LAST_UPDATE_TIME)
        android.util.Log.d(TAG, "位置缓存已清除")
    }

    /**
     * 将缓存位置转换为 Android Location 对象
     */
    fun toAndroidLocation(cached: CachedLocation): Location {
        return Location(cached.provider).apply {
            latitude = cached.latitude
            longitude = cached.longitude
            accuracy = cached.accuracy
            time = cached.timestamp
            if (cached.altitude != 0.0) altitude = cached.altitude
            if (cached.speed != 0f) speed = cached.speed
            if (cached.bearing != 0f) bearing = cached.bearing
        }
    }

    // ===== 私有方法 =====

    private fun saveCachedLocation(cached: CachedLocation) {
        cachedLocation = cached

        // 持久化到 MMKV
        val json = JSONObject().apply {
            put("latitude", cached.latitude)
            put("longitude", cached.longitude)
            put("accuracy", cached.accuracy)
            put("provider", cached.provider)
            put("timestamp", cached.timestamp)
            put("altitude", cached.altitude)
            put("speed", cached.speed)
            put("bearing", cached.bearing)
        }

        MMKV.defaultMMKV().putString(KEY_CACHED_LOCATION, json.toString())
        MMKV.defaultMMKV().putLong(KEY_LAST_UPDATE_TIME, cached.timestamp)
    }

    private fun loadCachedLocation(): CachedLocation? {
        val jsonStr = MMKV.defaultMMKV().getString(KEY_CACHED_LOCATION, null) ?: return null

        return try {
            val json = JSONObject(jsonStr)
            CachedLocation(
                latitude = json.optDouble("latitude", 0.0),
                longitude = json.optDouble("longitude", 0.0),
                accuracy = json.optDouble("accuracy", 1000.0).toFloat(),
                provider = json.optString("provider", "unknown"),
                timestamp = json.optLong("timestamp", 0),
                altitude = json.optDouble("altitude", 0.0),
                speed = json.optDouble("speed", 0.0).toFloat(),
                bearing = json.optDouble("bearing", 0.0).toFloat()
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "解析缓存位置失败: ${e.message}")
            null
        }
    }

    private fun isLocationValid(
        cached: CachedLocation,
        maxAgeMs: Long,
        maxAccuracy: Float
    ): Boolean {
        // 检查时间有效期
        val age = System.currentTimeMillis() - cached.timestamp
        if (age > maxAgeMs) {
            android.util.Log.d(TAG, "缓存位置已过期: ${age / 1000}秒")
            return false
        }

        // 检查精度
        if (cached.accuracy > maxAccuracy) {
            android.util.Log.d(TAG, "缓存位置精度不足: ${cached.accuracy}米")
            return false
        }

        // 检查坐标有效性
        if (cached.latitude == 0.0 && cached.longitude == 0.0) {
            android.util.Log.d(TAG, "缓存位置坐标无效")
            return false
        }

        return true
    }
}
