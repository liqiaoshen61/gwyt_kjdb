package com.jwch.gwyt_project.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.CheckBoxAdapter
import com.jwch.gwyt_project.databinding.ViewCheckBoxBinding
import com.jwch.gwyt_project.ext.cutString
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.SelectionListModel
import kotlin.text.isNotEmpty
import androidx.core.graphics.toColorInt


class CheckBoxtView : LinearLayout, OnItemClickListener {

    val layoutId: Int = R.layout.view_check_box
    var vb: ViewCheckBoxBinding? = null
    var checkBoxAdapter = CheckBoxAdapter()
    var datalist = mutableListOf<SelectionListModel>()
    var selectList = mutableListOf<SelectionListModel>() //选中项 多选
    var selectData: SelectionListModel? = null //选中项 单选
    var isMultiSelect = false
    var defaultCheckAll = false //默认全选
    var hideKey = false //隐藏key
    var isKeyTop = false //key默认居中展示 true时顶部对齐展示
    var oneLine = false //是否只展示一行 true：只展示一行 可滑动 false：自动换行，流线型排列
    var ui_type = 0
    var unselectAble = false //选中后，是否可以反选


    var keyTextSize: Int = 0
    var valueTextSize: Int = 0
    var keyColor: Int = 0

    fun initView(context: Context?) {
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params
            vb = ViewCheckBoxBinding.bind(contentView)
            vb?.apply {
                addView(root)
            }
        }
    }

    lateinit var mContext: Context

    constructor(context: Context?) : super(context) {
        initViewParams(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewParams(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initViewParams(context, attrs)
    }

    override fun onFinishInflate() {
        initViewData()
        super.onFinishInflate()
    }


    var key: String? = null
    var lable_array: String? = null

    var underineVisiable: Boolean = false
    var keyWeight: Float = 2.5f
    var isMustFillIn = false
    var startIndent: Float = 0f//头部缩进
    var isReadOnly = false //是否只读


    lateinit var selectionActionBlock: (MutableList<SelectionListModel>) -> Unit
    lateinit var selectionActionBlock2: (Int, SelectionListModel) -> Unit

    private fun initViewParams(context: Context?, attrs: AttributeSet?) {
        initView(context)
        if (context == null || attrs == null) return
        //获取属性
        val array = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxView)
        array.apply {

            isReadOnly = getBoolean(R.styleable.CheckBoxView_isReadOnly, false)
            key = getString(R.styleable.CheckBoxView_key)
            underineVisiable = getBoolean(R.styleable.CheckBoxView_underline_visiable, false)

            defaultCheckAll = getBoolean(R.styleable.CheckBoxView_defCheckAll, false)

            keyWeight = getFloat(R.styleable.CheckBoxView_key_weight, 2.5f)

            isMustFillIn = getBoolean(R.styleable.CheckBoxView_isMustFillIn, false)
            isMultiSelect = getBoolean(R.styleable.CheckBoxView_isMultiSelect, false)
            hideKey = getBoolean(R.styleable.CheckBoxView_hideKey, false)
            isKeyTop = getBoolean(R.styleable.CheckBoxView_isKeyTop, false)
            oneLine = getBoolean(R.styleable.CheckBoxView_oneLine, false)
            ui_type = getInteger(R.styleable.CheckBoxView_ui_type, 0)

            keyTextSize = getInteger(R.styleable.TextLineCompat_key_txt_size, 16)
            valueTextSize = getInteger(R.styleable.TextLineCompat_value_txt_size, 14)

            //左边缩进
            startIndent = getDimension(R.styleable.CheckBoxView_start_indent, 0f)
            if (startIndent > 0) {
                //缩进后，标题会被压缩，所以增大占据比例
                keyWeight = 1.8f
            }

            keyColor = getColor(
                R.styleable.TextLineCompat_key_color, ContextCompat.getColor(context, R.color.key_form_color)
            )

            //字段名
            val lableArray = getString(R.styleable.CheckBoxView_lable_array)
            setNewLabelArray(lableArray.self(), false)

            unselectAble = getBoolean(R.styleable.CheckBoxView_unselect_able, false)


        }

    }

    fun initViewData() {

        vb?.apply {
            //设置key
            tvKey.text = key.self()
            tvKey.visiable(key!!.isNotEmpty())
            //设置比重
//            var keyParmas: LayoutParams = tvKey.layoutParams as LayoutParams
//            keyParmas.weight = keyWeight
//            tvKey.layoutParams = keyParmas

            tvKey.textSize = keyTextSize.toFloat()

            underLine.visibility = if (underineVisiable) View.VISIBLE else View.GONE

            hideKey.yes {
                tvKey.gone()
                tvMustFillFlag.gone()
            }

            showMustFillFlag(isMustFillIn)

            oneLine.no {
                val flexboxLayoutManager = FlexboxLayoutManager(context)
                flexboxLayoutManager.flexDirection = FlexDirection.ROW //主轴为水平方向，起点在左端。
                flexboxLayoutManager.flexWrap = FlexWrap.WRAP //按正常方向换行
                //justifyContent 属性定义了项目在主轴上的对齐方式。
                flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START //交叉轴的起点对齐。
                lvCheckList.layoutManager = flexboxLayoutManager
            }.yes {
                lvCheckList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            lvCheckList.adapter = checkBoxAdapter
            checkBoxAdapter.update(datalist)
            checkBoxAdapter.setOnItemClickListener(this@CheckBoxtView)
            checkBoxAdapter.uiType = ui_type

            when(ui_type){
                3 ->{
                    tvKey.setTextColor("#29B66A".toColorInt())
                    tvKey.setBackgroundColor("#1929B66A".toColorInt())
                    tvKey.gravity = Gravity.CENTER
                    tvKey.setTypeface(tvKey.typeface, Typeface.BOLD)
                }
                4 ->{
                    tvKey.setTextColor(findColor(context, R.color.txt_black))
                    tvKey.setBackgroundColor(findColor(context, R.color.transColor))
                    tvKey.gravity = Gravity.LEFT
                    tvKey.setTypeface(tvKey.typeface, Typeface.NORMAL)
                }
                else ->{
                    tvKey.setTextColor(Color.parseColor("#FFFFFF"))
                    tvKey.setBackgroundColor(findColor(context, R.color.transColor))
                    tvKey.gravity = Gravity.LEFT
                    tvKey.setTypeface(tvKey.typeface, Typeface.NORMAL)
                }
            }

            //设置value
//            showValue()
//            setValueIsReadOnly(isReadOnly)
//            initClickListener()

            setCheckReadOnly(isReadOnly)

            defaultCheckAll.yes {
                datalist.forEach {
                    it.select = true
                }
                checkBoxAdapter.update(datalist)
            }

            isKeyTop.yes {
                llContent.gravity = Gravity.TOP
                tvKey.setPadding(0,resources.getDimensionPixelOffset(com.jameni.basepage_lib.R.dimen.nSize5),0,0)
            }.no {
                llContent.gravity = Gravity.CENTER_VERTICAL
            }

        }
    }

    fun setCheckReadOnly(only: Boolean = true) {
        isReadOnly = only
        isReadOnly.yes {
            vb?.lvCheckList?.isEnabled = false

        }
    }


    /**
     *设置列表选项数据
     */
    fun setNewLabelArray(strArray: String, isUpdate: Boolean = true) {
        if (CommonUtil.isNotEmpty(strArray)) {

            val textList = strArray.cutString(",")

            textList.forEach {
                datalist!!.add(SelectionListModel(it))
            }
        }

        isUpdate.yes {
            updateList(datalist!!)
        }
    }

    /**
     *更新列表选项数据
     */
    fun updateList(list: MutableList<SelectionListModel>, selectAll: Boolean? = null) {
        datalist = list
        isMultiSelect.yes { selectList.clear() }

        if (selectAll != null) {
            datalist.forEach {
                it.select = selectAll
                selectAll!!.yes { selectList.add(it) }
            }
        }
        checkBoxAdapter.update(datalist)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = datalist[position]
        isMultiSelect.yes {
            item.select = !item.select

            selectList = datalist.filter { it.select }.toMutableList()
        }.no {

            if (selectData != null) {
                if (unselectAble) {
                    //可反选
                    if (selectData!!.key!! == item.key!!) {
                        selectData!!.select = selectData!!.select.not()
                    } else {
                        selectData!!.select = false
                        selectData = item
                        selectData?.select = true
                    }

                } else {
                    //不可反选
                    selectData!!.select = false
                    selectData = item
                    selectData?.select = true
                }

            } else {
                selectData = item
                selectData?.select = true
            }


        }
        checkBoxAdapter.update(datalist)

        isMultiSelect.yes {
            if (::selectionActionBlock.isInitialized) {
                selectionActionBlock(selectList)
            }

        }.no {
            if (::selectionActionBlock.isInitialized) {
                val list = mutableListOf<SelectionListModel>()
                list.add(selectData!!)
                selectionActionBlock(list)
            }
            if (::selectionActionBlock2.isInitialized) {

                if (selectData!!.select) {
                    selectionActionBlock2(position, selectData!!)
                } else {
                    selectionActionBlock2(position, SelectionListModel("",""))
                }
            }
        }
    }


//    fun getCheckResult(): Int = selectResult

    fun setHighLight() {
        vb?.apply {
            llItem.setBackgroundColor(findColor(context, R.color.form_highlight))
        }
    }

    /**
     * 获取已选列表的key
     */
    fun getSelectKey(): String {
        val sb = StringBuffer()
        if (isMultiSelect) {
            selectList.forEachIndexed { index, selectModel ->
                if (selectModel.key.self().isNotEmpty()) {
                    sb.append(selectModel.key)

                    if (index < selectList.size - 1) {
                        sb.append(",")
                    }
                }
            }

        } else {
            sb.append(selectData?.key.self())
        }
        return sb.toString()
    }


    /**
     * 获取已选列表的值
     */
    fun getSelectValue(): String {
        val sb = StringBuffer()

        if (isMultiSelect) {
            selectList.forEachIndexed { index, selectModel ->

                if (selectModel.value.self().isNotEmpty()) {
                    sb.append(selectModel.value)

                    if (index < selectList.size - 1) {
                        sb.append(",")
                    }
                }
            }

        } else {
            sb.append(selectData?.value.self())
        }


        return sb.toString()
    }

    //全选
    fun selectAll(){
        updateList(datalist, true)
        selectList.clear()
        selectList.addAll(datalist)
    }


    fun setSelection(index: Int){
        CommonUtil.matchList(datalist).yes {
            datalist.forEach {
                it.select = false
            }
            val item = datalist[index]
            item.select = true

            selectList.clear()
            selectList.add(item)
            selectData = item

            checkBoxAdapter.update(datalist)
        }
    }


    fun setSelectionByKey(key: String){

        datalist?.run {

            isMultiSelect.yes {
//                val array = key.cutString()
//
//                array.forEach { item1 ->
//                    forEach { item2 ->
//
//                        if (CommonUtil.isNotNull(item1) && CommonUtil.isNotNull(item2)) {
//
//                            if (item1 == item2.key || item1 == item2.value) {
//                                item2.select = true
//                                selectList.add(item2)
//                            }
//
//                        }
//                    }
//                }
//                selectList.toJson().printMsg()


            }.no {
                selectData?.select = false
                forEach {
                    if (CommonUtil.isNotEmpty(it.value) && it.value == key) {
                        selectData = it
                        selectData?.select = true
                        return@no
                    }else if (CommonUtil.isNotEmpty(it.key) && it.key == key){
                        selectData = it
                        selectData?.select = true
                        return@no
                    }
                }
            }
            checkBoxAdapter.update(datalist)
        }
    }


    fun reset() {
        datalist.forEach {
            it.select = false
        }
        updateList(datalist)

        selectList.clear()
        selectData = null

    }

    /**
     *展示是否必填标识
     */
    fun showMustFillFlag(show: Boolean) {
        vb?.apply {
            if (tvMustFillFlag != null) {
                isMustFillIn = show
                if (show) {
                    tvMustFillFlag.show()
                } else {
                    tvMustFillFlag.hide()
                }
            }
        }

    }


}