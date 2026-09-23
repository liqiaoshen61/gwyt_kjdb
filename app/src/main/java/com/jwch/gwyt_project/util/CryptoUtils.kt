package com.jwch.gwyt_project.util

import android.util.Log
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import java.io.File

//解密方式
object CryptoUtils {


    // 解密gdb方法
    fun decryptGeodatabase(inputFilePath: String): String {
        val file = File(inputFilePath)
        if (!file.exists()) {
            Log.e("TAG", "输入文件不存在")
            return inputFilePath
        }

        val stream = TPKDecryptedStream(inputFilePath, 0x55)
        return stream.toFile()?.absolutePath.self()


////        // 初行政区划外的gdb 均需要解密
//        if (fileName != "省级" && fileName != "市级" && fileName != "区县" && fileName != "乡镇") {
//            val stream = TPKDecryptedStream(inputFilePath, 0x55)
//            return stream.toFile()?.absolutePath.self()
//        } else {
//            return inputFilePath
//        }
//        return inputFilePath

    }


    // 解密tpk方法
    fun decryptTpk(inputFilePath: String): String {
        val file = File(inputFilePath)
        if (!file.exists()) {
            Log.e("TAG", "输入文件不存在")
            return inputFilePath
        }
        val fileName = file.nameWithoutExtension

//        // 只有河道vtpk需要解密
        if (fileName.contains("河道") || fileName.contains("河管线")|| fileName.contains("河道范围管理线") ) {
            val stream = TPKDecryptedStream(inputFilePath, 0x55)
            return stream.toFile()?.absolutePath.self()
        } else {
            return inputFilePath
        }

    }


}