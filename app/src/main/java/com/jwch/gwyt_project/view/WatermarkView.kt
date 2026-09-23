package com.jwch.gwyt_project.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.jameni.allutillib.common.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局水印视图
 * 用于在屏幕上显示斜向平铺的水印文字，防止截图/拍照泄露信息
 *
 * 水印内容包括：
 * - 自定义文字（最多10个字）
 * - 当前日期（yyyy-MM-dd格式）
 */
class WatermarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 水印文字相关配置
    private var customText: String = ""  // 自定义水印文字（最多10个字）
    private var userInfo: String = ""    // 用户信息（已废弃）

    // 动态配置
    private var textSize: Float = 42f  // 文字大小
    private var textAlpha: Int = 128   // 文字透明度 (0-255)
    private var textAngle: Float = -30f  // 文字倾斜角度（度）

    // 绘制相关
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)  // 描边画笔
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)    // 填充画笔
    private var watermarkBitmap: Bitmap? = null
    private val matrix = Matrix()

    // 水印配置
    companion object {
        const val SPACING_HORIZONTAL = 250  // 水印水平间距
        const val SPACING_VERTICAL = 300  // 水印垂直间距
        const val MAX_CUSTOM_TEXT_LENGTH = 10  // 自定义文字最大长度

        private const val TAG = "WatermarkView"
    }

    init {
        // 设置描边画笔（黑色边框）
        strokePaint.apply {
            color = Color.BLACK  // 黑色描边
            textSize = this@WatermarkView.textSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE  // 描边模式
            strokeWidth = 3f  // 描边宽度
            strokeJoin = Paint.Join.ROUND  // 圆角连接
            strokeCap = Paint.Cap.ROUND  // 圆角端点
        }

        // 设置填充画笔（白色填充）
        fillPaint.apply {
            color = Color.WHITE  // 白色填充
            textSize = this@WatermarkView.textSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL  // 填充模式
        }
    }

    /**
     * 设置水印配置
     * @param customText 自定义文字（最多10个字，超出会被截断）
     * @param textSize 文字大小 (sp)
     * @param alpha 透明度 (0-100)
     * @param angle 倾斜角度 (-90到90度)
     */
    fun setConfig(customText: String, textSize: Int, alpha: Int, angle: Int) {
        // 限制自定义文字长度
        this.customText = if (customText.length > MAX_CUSTOM_TEXT_LENGTH) {
            customText.substring(0, MAX_CUSTOM_TEXT_LENGTH)
        } else {
            customText
        }

        // 设置文字大小
        this.textSize = textSize.toFloat()

        // 设置透明度 (将0-100映射到0-255)
        this.textAlpha = (alpha * 255 / 100)

        // 设置倾斜角度
        this.textAngle = angle.toFloat()

        // 更新描边画笔
        strokePaint.apply {
            this.textSize = this@WatermarkView.textSize
            // 描边使用半透明黑色
            color = Color.argb(this@WatermarkView.textAlpha, 0, 0, 0)
        }

        // 更新填充画笔
        fillPaint.apply {
            this.textSize = this@WatermarkView.textSize
            // 填充使用半透明白色
            color = Color.argb(this@WatermarkView.textAlpha, 255, 255, 255)
        }

        Log.d(TAG, "设置水印配置: customText=$this.customText, textSize=$textSize, alpha=$alpha, angle=$angle")

        // 重新生成水印位图
        createWatermarkBitmap()
        invalidate()
    }

    /**
     * 设置水印文字
     * @param customText 自定义文字（最多10个字，超出会被截断）
     * @param userInfo 用户信息（已废弃，不再使用）
     */
    fun setWatermarkText(customText: String, userInfo: String) {
        // 限制自定义文字长度
        this.customText = if (customText.length > MAX_CUSTOM_TEXT_LENGTH) {
            customText.substring(0, MAX_CUSTOM_TEXT_LENGTH)
        } else {
            customText
        }
        // 不再使用userInfo，但保留参数以兼容现有调用

        Log.d(TAG, "设置水印文字: customText=$this.customText")

        // 重新生成水印位图
        createWatermarkBitmap()
        invalidate()
    }

    /**
     * 创建水印位图（优化性能，避免每次绘制都重新生成）
     */
    private fun createWatermarkBitmap() {
        // 回收旧位图
        watermarkBitmap?.recycle()

        // 获取屏幕尺寸
        val screenWidth = width
        val screenHeight = height

        Log.d(TAG, "创建水印位图: screenWidth=$screenWidth, screenHeight=$screenHeight")

        if (screenWidth <= 0 || screenHeight <= 0) {
            Log.w(TAG, "屏幕尺寸无效，跳过水印创建")
            return
        }

        // 构建水印文字列表
        val watermarkLines = buildWatermarkLines()
        Log.d(TAG, "水印文字行: $watermarkLines")

        // 创建单个水印单元的位图
        val unitBitmap = createWatermarkUnit(watermarkLines)

        // 创建完整的平铺水印位图
        watermarkBitmap = createTiledWatermark(unitBitmap, screenWidth, screenHeight)

        Log.d(TAG, "水印位图创建完成: ${watermarkBitmap != null}")

        unitBitmap.recycle()
    }

    /**
     * 构建水印文字行列表
     */
    private fun buildWatermarkLines(): List<String> {
        val lines = mutableListOf<String>()

        // 第一行：自定义文字
        if (customText.isNotEmpty()) {
            lines.add(customText)
        }

        // 第二行：当前日期
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        lines.add(currentDate)

        return lines
    }

    /**
     * 创建单个水印单元
     */
    private fun createWatermarkUnit(lines: List<String>): Bitmap {
        // 测量文字宽度和高度
        var maxWidth = 0f
        val lineHeight = textSize * 1.5f
        val totalHeight = lineHeight * lines.size

        for (line in lines) {
            val width = fillPaint.measureText(line)
            if (width > maxWidth) maxWidth = width
        }

        // 留出边距和旋转空间
        val padding = 50f
        val unitWidth = (maxWidth + padding * 2).toInt()
        val unitHeight = (totalHeight + padding * 2).toInt()

        // 创建位图
        val bitmap = Bitmap.createBitmap(unitWidth, unitHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 绘制文字（先描边后填充）
        var currentY = padding + textSize
        for (line in lines) {
            // 先绘制黑色描边
            canvas.drawText(line, padding, currentY, strokePaint)
            // 再绘制白色填充
            canvas.drawText(line, padding, currentY, fillPaint)
            currentY += lineHeight
        }

        return bitmap
    }

    /**
     * 创建平铺的水印位图
     */
    private fun createTiledWatermark(unitBitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
        // 创建足够大的位图覆盖整个屏幕（考虑旋转后的范围）
        val diagonal = Math.sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble()).toInt()
        val tiledBitmap = Bitmap.createBitmap(diagonal, diagonal, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(tiledBitmap)

        // 设置旋转矩阵
        matrix.reset()
        matrix.postRotate(textAngle, unitBitmap.width / 2f, unitBitmap.height / 2f)

        // 平铺水印
        var y = 0
        while (y < diagonal) {
            var x = 0
            while (x < diagonal) {
                canvas.save()
                canvas.translate(x.toFloat(), y.toFloat())
                canvas.drawBitmap(unitBitmap, matrix, fillPaint)
                canvas.restore()
                x += unitBitmap.width + SPACING_HORIZONTAL
            }
            y += unitBitmap.height + SPACING_VERTICAL
        }

        return tiledBitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 如果水印位图存在，绘制到画布中心
        watermarkBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                // 计算绘制位置（居中）
                val left = (width - bitmap.width) / 2f
                val top = (height - bitmap.height) / 2f
                canvas.drawBitmap(bitmap, left, top, fillPaint)
                Log.d(TAG, "绘制水印: left=$left, top=$top, bitmap=${bitmap.width}x${bitmap.height}")
            } else {
                Log.w(TAG, "水印位图已回收")
            }
        } ?: Log.w(TAG, "水印位图为空")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "视图尺寸改变: w=$w, h=$h, oldw=$oldw, oldh=$oldh")
        // 尺寸改变时重新生成水印位图
        if (w > 0 && h > 0) {
            createWatermarkBitmap()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "水印视图已附加到窗口")
    }

    /**
     * 更新时间（可定时调用以更新水印中的时间）
     */
    fun updateTime() {
        createWatermarkBitmap()
        invalidate()
    }

    /**
     * 清理资源
     */
    fun destroy() {
        watermarkBitmap?.recycle()
        watermarkBitmap = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }
}
