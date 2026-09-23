package com.jwch.gwyt_project.ext

import com.tencent.mmkv.MMKV


inline fun saveKV(key: String, data: Any) {
//    val kv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE,null)//参数，多线程模式，任意字符串秘钥
    val kv = MMKV.defaultMMKV()

    when (data) {
        is String -> {
            kv.encode(key, data)
        }
        is Int -> {
            kv.encode(key, data)
        }
        is Long -> {
            kv.encode(key, data)
        }
        is Float -> {
            kv.encode(key, data)
        }
        is Double -> {
            kv.encode(key, data)
        }
        is Boolean -> {
            kv.encode(key, data)
        }
        is ByteArray -> {
            kv.encode(key, data)
        }
        else -> throw IllegalArgumentException("Type Error")
    }
}

inline fun <reified T> getKV(key: String, defValue: T): T {
    val kv = MMKV.defaultMMKV()

    return when (defValue) {
        is String -> kv.decodeString(key, defValue) as T
        is Int -> kv.decodeInt(key, defValue) as T
        is Long -> kv.decodeLong(key, defValue) as T
        is Float -> kv.decodeFloat(key, defValue) as T
        is Double -> kv.decodeDouble(key, defValue) as T
        is Boolean -> kv.decodeBool(key, defValue) as T
        is ByteArray -> kv.decodeBytes(key, defValue) as T
        else -> throw IllegalArgumentException("Type Error")
    }
}

inline fun <reified T> getKV(key: String): T {
    val kv = MMKV.defaultMMKV()
    val defValue = T::class.java
    return when (defValue.name) {
        "java.lang.String" -> kv.decodeString(key, "") as T
        "java.lang.Integer" -> kv.decodeInt(key, 0) as T
        "java.lang.Long" -> kv.decodeLong(key, 0) as T
        "java.lang.Float" -> kv.decodeFloat(key, 0f) as T
        "java.lang.Double" -> kv.decodeDouble(key, 0.0) as T
        "java.lang.Boolean" -> kv.decodeBool(key, false) as T
        "[B" -> kv.decodeBytes(key, ByteArray(0)) as T
        else -> throw IllegalArgumentException("Type Error")
    }
}
inline fun clearAllKv(){
    val kv = MMKV.defaultMMKV()
    kv.clearAll()

}

