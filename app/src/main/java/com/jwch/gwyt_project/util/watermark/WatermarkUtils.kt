package  com.jwch.gwyt_project.util.watermark

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.ExifInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.io.File
import java.io.FileOutputStream

object WatermarkUtils {

    /**
     * 添加水印核心方法
     * @param context Context
     * @param originalPath 原始图片路径
     * @param watermarkLayoutId 水印布局ID
     * @param watermarkConfig 水印配置回调
     * @return 生成的水印图片路径（失败返回null）
     */
    fun addWatermark(
        context: Context,
        originalPath: String,
        watermarkLayoutId: Int,
        watermarkConfig: (View) -> Unit
    ): String? {
        // 处理原始图片
        val originalBitmap = decodeAndRotateBitmap(originalPath) ?: return null
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true) ?: return null

        try {
            // 创建水印视图
            val watermarkView = createWatermarkView(
                context,
                watermarkLayoutId,
                originalBitmap.width,
                originalBitmap.height,
                watermarkConfig
            )

            // 将水印视图转换为Bitmap
            val watermarkBitmap = viewToBitmap(watermarkView)

            // 合并图片
            val canvas = Canvas(mutableBitmap)
            val position = calculatePosition(
                originalBitmap.width,
                originalBitmap.height,
                watermarkBitmap.width,
                watermarkBitmap.height
            )
            canvas.drawBitmap(watermarkBitmap, position.x.toFloat(), position.y.toFloat(), null)

            // 保存文件
            return saveWatermarkedImage(originalPath, mutableBitmap)
        } finally {
            originalBitmap.recycle()
            mutableBitmap.recycle()
        }
    }

    private fun createWatermarkView(
        context: Context,
        layoutId: Int,
        maxWidth: Int,
        maxHeight: Int,
        config: (View) -> Unit
    ): View {
        return LayoutInflater.from(context).inflate(layoutId, null).apply {
            // 动态设置最大宽度
            val maxWatermarkWidth = (maxWidth * 0.4).toInt()

            measure(
                View.MeasureSpec.makeMeasureSpec(maxWatermarkWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
            )

            // 二次测量确保内容适配
            if (measuredWidth > maxWatermarkWidth) {
                measure(
                    View.MeasureSpec.makeMeasureSpec(maxWatermarkWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY)
                )
            }

            config.invoke(this)
        }
    }

    private fun calculatePosition(
        originalWidth: Int,
        originalHeight: Int,
        watermarkWidth: Int,
        watermarkHeight: Int
    ): Point {
        val margin = 16.dpToPx()
        return Point(
            (originalWidth - watermarkWidth - margin).coerceAtLeast(margin),
            (originalHeight - watermarkHeight - margin).coerceAtLeast(margin)
        )
    }

    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }


    private fun saveWatermarkedImage(originalPath: String, bitmap: Bitmap): String? {
        val originalFile = File(originalPath)
        val outputFile = File(
            originalFile.parent,
            "${originalFile.name}"
        )

        return try {
            FileOutputStream(outputFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeAndRotateBitmap(imagePath: String): Bitmap? {
        // 读取图片EXIF信息处理旋转
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        // 根据方向计算旋转角度
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        // 加载原始图片
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return null

        // 处理旋转
        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            ).also {
                bitmap.recycle()
            }
        } else {
            bitmap
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    data class Point(val x: Int, val y: Int)
}