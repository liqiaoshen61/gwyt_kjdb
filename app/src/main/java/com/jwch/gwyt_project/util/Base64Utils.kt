package com.jwch.gwyt_project.util

import android.util.Base64


object Base64Utils {

    /**
     * 将字符串编码为Base64字符串
     * @param input 要编码的原始字符串
     * @param flags Base64编码标志，默认为Base64.DEFAULT
     * @return Base64编码后的字符串
     */
    @JvmStatic
    fun encode(input: String, flags: Int = Base64.DEFAULT): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, flags)
    }

    /**
     * 将Base64字符串解码为原始字符串
     * @param input Base64编码的字符串
     * @param flags Base64解码标志，默认为Base64.DEFAULT
     * @return 解码后的原始字符串
     * @throws IllegalArgumentException 如果输入不是有效的Base64字符串
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun decode(input: String, flags: Int = Base64.DEFAULT): String {
        val bytes = Base64.decode(input, flags)
        return String(bytes, Charsets.UTF_8)
    }

    /**
     * 将字节数组编码为Base64字符串
     * @param bytes 要编码的字节数组
     * @param flags Base64编码标志，默认为Base64.DEFAULT
     * @return Base64编码后的字符串
     */
    @JvmStatic
    fun encode(bytes: ByteArray, flags: Int = Base64.DEFAULT): String {
        return Base64.encodeToString(bytes, flags)
    }

    /**
     * 将Base64字符串解码为字节数组
     * @param input Base64编码的字符串
     * @param flags Base64解码标志，默认为Base64.DEFAULT
     * @return 解码后的字节数组
     * @throws IllegalArgumentException 如果输入不是有效的Base64字符串
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun decodeToBytes(input: String, flags: Int = Base64.DEFAULT): ByteArray {
        return Base64.decode(input, flags)
    }

    /**
     * 检查字符串是否为有效的Base64编码
     * @param input 要检查的字符串
     * @return 如果是有效的Base64编码则返回true，否则返回false
     */
    @JvmStatic
    fun isBase64(input: String): Boolean {
//        if (input.isEmpty() || input.length % 4 != 0) {
//            return false
//        }

        // 检查是否为纯数字
        if (input.matches("^[0-9]+$".toRegex())) {
            return false
        }

        // 检查是否只包含Base64字符（A-Za-z0-9+/=）
        val base64Pattern = "^[A-Za-z0-9+/]*={0,2}$"
        if (!input.matches(base64Pattern.toRegex())) {
            return false
        }

        return  true
//        // 尝试解码并重新编码，验证一致性
//        return try {
//            val decodedBytes = Base64.decode(input, Base64.DEFAULT)
//            val reencoded = Base64.encodeToString(decodedBytes, Base64.DEFAULT)
//            reencoded == input
//        } catch (e: IllegalArgumentException) {
//            false
//        }

    }


}

