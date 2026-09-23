package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.GridLayoutManager
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.AreaAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewEasyBufferQueryBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.MapEvent
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import kotlin.math.roundToInt


class EasyBufferQueryView : LinearLayout {

    val layoutId: Int = R.layout.view_easy_buffer_query
    var vb: ViewEasyBufferQueryBinding? = null
    var distance = 100.0 //缓冲区范围
    var queryType = 0 //查询方式 0定位 1选点 2输入

    // 前50%的范围定义
    private val minValue = 100
    private val middleValue = 1000
    private val maxValue = 10000
    private val step = 50  // 前50%的步长


    fun initView(context: Context?) {
//        EventBus.getDefault().register(this)
        if(context !=null){
            mContext = context
            val contentView: View = LayoutInflater.from(mContext).inflate(layoutId, null)
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params
            vb = ViewEasyBufferQueryBinding.bind(contentView)
            vb?.apply {
                addView(root)

                setupSeekBar()
            }


        }
    }


    lateinit var mContext: Context
    lateinit var mActivity: Activity


    constructor(context: Context?) : super(context) {
        initViewData(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewData(context, attrs)
    }

    override fun onFinishInflate() {
        initViewData()

        super.onFinishInflate()
    }

    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)
        if (context == null || attrs == null) return
    }

    fun initViewData() {

        vb!!.llEasyAreaView.onClick {}

        vb!!.llArea.onClick {
            if( vb!!.llcontent.isShown()){
                vb!!.llcontent.gone()
                vb!!.ivClose.gone()
            }else{
                vb!!.llcontent.show()
                vb!!.ivClose.show()
            }
        }

        vb!!.ivClose.onClick {
            vb!!.llcontent.gone()
            vb!!.ivClose.gone()
        }

        vb!!.llBufferLoaction.onClick {
            queryType = 0
            EventBus.getDefault().post(MapEvent(MapEvent.SEND_BUFFER_QUERY_TASK, queryType,distance))
        }
        vb!!.llBufferCustom.onClick {
            queryType = 1
            EventBus.getDefault().post(MapEvent(MapEvent.SEND_BUFFER_QUERY_TASK, queryType,distance))
        }
        vb!!.llBufferInput.onClick {
            queryType = 2
            EventBus.getDefault().post(MapEvent(MapEvent.SEND_BUFFER_QUERY_TASK, queryType,distance))
        }

    }


    private fun setupSeekBar() {
        // 设置初始值
        vb!!.sbBuffer.progress = 0
        updateValueDisplay(minValue)

        vb!!.sbBuffer.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = calculateValueFromProgress(progress)
                updateValueDisplay(value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 可选：开始滑动时的处理
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 当用户停止滑动时，如果是前50%，吸附到最近的步长
                val value = calculateValueFromProgress(seekBar.progress)
                distance = value.toDouble()
                if (seekBar.progress <= 50) {
                    // 找到最接近的步长值
                    val snappedValue = snapToStep(value)
                    val snappedProgress = calculateProgressFromValue(snappedValue)
                    seekBar.progress = snappedProgress
                    updateValueDisplay(snappedValue)
                }


                EventBus.getDefault().post(MapEvent(MapEvent.SEND_BUFFER_QUERY_TASK,queryType,distance,false))
            }
        })
    }

    /**
     * 将进度值（0-100）映射到实际值
     * 前50%（0-50）：100-1000，定量步长50
     * 后50%（50-100）：1000-10000，自由滑动
     */
    private fun calculateValueFromProgress(progress: Int): Int {
        return when {
            progress <= 50 -> {
                // 前50%：定量步长
                // 将0-50的进度映射到离散的步长值
                val stepCount = (middleValue - minValue) / step
                val progressRatio = progress / 50.0f
                val stepIndex = (progressRatio * stepCount).roundToInt()
                minValue + stepIndex * step
            }
            else -> {
                // 后50%：自由滑动
                val progressRatio = (progress - 50) / 50.0f
                (middleValue + progressRatio * (maxValue - middleValue)).roundToInt()
            }
        }.coerceIn(minValue, maxValue)
    }

    /**
     * 将值吸附到最近的步长（仅对前50%范围有效）
     */
    private fun snapToStep(value: Int): Int {
        if (value >= middleValue) return value

        // 计算距离最近的步长
        val stepCount = (value - minValue).toFloat() / step
        val lowerStep = minValue + (stepCount.toInt() * step)
        val upperStep = lowerStep + step

        // 选择距离最近的步长值
        return if ((value - lowerStep) < (upperStep - value)) {
            lowerStep
        } else {
            upperStep.coerceAtMost(middleValue)  // 不超过1000
        }
    }

    /**
     * 反向映射：从值获取进度
     */
    private fun calculateProgressFromValue(value: Int): Int {
        return when {
            value <= middleValue -> {
                // 前50%范围：100-1000，步长50
                val stepIndex = (value - minValue) / step
                val stepCount = (middleValue - minValue) / step
                (stepIndex.toFloat() / stepCount * 50).roundToInt()
            }
            else -> {
                // 后50%范围：1000-10000
                50 + ((value - middleValue) / (maxValue - middleValue).toFloat() * 50).roundToInt()
            }
        }.coerceIn(0, 100)
    }

    /**
     * 更新显示的值
     */
    private fun updateValueDisplay(value: Int) {
        val displayText = if (value < 1000) {
            "${value}米"
        } else {
            val kmValue = value / 1000.0
            if (value % 1000 == 0) {
                "${kmValue.toInt()}公里"
            } else {
                // 保留一位小数，且去除多余的0
                val formatted = "%.1f".format(kmValue)
                "${formatted}公里"
            }
        }
        vb!!.tvDistance.text = displayText
    }

    /**
     * 设置指定值（外部调用）
     */
    fun setValue(targetValue: Int) {
        val progress = calculateProgressFromValue(targetValue)
        vb!!.sbBuffer.progress = progress
        updateValueDisplay(targetValue)
    }



    fun initView(activity :Activity, str :String = ""){
        this.mActivity = activity


    }



}