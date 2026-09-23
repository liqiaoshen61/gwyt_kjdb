package com.jwch.gwyt_project.util

import android.content.Context
import android.net.ConnectivityManager

class NetUtil {


    companion object{

        fun isNetworkConnected(context:Context?):Boolean{
            if (context != null) {

                val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val info = manager.activeNetworkInfo
                if (info != null) {
                    return info.isAvailable
                }
            }
            return false
        }
    }
}