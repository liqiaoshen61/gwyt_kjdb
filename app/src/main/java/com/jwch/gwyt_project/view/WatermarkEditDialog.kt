package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.SeekBar
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.DialogWatermarkEditBinding
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.Keys
import org.greenrobot.eventbus.EventBus

/**
 * 水印编辑对话框
 */
class WatermarkEditDialog(context: Context) : JameniBaseDialog(context) {

    companion object {
        private const val TAG = "WatermarkEditDialog"

        // 默认值
        const val DEFAULT_TEXT_SIZE = 42  // 默认文字大小
        const val DEFAULT_ALPHA = 60      // 默认透明度 (0-100)
        const val DEFAULT_ANGLE = -30     // 默认倾斜角度 (度)

        // 范围
        const val MIN_TEXT_SIZE = 24
        const val MAX_TEXT_SIZE = 60
        const val MIN_ANGLE = -90
        const val MAX_ANGLE = 90
    }

    private var vb: DialogWatermarkEditBinding? = null

    // 当前配置
    private var currentText: String = ""
    private var currentSize: Int = DEFAULT_TEXT_SIZE
    private var currentAlpha: Int = DEFAULT_ALPHA
    private var currentAngle: Int = DEFAULT_ANGLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = layoutInflater.inflate(R.layout.dialog_watermark_edit, null)
        vb = DialogWatermarkEditBinding.bind(view)
        setContentView(view)

        setCanceledOnTouchOutside(false)

        initConfig()
        initView()
        setOnClick()
    }

    /**
     * 初始化配置（读取本地保存的值）
     */
    private fun initConfig() {
        currentText = getKV(Keys.WATERMARK_CUSTOM_TEXT, "")
        currentSize = getKV(Keys.WATERMARK_TEXT_SIZE, DEFAULT_TEXT_SIZE)
        currentAlpha = getKV(Keys.WATERMARK_ALPHA, DEFAULT_ALPHA)
        currentAngle = getKV(Keys.WATERMARK_ANGLE, DEFAULT_ANGLE)

        Log.d(TAG, "初始化配置: text=$currentText, size=$currentSize, alpha=$currentAlpha, angle=$currentAngle")
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        // 设置文字
        vb?.etWatermarkText?.setText(currentText)

        // 设置大小 SeekBar (将大小映射到0-100的范围)
        val sizeProgress = ((currentSize - MIN_TEXT_SIZE).toFloat() / (MAX_TEXT_SIZE - MIN_TEXT_SIZE) * 100).toInt()
        vb?.seekBarWatermarkSize?.progress = sizeProgress
        vb?.tvWatermarkSizeValue?.text = "${currentSize}sp"

        // 设置透明度 SeekBar
        vb?.seekBarWatermarkAlpha?.progress = currentAlpha
        vb?.tvWatermarkAlphaValue?.text = "${currentAlpha}%"

        // 设置角度 SeekBar (将-90到90映射到0-180)
        val angleProgress = currentAngle - MIN_ANGLE
        vb?.seekBarWatermarkAngle?.progress = angleProgress
        vb?.tvWatermarkAngleValue?.text = "${currentAngle}°"

        // SeekBar 监听
        vb?.seekBarWatermarkSize?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 将进度转换为实际大小
                currentSize = MIN_TEXT_SIZE + (progress * (MAX_TEXT_SIZE - MIN_TEXT_SIZE) / 100)
                vb?.tvWatermarkSizeValue?.text = "${currentSize}sp"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        vb?.seekBarWatermarkAlpha?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentAlpha = progress
                vb?.tvWatermarkAlphaValue?.text = "${currentAlpha}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        vb?.seekBarWatermarkAngle?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 将进度转换为实际角度 (0-180 -> -90到90)
                currentAngle = progress + MIN_ANGLE
                vb?.tvWatermarkAngleValue?.text = "${currentAngle}°"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * 设置点击事件
     */
    private fun setOnClick() {
        // 关闭按钮
        vb?.imgClose?.setOnClickListener {
            dismiss()
        }

        // 重置按钮
        vb?.tvReset?.setOnClickListener {
            resetToDefault()
        }

        // 确认按钮
        vb?.tvConfirm?.setOnClickListener {
            saveAndApply()
        }
    }

    /**
     * 重置为默认设置（保留文字）
     */
    private fun resetToDefault() {
        // 保留文字，重置其他设置
        currentSize = DEFAULT_TEXT_SIZE
        currentAlpha = DEFAULT_ALPHA
        currentAngle = DEFAULT_ANGLE

        Log.d(TAG, "重置为默认设置: size=$currentSize, alpha=$currentAlpha, angle=$currentAngle")

        // 更新UI
        val sizeProgress = ((currentSize - MIN_TEXT_SIZE).toFloat() / (MAX_TEXT_SIZE - MIN_TEXT_SIZE) * 100).toInt()
        vb?.seekBarWatermarkSize?.progress = sizeProgress
        vb?.tvWatermarkSizeValue?.text = "${currentSize}sp"

        vb?.seekBarWatermarkAlpha?.progress = currentAlpha
        vb?.tvWatermarkAlphaValue?.text = "${currentAlpha}%"

        val angleProgress = currentAngle - MIN_ANGLE
        vb?.seekBarWatermarkAngle?.progress = angleProgress
        vb?.tvWatermarkAngleValue?.text = "${currentAngle}°"

        // 提示用户
        "已恢复默认设置".tip()
    }

    /**
     * 保存并应用配置
     */
    private fun saveAndApply() {
        // 获取输入的文字
        currentText = vb?.etWatermarkText?.text?.toString() ?: ""

        Log.d(TAG, "保存配置: text=$currentText, size=$currentSize, alpha=$currentAlpha, angle=$currentAngle")

        // 保存到本地
        saveKV(Keys.WATERMARK_CUSTOM_TEXT, currentText)
        saveKV(Keys.WATERMARK_TEXT_SIZE, currentSize)
        saveKV(Keys.WATERMARK_ALPHA, currentAlpha)
        saveKV(Keys.WATERMARK_ANGLE, currentAngle)

        // 发送事件通知更新
        EventBus.getDefault().post(DataEvent(DataEvent.WATERMARK_CONFIG_CHANGED))

        // 提示用户
        "水印设置已更新".tip()

        dismiss()
    }


}
