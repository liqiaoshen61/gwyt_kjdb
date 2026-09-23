package com.jwch.gwyt_project.view

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewTextLineBinding
import com.jwch.gwyt_project.ext.self


class TextLine : LinearLayout {
    val layoutId: Int = R.layout.view_text_line
    var vb: ViewTextLineBinding? = null

    fun initView(context: Context?) {
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params

            vb = ViewTextLineBinding.bind(contentView)

            vb?.apply {
                addView(root)
            }
        }
    }


    lateinit var mContext: Context

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

    var key: String? = null
    var value: String? = null
    var hint: String? = null

    var keyTextSize: Int = 0
    var valueTextSize: Int = 0
    var keyColor: Int = 0
    var valueColor: Int = 0
    var hintColor: Int = 0
    var bgColor: Int = 0
    var maxLength: Int = 0


    var singleLine: Boolean = false

    var keyWeight: Float = 2.5f
    var valueHeight: Float = 0f
    var horizontal_padding: Float = 0f
    var vertical_padding: Float = 0f


    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)

        if (context == null || attrs == null) return


        val array = context.obtainStyledAttributes(attrs, R.styleable.TextLine)

        array.apply {
            key = getString(R.styleable.TextLine_key)
            value = getString(R.styleable.TextLine_value)
            hint = getString(R.styleable.TextLine_hint)

            keyColor = getColor(R.styleable.TextLine_key_color, ContextCompat.getColor(context, R.color.txt_black))
            valueColor = getColor(R.styleable.TextLine_value_color, ContextCompat.getColor(context, R.color.txt_black))
//            hintColor = getColor(R.styleable.TextLine_hint_color, ContextCompat.getColor(context, R.color.bg_gray))
            hintColor = getColor(R.styleable.TextLine_hint_color, 0)
            bgColor = getColor(R.styleable.TextLine_bg_color, ContextCompat.getColor(context, com.jameni.basepage_lib.R.color.whiteColor))

            singleLine = getBoolean(R.styleable.TextLine_singleLine, false)

            keyWeight = getFloat(R.styleable.TextLine_key_weight, 2.5f)
            valueHeight = getDimension(R.styleable.TextLine_value_height, 0f)


            keyTextSize = getInteger(R.styleable.TextLine_key_txt_size, 15)
            valueTextSize = getInteger(R.styleable.TextLine_value_txt_size, 14)

            maxLength = getInteger(R.styleable.TextLine_max_length, 0)


            horizontal_padding = getDimension(R.styleable.TextLine_horizontal_padding, 0f)
            vertical_padding = getDimension(R.styleable.TextLine_vertical_padding, 0f)
        }

    }


    fun setValueText(v: String?) = vb!!.tvValue.setText(v.self())
    fun getValueText(): String = vb!!.tvValue.text.toString()


    fun initViewData() {

        vb!!.tvKey.text = key.self()
        vb!!.tvValue.setText(value.self())
        vb!!.tvValue.setHint(hint.self())

        vb!!.tvKey.setTextColor(keyColor)
        vb!!.tvValue.setTextColor(valueColor)
        if (hintColor != 0) vb!!.tvValue.setHintTextColor(hintColor)
        vb!!.llTextline.setBackgroundColor(bgColor)

        if (singleLine) {
            vb!!.tvValue.isSingleLine = singleLine
            vb!!.tvValue.maxLines = 1
            valueHeight = 0f
        }


        //设置比重
        var keyParmas: LayoutParams = vb!!.tvKey.layoutParams as LayoutParams
        keyParmas.weight = keyWeight
        vb!!.tvKey.layoutParams = keyParmas

        //设置文本框高度
        if (valueHeight > 0) {

            var valueParmas: LayoutParams = vb!!.tvValue.layoutParams as LayoutParams
            valueParmas.height = valueHeight.toInt()
            vb!!.tvValue.layoutParams = valueParmas
            vb!!.tvValue.isSingleLine = false
            vb!!.tvValue.maxLines = Int.MAX_VALUE


//            keyParmas.height = valueHeight.toInt()
//            tvKey.layoutParams = keyParmas
//            tvValue.gravity = Gravity.LEFT and Gravity.TOP
//            tvKey.gravity = Gravity.LEFT and Gravity.TOP

        }


        if (maxLength > 0) {
            vb!!.tvValue.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength)))
        }

        if (horizontal_padding > 0) {
            vb!!.llTextline.setPadding(horizontal_padding.toInt(), vb!!.llTextline.paddingTop, horizontal_padding.toInt(), vb!!.llTextline.bottom)
        }
        if (vertical_padding > 0) {
            vb!!.llTextline.setPadding(vb!!.llTextline.paddingLeft, vertical_padding.toInt(), vb!!.llTextline.paddingRight, vertical_padding.toInt())
        }
    }

    fun setTitle(title: String) {
        this.key = key
        vb!!.tvKey.text = title

    }
}