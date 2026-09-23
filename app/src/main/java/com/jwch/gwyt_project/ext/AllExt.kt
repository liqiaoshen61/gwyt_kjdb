package com.jwch.gwyt_project.ext

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.PrintUtil
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.view.EditLine
import com.jwch.gwyt_project.view.GridAddImage
import com.jwch.gwyt_project.view.SelectionLine
import com.jwch.gwyt_project.view.TextLine


//扩展函数

fun String?.self(): String {
    return this ?: ""
}

fun String?.selfTemp0(): String {
    return this ?: "0"
}

fun String?.selfTempString(str : String): String {
    return this ?: str
}

fun String?.selfTempNo(str: String = "暂无"): String {

    if (this == null || this.isEmpty()) {
        return str
    } else {
        return this
    }
}

fun Int?.self(): Int {
    return this ?: 0
}
fun Float?.self(): Float {
    return this ?: 0f
}

fun Long?.self(): Long {
    return this ?: 0
}

fun Any?.isEmpty(): Boolean {

    this?.let {
        when (this) {
            is EditText -> return this.text.toString().isBlank()
            is TextView -> return this.text.toString().isBlank()
            is EditLine -> return this.getValueText().isBlank()
            is TextLine -> return this.getValueText().isBlank()
            is SelectionLine -> return this.getSelectValue().self().isBlank()
            is String -> return this.self().isBlank()
            else -> {
            }
        }
    }
    return true
}

fun Any?.isNotEmpty(): Boolean = !this.isEmpty()
fun Any?.checkEmpty(): String? = checkEmpty(null)

fun Any?.checkEmpty(tipMsg: String?): String? {

    var result: String? = null

    this?.let {

        when (this) {
            is EditText -> {
                if (this.text.toString().isBlank()) {
                    Toast.makeText(
                            AppContext.app, tipMsg ?: this.hint.toString(), Toast.LENGTH_SHORT
                    ).show()
                    return result
                } else {
                    return this.value()
                }
            }
            is TextView -> {
                if (this.text.toString().isBlank()) {
                    Toast.makeText(
                            AppContext.app, tipMsg ?: this.hint.toString(), Toast.LENGTH_SHORT
                    ).show()
                    return result
                } else {
                    return this.value()
                }
            }
            is EditLine -> {
                if (this.isEmpty()) {
                    Toast.makeText(AppContext.app, tipMsg ?: this.hint.self(), Toast.LENGTH_SHORT).show()
                    return result
                } else {
                    return this.value()
                }
            }


            is TextLine -> {
                if (this.isEmpty()) {
                    Toast.makeText(AppContext.app, tipMsg ?: this.hint.self(), Toast.LENGTH_SHORT).show()
                    return result
                } else {
                    return this.value()
                }
            }

            is SelectionLine -> {
                if (this.isEmpty()) {
                    Toast.makeText(AppContext.app, tipMsg ?: this.hint.self(), Toast.LENGTH_SHORT).show()
                    return result
                } else {
                    return this.value()
                }
            }

            is GridAddImage -> {
                if (this.isEmpty()) {
                    Toast.makeText(AppContext.app, tipMsg ?: "请选择图片", Toast.LENGTH_SHORT).show()
                    return result
                } else {
                    return this.value()
                }
            }


            is String -> {
                if (this.self() == "") {
                    Toast.makeText(AppContext.app, tipMsg ?: "空字符串", Toast.LENGTH_SHORT).show()
                    return result
                } else {
                    return this
                }
            }
            else -> {
            }
        }
    }
    return result

}


fun Any.value(): String {
    when (this) {
        is TextView -> return this.text.toString()
        is EditText -> return this.text.toString()
        is TextLine -> return this.getValueText()
        is EditLine -> return this.getValueText()
        is SelectionLine -> return this.getSelectValue().self()
        else -> return ""
    }
}

//类型转化方法
inline fun <reified T> Any?.cast(): T? = if (this is T) this else null

inline fun <reified T> Any?.cast(block: (params: T) -> Unit) {

    this?.let {
        if (it is T) {
            block(this as T)
        }
    }

}


fun findColor(context: Context, @ColorRes resId: Int): Int = ContextCompat.getColor(context, resId)
fun findDrawable(context: Context, @DrawableRes resId: Int): Drawable? = ContextCompat.getDrawable(context, resId)

inline fun isNull(obj: Any?, block: () -> Unit) {
    if (obj == null) block()
}

inline fun isNotNullObj(obj: Any?, block: () -> Unit) {
    if (obj != null) block()
}

inline fun isNullObj(obj: Any?, block: () -> Unit) {
    if (null == obj) block()
}

inline fun String?.printMsg() {
    PrintUtil.printMsg(this.self())
}

inline fun String?.printError() {
    Log.e("printInfo", this.self())
}


inline fun String?.cutString(flag: String = ","): MutableList<String> {

    val list = mutableListOf<String>()
    val str = this.self()
    if (!str.trim().isBlank()) {
        if (str.contains(flag)) {
            val array = str.split(flag)
            array.forEach { list.add(it) }
        } else {
            list.add(str)
        }
    }
    return list


}


inline fun getImageUrl(url: String?): String {

    if (url != null) {
        if (url.startsWith("http")) {
            return url
        } else {
            return "${Config.requestUrl}/${url.self()}"
        }
    }
    return ""

}

inline fun String.tip() {
//    Toast.makeText(AppContext.app, this, Toast.LENGTH_SHORT).show()
    ToastUtils.show(this)
}
