package com.jwch.gwyt_project.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object a {
    var gson: Gson = Gson()
}

//扩展函数
fun Any?.toJson(): String {
    return a.gson.toJson(this)
}


inline fun <reified T : Any> getObjFromJson(data: String): T {
    return a.gson.fromJson(data, T::class.java)
}

fun <T> getObjFromJson2(data: String, cls: Class<T>): T {
    return a.gson.fromJson(data, cls)
}


fun <T> getObjByType(data: String, type: java.lang.reflect.Type): T {
    return a.gson.fromJson(data, type)
}


inline fun <reified T> getListFromJson(data: String): MutableList<T> {
    return a.gson.fromJson<MutableList<T>>(data, object : TypeToken<List<T>>() {}.type)
}


// 为Java调用者提供的非内联版本
fun <T> getListFromJson(data: String, clazz: Class<T>): MutableList<T> {
    // 这里我们使用TypeToken来获取List<T>的类型
    val type = TypeToken.getParameterized(MutableList::class.java, clazz).type
    return a.gson.fromJson(data, type)
}

inline fun <reified T> getmapFromJson(data: String): MutableMap<String, T> {
    return a.gson.fromJson(data, object : TypeToken<Map<String, T>>() {}.type)
}

