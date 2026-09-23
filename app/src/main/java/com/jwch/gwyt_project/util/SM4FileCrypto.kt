package com.jwch.gwyt_project.util


import android.content.Context
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * SM4 文件加解密工具类（SM4/CBC/PKCS7Padding）
 *
 *
 * 注意：
 *  - 固定密钥为 16 字节，可自行修改。
 *  - 每次加密仍会随机生成 IV（保证安全性）。
 */
class SM4FileCrypto(
    private val context: Context,
    private val ttlMillis: Long = 24L * 60 * 60 * 1000 // 默认清理 1 天前的文件
) {
    companion object {
        // ===== 固定的 16 字节密钥 =====
        private val FIXED_KEY = "Yx31sWlxsx7d461Z".toByteArray(Charsets.UTF_8)
    }

    init {
        // 注册 BouncyCastle 提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private val secureRandom = SecureRandom()
    private val KEY_ALGO = "SM4"
    private val TRANSFORM = "SM4/CBC/PKCS7Padding"

    /** 加密文件 */
    @Throws(IOException::class)
    fun encrypt(srcPath: String): String {
        cleanUpExpiredTempFiles()

        val srcFile = File(srcPath)
        require(srcFile.exists() && srcFile.isFile) { "Source file not found: $srcPath" }

        val dstFile = createTempFileInCache(prefix = "sm4_enc_", suffix = ".bin")
        FileInputStream(srcFile).use { fis ->
            FileOutputStream(dstFile).use { fos ->
                // 生成随机 IV 并写入文件头
                val iv = ByteArray(16)
                secureRandom.nextBytes(iv)
                fos.write(iv)

                val cipher = Cipher.getInstance(TRANSFORM, "BC")
                val keySpec = SecretKeySpec(FIXED_KEY, KEY_ALGO)
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

                CipherOutputStream(fos, cipher).use { cos ->
                    copyStream(fis, cos)
                    cos.flush()
                }
            }
        }
        return dstFile.absolutePath
    }

    /** 解密文件 */
    @Throws(IOException::class)
    fun decrypt(srcPath: String): String {
        cleanUpExpiredTempFiles()

        val srcFile = File(srcPath)
        require(srcFile.exists() && srcFile.isFile) { "Source file not found: $srcPath" }

        val dstFile = createTempFileInCache(prefix = "sm4_dec_", suffix = ".dat")
        FileInputStream(srcFile).use { fis ->
            // 读取前 16 字节 IV
            val iv = ByteArray(16)
            val read = fis.read(iv)
            require(read == 16) { "Invalid encrypted file: missing IV" }

            val cipher = Cipher.getInstance(TRANSFORM, "BC")
            val keySpec = SecretKeySpec(FIXED_KEY, KEY_ALGO)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            CipherInputStream(fis, cipher).use { cis ->
                FileOutputStream(dstFile).use { fos ->
                    copyStream(cis, fos)
                    fos.flush()
                }
            }
        }
        return dstFile.absolutePath
    }

    /** 手动清理 */
    fun cleanUpNow() {
        cleanUpExpiredTempFiles()
    }

    // ---------------- 内部方法 ----------------

    private fun createTempFileInCache(prefix: String, suffix: String): File {
        val cache = context.cacheDir
        if (!cache.exists()) cache.mkdirs()
        val tempFile = File.createTempFile(prefix, suffix, cache)
        try {
            tempFile.setReadable(true, true)
            tempFile.setWritable(true, true)
        } catch (_: Throwable) { }
        return tempFile
    }

    private fun cleanUpExpiredTempFiles() {
        val now = System.currentTimeMillis()
        val cache = context.cacheDir
        val files = cache.listFiles() ?: return
        for (f in files) {
            if ((f.name.startsWith("sm4_enc_") || f.name.startsWith("sm4_dec_")) && f.isFile) {
                val age = now - f.lastModified()
                if (age > ttlMillis) f.delete()
            }
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
    }
}
