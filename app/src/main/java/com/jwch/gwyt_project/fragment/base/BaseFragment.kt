package com.jwch.gwyt_project.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.jameni.basepage_lib.basefragment.FinalFragment
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.core.AppContext
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseFragment<T : ViewBinding> : FinalFragment(){

    lateinit var vb: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            val superclass: Type = javaClass.genericSuperclass
            //获得父类的泛型参数的实际类型
            val aClass =
                (superclass as ParameterizedType).getActualTypeArguments().get(0) as Class<*>
            //获取inflate方法 传入相应的参数
            val method: Method = aClass.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.javaPrimitiveType
            )
            //执行inflate方法
            vb = method.invoke(null, layoutInflater, container, false) as T
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return vb.getRoot()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initPageData(null)
        initViewListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    open protected abstract fun initView()

    open protected fun initViewListener() {}
    open protected fun initPageData(data: Any?) {}

    open protected fun <T> getFragmentParams(key: String): T {
        var bundle: Bundle? = arguments
        bundle?.let {
            if (it.containsKey(key)) return it.get(key) as T
        }
        return null as T
    }


    protected fun addPageDatas(key: String, data: Any) = AppContext.map.put(key.self(), data)
    protected fun getPageDatas(key: String): Any? = AppContext.map[key]
    protected fun clearPageDatas() = AppContext.map.clear()


    protected fun getArgsInt(args: Bundle?, key: String): Int {
        if (args == null) return 0
        return args.getInt(key, 0)
    }

    protected fun getArgsString(args: Bundle?, key: String): String {
        if (args == null) return ""
        return args.getString(key, "")
    }

    protected fun getArgsBool(args: Bundle?, key: String): Boolean {
        if (args == null) return false
        return args.getBoolean(key, false)
    }
    protected fun isBackSuccess(resultCode: Int) = resultCode == Config.RESULT_CODE



}