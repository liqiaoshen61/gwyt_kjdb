package com.jwch.gwyt_project.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoHelper private constructor(private val context: Context) {

    companion object {
        private const val KEY_ALIAS = "offline_map_key"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // GCM推荐12字节IV

        @Volatile
        private var instance: CryptoHelper? = null

        fun getInstance(context: Context): CryptoHelper {
            return instance ?: synchronized(this) {
                instance ?: CryptoHelper(context.applicationContext).also { instance = it }
            }
        }
    }


    // 密钥管理（AndroidKeyStore保护）
    private val secretKey: SecretKey by lazy {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)
            ?: throw IllegalStateException("密钥生成失败")
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // 可根据需求开启生物验证
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
    }

    // 加密文件到私有目录
    // 加密文件到原始文件所在目录
    fun encryptFile(originalFile: File): File {
        // 验证原始文件存在性
        require(originalFile.exists()) { "原始文件不存在" }

        // 生成加密文件路径（同目录 + encrypted_前缀）
        val parentDir = originalFile.parentFile
        val encryptedFile = File(parentDir, "encrypted_${originalFile.name}")

        // 删除已存在的加密文件
        if (encryptedFile.exists()) encryptedFile.delete()

        val cipher = Cipher.getInstance(AES_MODE).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }

        FileOutputStream(encryptedFile).use { fos ->
            fos.write(cipher.iv)
            CipherOutputStream(fos, cipher).use { cos ->
                FileInputStream(originalFile).use { it.copyTo(cos) }
            }
        }
        return encryptedFile
    }

    // 解密到内存（不落盘）
    fun decryptToMemory(encryptedFile: File): ByteArray {
        require(encryptedFile.exists()) { "加密文件不存在" }

        return FileInputStream(encryptedFile).use { fis ->
            val iv = ByteArray(IV_LENGTH).also { fis.read(it) }
            val cipher = Cipher.getInstance(AES_MODE).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            }
            CipherInputStream(fis, cipher).use { it.readBytes() }
        }
    }

    // 解密到临时文件（可选）
    fun decryptToTempFile(encryptedFile: File): File {
        val tempFile = File.createTempFile("decrypted", ".tmp", context.cacheDir)
        decryptToStream(encryptedFile, FileOutputStream(tempFile))
        return tempFile
    }

    // 通用流解密
    private fun decryptToStream(encryptedFile: File, outputStream: OutputStream) {
        FileInputStream(encryptedFile).use { fis ->
            val iv = ByteArray(IV_LENGTH).also { fis.read(it) }
            val cipher = Cipher.getInstance(AES_MODE).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            }
            CipherInputStream(fis, cipher).use { input ->
                input.copyTo(outputStream)
            }
        }
    }

    //判断是否为加密数据
    fun isEncrypted(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false

        //验证文件头部是否包含合法的IV（GCM IV应为12字节）
        return try {
            FileInputStream(file).use { fis ->
                val iv = ByteArray(IV_LENGTH)
                fis.read(iv)
                iv.size == IV_LENGTH // 验证IV长度是否正确
            }
        } catch (e: Exception) {
            false
        }
    }
}