package com.jwch.gwyt_project.util

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * License管理工具类
 * 用于验证软件授权是否过期
 */
object LicenseManager {

    // 密钥（混淆存储，增加逆向难度）
    private const val KEY_PART_1 = "GwYt"
    private const val KEY_PART_2 = "2024"
    private const val KEY_PART_3 = "Lice"
    private const val KEY_PART_4 = "nseK"
    private const val KEY_PART_5 = "ey#!"

    // 获取完整密钥
    private val SECRET_KEY: String
        get() = KEY_PART_1 + KEY_PART_3 + KEY_PART_2 + KEY_PART_5 + KEY_PART_4

    // IV向量
    private const val IV = "GwYtProjectIv202"

    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val AES = "AES"

    /**
     * AES加密
     * @param data 待加密的数据
     * @return Base64编码的加密结果
     */
    fun encrypt(data: String): String {
        return try {
            val keySpec = generateKeySpec()
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            val encrypted = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * AES解密
     * @param encryptedData Base64编码的加密数据
     * @return 解密后的原始数据
     */
    fun decrypt(encryptedData: String): String {
        return try {
            val keySpec = generateKeySpec()
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decrypted = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 生成AES密钥规范
     */
    private fun generateKeySpec(): SecretKeySpec {
        val keyBytes = MessageDigest.getInstance("MD5")
            .digest(SECRET_KEY.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, AES)
    }

    /**
     * 验证License是否有效
     * @param encryptedLicense 加密的license字符串
     * @return LicenseResult 验证结果
     */
    fun verifyLicense(encryptedLicense: String): LicenseResult {
        if (encryptedLicense.isBlank()) {
            return LicenseResult.Error("License为空")
        }

        return try {
            // 解密license
            val expireDateStr = decrypt(encryptedLicense)
            if (expireDateStr.isBlank()) {
                return LicenseResult.Error("License解密失败")
            }

            // 解析日期 (格式: yyyy-MM-dd)
            val expireDate = parseDate(expireDateStr)
                ?: return LicenseResult.Error("日期格式错误，应为yyyy-MM-dd")

            // 获取当前日期（不含时间）
            val currentDate = getCurrentDate()

            // 比较日期
            if (currentDate.isAfter(expireDate)) {
                LicenseResult.Expired(expireDateStr)
            } else {
                LicenseResult.Valid(expireDateStr)
            }
        } catch (e: Exception) {
            LicenseResult.Error("验证失败: ${e.message}")
        }
    }

    /**
     * 解析日期字符串
     */
    private fun parseDate(dateStr: String): java.time.LocalDate? {
        return try {
            java.time.LocalDate.parse(dateStr.trim())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前日期
     */
    private fun getCurrentDate(): java.time.LocalDate {
        return java.time.LocalDate.now()
    }

    /**
     * 验证结果密封类
     */
    sealed class LicenseResult {
        data class Valid(val expireDate: String) : LicenseResult()
        data class Expired(val expireDate: String) : LicenseResult()
        data class Error(val message: String) : LicenseResult()
    }
}
