package com.jwch.gwyt_project.util


import com.jwch.gwyt_project.ext.self
import java.security.MessageDigest

class MD5E {
    companion object {
        var instances = MD5E()
    }


    fun encrypt(data: String): String {

        if (data.isNullOrBlank()) {
            return ""
        }

        var b = encryptMD5(data.toByteArray())
        return byte2hex(b).self()
    }


    //加密
    private fun encryptMD5(data: ByteArray): ByteArray {
        var md5 = MessageDigest.getInstance("MD5")
        md5.update(data)
        return md5.digest()
    }

    /**
     * 二进制转十六进制
     *
     * @param b
     * @return
     */
    private fun byte2hex(b: ByteArray): String? {
        var hs = ""
        var stmp = ""
        for (n in b.indices) {
            val v = b[n].toInt() and 0XFF
            stmp = Integer.toHexString(v)
            hs = if (stmp.length == 1) {
                hs + "0" + stmp
            } else {
                hs + stmp
            }
        }
        return hs
    }
}