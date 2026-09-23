package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenidialoglib.dialog.ListDialog
import com.jameni.jamenidialoglib.i.DialogItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewSelectionLineBinding
import com.jwch.gwyt_project.databinding.ViewTextLineBinding
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.model.SelectionListModel


class SelectionLine : LinearLayout, DialogItemClickListener {
    val layoutId: Int = R.layout.view_selection_line
    var vb: ViewSelectionLineBinding? = null

    fun initView(context: Context?) {
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params

            vb = ViewSelectionLineBinding.bind(contentView)

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
    var tag: Int = 0

    var singleLine: Boolean = false
    var isCenter: Boolean = true
    var outsideCancle: Boolean = true

    var keyWeight: Float = 2.5f
    var heighScale: Float = 0.7f

    var datalist: MutableList<SelectionListModel>? = null
    var listDialog: ListDialog? = null
    var listener: DialogItemClickListener? = null
    var selectData: SelectionListModel? = null


    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)

        if (context == null || attrs == null) return


        val array = context.obtainStyledAttributes(attrs, R.styleable.SelectionLine)

        array.apply {
            key = getString(R.styleable.SelectionLine_key)
            value = getString(R.styleable.SelectionLine_value)
            hint = getString(R.styleable.SelectionLine_hint)

            keyColor = getColor(R.styleable.SelectionLine_key_color, ContextCompat.getColor(context, R.color.txt_black))
            valueColor = getColor(R.styleable.SelectionLine_value_color, ContextCompat.getColor(context, R.color.txt_black))
            hintColor = getColor(R.styleable.SelectionLine_hint_color, 0)
            bgColor = getColor(R.styleable.SelectionLine_bg_color, ContextCompat.getColor(context, com.jameni.basepage_lib.R.color.whiteColor))
            singleLine = getBoolean(R.styleable.SelectionLine_singleLine, false)
            keyWeight = getFloat(R.styleable.SelectionLine_key_weight, 2.5f)
            keyTextSize = getInteger(R.styleable.SelectionLine_key_txt_size, 15)
            valueTextSize = getInteger(R.styleable.SelectionLine_value_txt_size, 14)
            maxLength = getInteger(R.styleable.SelectionLine_max_length, 0)
            tag = getInteger(R.styleable.SelectionLine_dialog_tag, 0)

            isCenter = getBoolean(R.styleable.SelectionLine_dialog_center, true)
            outsideCancle = getBoolean(R.styleable.SelectionLine_outsie_cancleable, true)

            heighScale = getFloat(R.styleable.SelectionLine_key_weight, 0.7f)

        }

    }


    fun setSelection(obj: Any) {

        datalist?.run {
            when (obj) {
                is Int -> if (size > obj) selectData = get(obj)
                is String -> {

                    forEach {
                        if (it.getText() == obj) selectData = it
                    }
                }
            }

            selectData?.run {
                vb!!.tvValue.text = getText()
            }
        }
    }


    fun initViewData() {

        vb!!.tvKey.text = key.self()
        vb!!.tvValue.setText(value.self())
        vb!!.tvValue.setHint(hint.self())

        vb!!.tvKey.setTextColor(keyColor)
        vb!!.tvValue.setTextColor(valueColor)
        if (hintColor != 0) vb!!.tvValue.setHintTextColor(hintColor)
        vb!!.llSelectionline.setBackgroundColor(bgColor)

        if (singleLine) {
            vb!!.tvValue.isSingleLine = singleLine
            vb!!.tvValue.maxLines = 1
        }

        //设置比重
        var keyParmas: LayoutParams = vb!!.tvKey.layoutParams as LayoutParams
        keyParmas.weight = keyWeight
        vb!!.tvKey.layoutParams = keyParmas

        //设置文本框高度


        if (maxLength > 0) {
            vb!!.tvValue.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength)))
        }

        vb!!.llSelectionline?.setOnClickListener {
            showSelectionList()
        }

    }

    fun showSelectionList() {

        datalist?.let {

            if (it.isEmpty()) {
                CommonUtil.tip(context, "暂无可选项目")
                return
            }


            listDialog = listDialog ?: ListDialog(context as Activity?, datalist, this@SelectionLine, isCenter)

            listDialog?.run {
                setCanceledOnTouchOutside(outsideCancle)
                setTag(tag)
                if (!isShowing) show()
            }
        }
    }

    override fun dialogItemClick(obj: Any?, position: Int, flag: Int) {

        obj?.let {
            selectData = obj as SelectionListModel
            vb!!.tvValue.text = selectData?.value.self()
        }
        listener?.dialogItemClick(obj, position, flag)
    }


    fun getSelectKey(): String? = selectData?.key

    fun getSelectValue(): String? = selectData?.value
}