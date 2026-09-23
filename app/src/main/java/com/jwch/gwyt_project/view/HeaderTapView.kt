package com.jwch.gwyt_project.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor

class HeaderTapView : LinearLayout {


    val layoutId: Int = R.layout.view_header_tap
    val cutFlag = ","


    lateinit var mContext: Context
    var viewList = mutableListOf<View>()

    lateinit var onSelectListener: (Int, String) -> Unit

    fun setListener(block: (Int, String) -> Unit) {
        onSelectListener = block
    }


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

    var text_size: Int = 15
    var text_padding: Int = 0
    var lable_gravity: Int = 0
    var isaverage = true //是否等分
    var isunderlineMatch = false //下划线是否撑满
    var lable_array: String = ""
    var underlineColor: Int = 0
    var selectTextColor: Int = 0
    var unselectTextColor: Int = 0


    var currentIndex = 0//当前位置
    var currentText = ""//当前选中文本


    private fun getItemView(data: String, select: Boolean, index: Int) {
        if (::mContext.isInitialized) {
            val contentView: View = LayoutInflater.from(mContext).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            //等分
            if (isaverage) {
                params.weight = 1.0f
            }


            val layout = contentView.findViewById<RelativeLayout>(R.id.rlTap)
            val text = contentView.findViewById<TextView>(R.id.tvTap)
            val underLine = contentView.findViewById<View>(R.id.underLine)

//            when (index) {
//                0 -> {
//                    layout.setBackgroundColor(mContext.getColor(R.color.main_color))
//                }
//                1 -> {
//                    layout.setBackgroundColor(mContext.getColor(R.color.colorPrimaryDark))
//                }
//                2 -> {
//                    layout.setBackgroundColor(mContext.getColor(R.color.bg_gray))
//                }
//
//            }

            text.padding = text_padding
            text.textSize = text_size.toFloat()
            text.text = data
            underLine.setBackgroundColor(underlineColor)

            //视图要等分的时候，下划线撑满才有效
            if (isaverage && isunderlineMatch) {
//                "isunderlineMatch${isunderlineMatch}".printMsg()

//                val lineParmas = underLine.layoutParams as RelativeLayout.LayoutParams
//                lineParmas.removeRule(RelativeLayout.ALIGN_LEFT)
//                lineParmas.removeRule(RelativeLayout.ALIGN_RIGHT)
//                lineParmas.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
//                lineParmas.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

                val textParmas = text.layoutParams as RelativeLayout.LayoutParams
                textParmas.width = RelativeLayout.LayoutParams.MATCH_PARENT

            }

            if (select) {
                underLine.show()
                text.textColor = selectTextColor
            } else {
                underLine.hide()
                text.textColor = unselectTextColor
            }


            contentView.layoutParams = params
            contentView.tag = index

            contentView.onClick {

                currentIndex = it.tag as Int
                currentText = textList[currentIndex]

                viewList.forEachIndexed { i, view ->
                    val line = view.findViewById<View>(R.id.underLine)
                    val clickText = view.findViewById<TextView>(R.id.tvTap)
                    if (i == currentIndex) {
                        line.show()
                        clickText.textColor = selectTextColor
                    } else {
                        line.hide()
                        clickText.textColor = unselectTextColor
                    }
                }

                if (::onSelectListener.isInitialized) {
                    onSelectListener(currentIndex, currentText.self())
                }
            }


            addView(contentView)
            viewList.add(contentView)
        }

    }

    private fun initViewData(context: Context?, attrs: AttributeSet?) {

        if (context == null || attrs == null) return
        mContext = context
        val array = context.obtainStyledAttributes(attrs, R.styleable.HeaderTapView)

        array.apply {
            text_padding = getDimension(R.styleable.HeaderTapView_text_padding, 5.0f).toInt()
            lable_gravity = getInt(R.styleable.HeaderTapView_lable_gravity, 0)
            lable_array = getString(R.styleable.HeaderTapView_lable_array).self()
            isaverage = getBoolean(R.styleable.HeaderTapView_isaverage, true)
            underlineColor = getColor(R.styleable.HeaderTapView_underline_color, ContextCompat.getColor(context, com.jameni.basepage_lib.R.color.main_color))
            text_size = getInteger(R.styleable.HeaderTapView_tap_txt_size, 16)

            selectTextColor = getColor(R.styleable.HeaderTapView_select_text_color, ContextCompat.getColor(context, com.jameni.basepage_lib.R.color.main_color))
            unselectTextColor = getColor(R.styleable.HeaderTapView_unselect_text_color, ContextCompat.getColor(context, R.color.txt_black))
            isunderlineMatch = getBoolean(R.styleable.HeaderTapView_isunderline_match, false)


//            "lable array $lable_array".printMsg()
        }

    }


    lateinit var textList: List<String>


    private fun initViewData() {

        gravity = getContentGravity()

        if (lable_array.isNotEmpty() && lable_array.contains(cutFlag)) {
            textList = lable_array.split(cutFlag)
            addItemViews()
        }
    }


    private fun addItemViews() {
        if (::textList.isInitialized) {
            if (CommonUtil.matchList(textList)) {
                textList.forEachIndexed { index, s ->
                    val select = index == 0
                    if (select) {
                        currentIndex = index
                        currentText = s
                    }

                    getItemView(s, select, index)
                }
            }
        }

    }


    fun setTapData(list: List<String>) {
        if (list.size < 2) return
        if (::textList.isInitialized) return

        this.textList = list
        addItemViews()
    }


    fun getContentGravity() = when (lable_gravity) {
        0 -> Gravity.CENTER
        1 -> Gravity.LEFT
        2 -> Gravity.RIGHT
        else -> Gravity.CENTER
    }


    fun getCurrentPosition() = currentIndex
    fun getCurrentSelectText() = currentText

    fun selectIndex(i: Int) {
        if (!::textList.isInitialized)
            return

        if (i >= 0 && i < textList.size) {
            if (i != currentIndex) {
                viewList[i].performClick()
            }
        }
    }


    fun isPage(index: Int) = currentIndex == index
}