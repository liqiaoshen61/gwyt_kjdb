package com.jwch.gwyt_project.view

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewEditLineBinding
import com.jwch.gwyt_project.ext.self

class EditLine : LinearLayout {

    val layoutId: Int = R.layout.view_edit_line
    var vb: ViewEditLineBinding? = null

    fun initView(context: Context?) {
        context?.let {
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            contentView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            vb = ViewEditLineBinding.bind(contentView)

            vb?.apply {
                addView(root)
            }
        }
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewData(context, attrs)
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
    var editable: Boolean = true

    var keyWeight: Float = 2.5f
    var valueHeight: Float = 0f

//    var intputType :


    fun initViewData(context: Context?, attrs: AttributeSet?) {

        initView(context)

        if (context == null || attrs == null) return

        val array = context.obtainStyledAttributes(attrs, R.styleable.EditLine)


        array.run {
            key = getString(R.styleable.EditLine_key)
            value = getString(R.styleable.EditLine_value)
            hint = getString(R.styleable.EditLine_hint)

            keyColor = getColor(R.styleable.EditLine_key_color, ContextCompat.getColor(context, R.color.txt_black))
            valueColor = getColor(R.styleable.EditLine_value_color, ContextCompat.getColor(context, R.color.txt_black))
            hintColor = getColor(R.styleable.EditLine_hint_color, 0)
            bgColor = getColor(R.styleable.EditLine_bg_color, ContextCompat.getColor(context, com.jameni.basepage_lib.R.color.whiteColor))

            editable = getBoolean(R.styleable.EditLine_editable, true)
            singleLine = getBoolean(R.styleable.EditLine_singleLine, false)

            keyWeight = getFloat(R.styleable.EditLine_key_weight, 2.5f)
            valueHeight = getDimension(R.styleable.EditLine_value_height, 0f)

            keyTextSize = getInteger(R.styleable.EditLine_key_txt_size, 15)
            valueTextSize = getInteger(R.styleable.EditLine_value_txt_size, 14)
            maxLength = getInteger(R.styleable.EditLine_max_length, 0)
        }
    }


    fun initViewData() {

        vb!!.tvKey.text = key.self()
        vb!!.etValue.setText(value.self())
        vb!!.etValue.setHint(hint.self())

        vb!!.tvKey.setTextColor(keyColor)
        vb!!.etValue.setTextColor(valueColor)
        if (hintColor != 0)  vb!!.etValue.setHintTextColor(hintColor)
        vb!!.llEditline.setBackgroundColor(bgColor)

        vb!!.etValue.isEnabled = editable
        if (singleLine) {
            vb!!.etValue.isSingleLine = singleLine
            vb!!.etValue.maxLines = 1
            valueHeight = 0f
        }


        //设置比重
        var keyParmas: LayoutParams =  vb!!.tvKey.layoutParams as LayoutParams
        keyParmas.weight = keyWeight
        vb!!.tvKey.layoutParams = keyParmas

        //设置文本框高度
        if (valueHeight > 0) {

            var valueParmas: LayoutParams =  vb!!.etValue.layoutParams as LayoutParams
            valueParmas.height = valueHeight.toInt()
             vb!!.etValue.layoutParams = valueParmas
             vb!!.etValue.isSingleLine = false
             vb!!.etValue.maxLines = Int.MAX_VALUE

//            keyParmas.height = valueHeight.toInt()
//            tvKey.layoutParams = keyParmas
//            etValue.gravity = Gravity.LEFT and Gravity.TOP
//            tvKey.gravity = Gravity.LEFT and Gravity.TOP

        }


        if (maxLength > 0) {
            vb!!.etValue.setFilters(arrayOf<InputFilter>(LengthFilter(maxLength)))
        }

    }


    fun setValueText(v: String?) {
        vb!!.etValue.setText(v.self())
    }

    fun getValueText(): String =  vb!!.etValue.text.toString().self()


    override fun onFinishInflate() {
        initViewData()
        super.onFinishInflate()
    }


}