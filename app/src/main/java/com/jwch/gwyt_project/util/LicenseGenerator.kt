package com.jwch.gwyt_project.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * License生成工具
 * 用于生成加密的license字符串
 *
 * 使用方法：
 * 1. 直接运行main方法
 * 2. 或在其他代码中调用 generateLicense(expireDate) 方法
 *
 * 示例：
 * LicenseGenerator.generateLicense("2026-12-31")
 *
 * 将生成的加密字符串填入 region_info.txt 的 license 字段：
 * {"region":"龙岩市","license":"生成的加密字符串"}
 */
object LicenseGenerator {

    // 密钥（与LicenseManager保持一致）
    private const val KEY_PART_1 = "GwYt"
    private const val KEY_PART_2 = "2024"
    private const val KEY_PART_3 = "Lice"
    private const val KEY_PART_4 = "nseK"
    private const val KEY_PART_5 = "ey#!"

    private val SECRET_KEY: String
        get() = KEY_PART_1 + KEY_PART_3 + KEY_PART_2 + KEY_PART_5 + KEY_PART_4

    private const val IV = "GwYtProjectIv202"
    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val AES = "AES"

    /**
     * 生成加密的License
     * @param expireDate 过期日期，格式：yyyy-MM-dd，如 "2026-12-31"
     * @return 加密后的Base64字符串
     */
    fun generateLicense(expireDate: String): String {
        val keySpec = generateKeySpec()
        val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val encrypted = cipher.doFinal(expireDate.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    /**
     * 解密License（用于验证）
     */
    fun decryptLicense(encryptedLicense: String): String {
        val keySpec = generateKeySpec()
        val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedLicense))
        return String(decrypted, StandardCharsets.UTF_8)
    }

    private fun generateKeySpec(): SecretKeySpec {
        val keyBytes = MessageDigest.getInstance("MD5")
            .digest(SECRET_KEY.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, AES)
    }

    /**
     * 生成完整的region_info.txt内容
     */
    fun generateRegionInfoJson(region: String, expireDate: String): String {
        val license = generateLicense(expireDate)
        return """{"region":"$region","license":"$license"}"""
    }

    @JvmStatic
    fun main() {
        println("========== License 生成工具 ==========")
        println()

        // 示例：生成多个过期日期的license
        val examples = listOf(
//            "漳州市" to "2028-11-10",
//            "明溪县" to "2027-01-17",
//            "永春县" to "2026-09-20",
//            "龙岩市" to "2026-11-30",
//            "长汀县" to "2028-12-31",
//            "漳平市" to "2027-01-22",
//            "福州市" to "2026-06-01",
//            "泰宁县" to "2027-08-01",
            "福建省" to "2027-05-31",
        )

        examples.forEach { (region, expireDate) ->
            val license = generateLicense(expireDate)
            println("加密License: $license")
            println("验证解密: ${decryptLicense(license)}")
            println("完整JSON: ${generateRegionInfoJson(region, expireDate)}")
            println("----------------------------------------")
        }

    }
}
