package com.jwch.gwyt_project.util

import java.io.File
import java.io.RandomAccessFile

class TPKDecryptedStream(private val filePath: String, private val key: Int) {
    private val sourceFile = File(filePath)
    private val fileSize = sourceFile.length()
    private val headerEnd = minOf(1024, fileSize.toInt())
    private val tailStart = maxOf(0, fileSize - 1024).toInt()

    /**
     * 解密文件并返回临时File对象
     * 注意：这会创建一个临时文件，使用后需要手动清理
     */
    fun toFile(): File? {
        return try {
            // 创建临时文件
            val tempFile = createTempFile()

            // 使用NIO高效复制并解密
            RandomAccessFile(sourceFile, "r").use { sourceRaf ->
                RandomAccessFile(tempFile, "rw").use { tempRaf ->
                    val sourceChannel = sourceRaf.channel
                    val tempChannel = tempRaf.channel

                    // 直接传输文件内容（高效）
                    if (fileSize > 0) {
                        sourceChannel.transferTo(0, fileSize, tempChannel)
                    }
                }
            }

            // 对临时文件进行解密操作
            decryptFile(tempFile)

            tempFile
        } catch (e: Exception) {
            println("创建解密文件失败: ${e.message}")
            // 清理可能创建的临时文件
            deleteTempFile()
            null
        }
    }


    /**
     * 解密文件并返回File，使用应用缓存目录
     */
    fun toCacheFile(context: android.content.Context): File? {
        return try {
            val cacheDir = context.cacheDir
            val cacheFile = File(cacheDir, "${sourceFile.name}")

            // 复制源文件到缓存目录
            sourceFile.copyTo(cacheFile, overwrite = true)

            // 解密文件
            decryptFile(cacheFile)

            // 设置应用退出时自动删除
            cacheFile.deleteOnExit()

            cacheFile
        } catch (e: Exception) {
            println("创建缓存文件失败: ${e.message}")
            null
        }
    }

    fun toFile(path: String): File? {
        return try {
            val cacheFile = File(path, "${sourceFile.name}")

            // 复制源文件到缓存目录
            sourceFile.copyTo(cacheFile, overwrite = true)

            // 解密文件
            decryptFile(cacheFile)

            // 设置应用退出时自动删除
            cacheFile.deleteOnExit()

            cacheFile
        } catch (e: Exception) {
            println("创建缓存文件失败: ${e.message}")
            null
        }
    }


    private fun createTempFile(): File {
        val tempDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
        val tempFile = File(tempDir, "tpk_decrypted_${System.currentTimeMillis()}_${sourceFile.name}")
        tempFile.deleteOnExit() // 确保JVM退出时删除
        return tempFile
    }

    private fun decryptFile(file: File) {
        RandomAccessFile(file, "rw").use { raf ->
            val channel = raf.channel
            val size = raf.length()

            // 解密文件头部
            if (headerEnd > 0) {
                val headerBuffer = ByteArray(headerEnd)
                raf.seek(0)
                raf.read(headerBuffer)

                for (i in headerBuffer.indices) {
                    headerBuffer[i] = (headerBuffer[i].toInt() xor key).toByte()
                }

                raf.seek(0)
                raf.write(headerBuffer)
            }

            // 解密文件尾部
            if (tailStart < size && tailStart > headerEnd) {
                val tailSize = (size - tailStart).toInt()
                val tailBuffer = ByteArray(tailSize)

                raf.seek(tailStart.toLong())
                raf.read(tailBuffer)

                for (i in tailBuffer.indices) {
                    tailBuffer[i] = (tailBuffer[i].toInt() xor key).toByte()
                }

                raf.seek(tailStart.toLong())
                raf.write(tailBuffer)
            }
        }
    }

    private fun deleteTempFile() {
        // 如果有需要，可以在这里实现临时文件清理逻辑
    }

    /**
     * 获取文件大小
     */
    fun getSize(): Long = fileSize

    /**
     * 获取源文件名
     */
    fun getSourceFileName(): String = sourceFile.name



    /**
     * 读取文件是否是解密状态（即是否 xml 文件）
     */
    private fun isPlainXml(file: File): Boolean {
        if (!file.exists() || file.length() < 5) return false
        RandomAccessFile(file, "r").use { raf ->
            val header = ByteArray(5)
            raf.read(header)
            val text = String(header)
            return text.startsWith("<?xml")
        }
    }

    /**
     * 对文件进行异或处理（头 + 尾）
     *（加密与解密本质一致）
     */
    private fun xorProcess(file: File) {
        RandomAccessFile(file, "rw").use { raf ->
            val size = raf.length()
            val headerEnd = minOf(1024, size.toInt())
            val tailStart = maxOf(0, size - 1024).toInt()

            // ---------- 头部 ----------
            if (headerEnd > 0) {
                val headerBuffer = ByteArray(headerEnd)
                raf.seek(0)
                raf.read(headerBuffer)
                for (i in headerBuffer.indices) {
                    headerBuffer[i] = (headerBuffer[i].toInt() xor key).toByte()
                }
                raf.seek(0)
                raf.write(headerBuffer)
            }

            // ---------- 尾部 ----------
            if (tailStart > headerEnd && tailStart < size) {
                val tailSize = (size - tailStart).toInt()
                val tailBuffer = ByteArray(tailSize)

                raf.seek(tailStart.toLong())
                raf.read(tailBuffer)
                for (i in tailBuffer.indices) {
                    tailBuffer[i] = (tailBuffer[i].toInt() xor key).toByte()
                }

                raf.seek(tailStart.toLong())
                raf.write(tailBuffer)
            }
        }
    }

    /**
     * 加密文件（原地加密）
     * @param callback (success, message)
     */
    fun encryptFileInPlace(callback: (Boolean, String) -> Unit) {
        try {
            if (!sourceFile.exists()) {
                callback(false, "文件不存在")
                return
            }

            // 如果是 xml，则说明未加密 → 允许加密
            if (isPlainXml(sourceFile)) {
                xorProcess(sourceFile)
                callback(true, "加密完成")
            } else {
                callback(false, "文件已是加密状态，无需重复加密")
            }
        } catch (e: Exception) {
            callback(false, "加密失败: ${e.message}")
        }
    }

    /**
     * 解密文件（原地解密）
     * @param callback (success, message)
     */
    fun decryptFileInPlace(callback: (Boolean, String) -> Unit) {
        try {
            if (!sourceFile.exists()) {
                callback(false, "文件不存在")
                return
            }

            // 如果不是 xml，则说明是加密文件 → 允许解密
            if (!isPlainXml(sourceFile)) {
                xorProcess(sourceFile)
                callback(true, "解密完成")
            } else {
                callback(false, "文件已是解密状态，无需重复解密")
            }
        } catch (e: Exception) {
            callback(false, "解密失败: ${e.message}")
        }
    }


}



