package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jameni.jamenidialoglib.i.DialogItemClickListener
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.MultiSelectionAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewTextLineCompatBinding
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.SelectionListModel
import com.jwch.gwyt_project.view.dialog.SingleSelectListDialog
import com.loper7.date_time_picker.DateTimeConfig
import com.loper7.date_time_picker.dialog.CardDatePickerDialog
import com.qmuiteam.qmui.kotlin.onClick
import kotlin.text.isNotEmpty
import com.jwch.gwyt_project.ext.isNotNullObj
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.cutString
import com.jwch.gwyt_project.ext.hide
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.util.Keys


class TextLineCompat : LinearLayout, DialogItemClickListener {

    val layoutId: Int = R.layout.view_text_line_compat
    var vb: ViewTextLineCompatBinding? = null
    fun initView(context: Context?) {
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params

            vb = ViewTextLineCompatBinding.bind(contentView)

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

    companion object {
        val VIEW_TEXT = 0//普通文本展示
        val VIEW_EIDT_TEXT_NORMAL = 1//普通文本输入
        val VIEW_EIDT_TEXT_AREA = 2//文本域输入
        val VIEW_EIDT_TEXT_NUMBER_ONLY = 3//输入纯数字，不含小数点
        val VIEW_EIDT_TEXT_NUMBER = 4//输入数字
        val VIEW_SELECTION_LIST_SINGLE = 5//列表，单选
        val VIEW_SELECTION_LIST_MULTI = 6//列表多选
        val VIEW_SELECTION_TIME = 7//选择时间
        val VIEW_LOCATION = 8//定位控件
    }

    var viewType = 0 //控件类型
    var key: String? = null
    var value: String? = null
    var hint: String? = null
    var unit: String? = null//单位

    var keyTextSize: Int = 0
    var valueTextSize: Int = 0
    var keyColor: Int = 0
    var valueColor: Int = 0
    var hintColor: Int = 0
    var bgColor: Int = 0
    var maxLength: Int = 0
    var dialogTag: Int = 0


    var singleLine: Boolean = false
    var underineVisiable: Boolean = true
    var keyWeight: Float = 2.5f
    var valueHeight: Float = 0f
    var horizontal_padding: Float = 0f
    var vertical_padding: Float = 0f

    var isMustFillIn = false
    var fieldName = ""//字段名称
    var startIndent: Float = 0f//头部缩进
    var isReadOnly = false //是否只读

    var datalist: MutableList<SelectionListModel>? = null
    var selectList: MutableList<SelectionListModel> = mutableListOf()
    var listDialog: SingleSelectListDialog? = null
    var isCenter: Boolean = true
    var multiListDialog: MultiListDialog? = null
    var multiAdapter: MultiSelectionAdapter<SelectionListModel>? = null
    var listener: DialogItemClickListener? = null
    var selectData: SelectionListModel? = null
    var isMultiSelect = false
    var outsideCancle: Boolean = true//点击对话框外部，是否关闭对话框
    var heighScale: Float = 0.7f//对话框高度比例

    var maxTime: Long? = null//最大时间
    var miniTime: Long? = null//最小时间
    var defTime: Long? = null//默认时间，当前
    var selectTime: Long? = null//选中的时间
    var timeFormatType = ""

    var lat: Double? = null//纬度
    var lng: Double? = null//经度
    var address = ""//地址
    var province = ""//省
    var city = ""//市
    var district = ""//区县
    var town = ""//乡镇
    var village = ""//村庄
    var isAutoGetLocation = true//自动开启定位获取地址

    //    lateinit var locationUtil: BaiduLocationService
//    lateinit var latlngList: MutableList<LatLng>
    var getLocationType = 0 //获取定位信息的方式 0 gps定位 1 从地图上选择
    var getTimeType = 0 //时间选择器的类型 0 年 1 年月 2 年月日 3 年月日时 4年月日时分
    var activity: Activity? = null

    var clearAble = false //是否可以清除已输入的信息
    var copyAble = false //
    var data: Any? = null

    var showUnit = false //是否展示单位
    var isOld = false // 老人模式，大号字
    var oldKeySize = 20f
    var oldValueSize = 18f

    var showImgCustom = false //是否展示自定义图片
    var showTvCustom = false //是否展示自定义文字
    var iconResId = 0
    var customTxt : String? = ""

    var showValueBorder = false //是否展示value的外框

    var isOnTextChanged = false //是否监听输入变化
    lateinit var onTextChangedBlock: (String) -> Unit


    lateinit var actionBlock: () -> Unit
    lateinit var selectionActionBlock: (MutableList<SelectionListModel>) -> Unit
    lateinit var selectionActionBlock2: (Int, SelectionListModel) -> Unit

    var selectPosition = 0//单选项 选中的position
    lateinit var imgCustomBlock: () -> Unit
    lateinit var tvCustomBlock: () -> Unit

    lateinit var selectTimeActionBlock: (Long, String) -> Unit


    private fun initViewParams(context: Context?, attrs: AttributeSet?) {
        initView(context)
        if (context == null || attrs == null) return
        //获取属性
        val array = context.obtainStyledAttributes(attrs, R.styleable.TextLineCompat)
        array.apply {

            viewType = getInteger(R.styleable.TextLineCompat_view_type, VIEW_TEXT)
//            "控件类型：$viewType".printMsg()
            isReadOnly = getBoolean(R.styleable.TextLineCompat_isReadOnly, false)
            copyAble = getBoolean(R.styleable.TextLineCompat_copyAble, false)
            isOld = getBoolean(R.styleable.TextLineCompat_old_model, false)
            showImgCustom = getBoolean(R.styleable.TextLineCompat_showImgCustom, false)
            showValueBorder = getBoolean(R.styleable.TextLineCompat_showValueBorder, false)
            showTvCustom = getBoolean(R.styleable.TextLineCompat_showTvCustom, false)
            iconResId = getResourceId(R.styleable.TextLineCompat_icon_resid, R.mipmap.arrow_right)
            customTxt = getString(R.styleable.TextLineCompat_customTxt)
            key = getString(R.styleable.TextLineCompat_key)
            value = getString(R.styleable.TextLineCompat_value)
            hint = getString(R.styleable.TextLineCompat_hint)

//            "key  $key   value  $value   hint  $hint  isreadonly  $isReadOnly".printMsg()

            keyColor = getColor(
                R.styleable.TextLineCompat_key_color, ContextCompat.getColor(context, R.color.key_form_color)
            )
            valueColor = getColor(
                R.styleable.TextLineCompat_value_color, ContextCompat.getColor(context, R.color.value_form_color)
            )
            hintColor = getColor(R.styleable.TextLineCompat_hint_color, 0)
            bgColor = getColor(
                R.styleable.TextLineCompat_bg_color, ContextCompat.getColor(context, R.color.white)
            )

            singleLine = getBoolean(R.styleable.TextLineCompat_singleLine, false)
            underineVisiable = getBoolean(R.styleable.TextLineCompat_underline_visiable, true)


            keyWeight = getFloat(R.styleable.TextLineCompat_key_weight, 2.5f)
            valueHeight = getDimension(R.styleable.TextLineCompat_value_height, 0f)


            keyTextSize = getInteger(R.styleable.TextLineCompat_key_txt_size, 15)
            valueTextSize = getInteger(R.styleable.TextLineCompat_value_txt_size, 14)

            maxLength = getInteger(R.styleable.TextLineCompat_max_length, 0)
            isMustFillIn = getBoolean(R.styleable.TextLineCompat_isMustFillIn, false)

            horizontal_padding = getDimension(R.styleable.TextLineCompat_horizontal_padding, 0f)
            vertical_padding = getDimension(R.styleable.TextLineCompat_vertical_padding, 0f)

            isOnTextChanged = getBoolean(R.styleable.TextLineCompat_isOnTextChanged, false)

            //左边缩进
            startIndent = getDimension(R.styleable.TextLineCompat_start_indent, 0f)
            if (startIndent > 0) {
                //缩进后，标题会被压缩，所以增大占据比例
                keyWeight = 1.8f
            }

            //当控件为多选列表时 isMultiSelect参数为true
            isMultiSelect = viewType == VIEW_SELECTION_LIST_MULTI

            //字段名
            fieldName = getString(R.styleable.TextLineCompat_field_name).self()
            val lableArray = getString(R.styleable.TextLineCompat_lable_array)
            setNewLabelArray(lableArray.self(), false)
            isCenter = getBoolean(R.styleable.TextLineCompat_dialog_center, true)
            dialogTag = getInteger(R.styleable.TextLineCompat_dialog_tag, 0)
            outsideCancle = getBoolean(R.styleable.TextLineCompat_outsie_cancleable, true)
            heighScale = getFloat(R.styleable.TextLineCompat_height_scale, 0.7f)
            getLocationType = getInt(R.styleable.TextLineCompat_get_location_type, 0)
            getTimeType = getInt(R.styleable.TextLineCompat_select_time_type, 4)

            val strMaxTime = getString(R.styleable.TextLineCompat_max_time)
            val strMiniTime = getString(R.styleable.TextLineCompat_mini_time)
            val strDefTime = getString(R.styleable.TextLineCompat_def_time)
            timeFormatType = getString(R.styleable.TextLineCompat_time_format_type).self()

            CommonUtil.isNotEmpty(timeFormatType).no {
                timeFormatType = Config.timeFormat1
            }

            CommonUtil.isNotEmpty(strMaxTime).yes {
                maxTime = TimeUtil.getStringToDate(strMaxTime, Config.timeFormat2)
            }
            CommonUtil.isNotEmpty(strMiniTime).yes {
                miniTime = TimeUtil.getStringToDate(strMiniTime, Config.timeFormat2)
            }
            CommonUtil.isNotEmpty(strDefTime).yes {
                defTime = TimeUtil.getStringToDate(strDefTime, Config.timeFormat2)
            }

//            "strTimeFormatType  $strTimeFormatType   maxTime $defTime  miniTime $miniTime   defTime  $defTime".printMsg()
            isAutoGetLocation = getBoolean(R.styleable.TextLineCompat_isAutoGetLocation, true)

            clearAble = getBoolean(R.styleable.TextLineCompat_clearAble, false)

            unit = getString(R.styleable.TextLineCompat_unit)
            showUnit = unit.isNullOrBlank() != true

        }
        if(isOnTextChanged){
            onTextChangedlistener()
        }


    }

    fun initViewData() {

        vb?.apply {
            //设置key
            tvKey.text = key.self()
            tvKey.setTextColor(keyColor)
            //设置比重
            var keyParmas: LayoutParams = tvKey.layoutParams as LayoutParams
            keyParmas.weight = keyWeight
            tvKey.layoutParams = keyParmas

            isOld.yes {
                tvKey.textSize = oldKeySize
                tvValue.textSize = oldValueSize
                etValue.textSize = oldValueSize
                etValueMultiLine.textSize = oldValueSize
                etValueNumber.textSize = oldValueSize
                etValueNumberOnly.textSize = oldValueSize
            }
            imgCustom.visiable(showImgCustom)
            if (showImgCustom) {
                imgCustom.setImageResource(iconResId)
            }
            tvCustom.visiable(showTvCustom)
            if(showTvCustom){
                tvCustom.text = customTxt
            }

            if (showValueBorder) {
                rlValue.setBackgroundResource(R.drawable.bg_hollow_gray2_border6)
            }else{
                rlValue.setBackgroundResource(R.color.transColor)
            }
            underLine.visibility = if (underineVisiable) View.VISIBLE else View.GONE
            llTextline.setBackgroundColor(bgColor)

            if (horizontal_padding > 0) {
                llTextline.setPadding(
                    horizontal_padding.toInt(), llTextline.paddingTop, horizontal_padding.toInt(), llTextline.bottom
                )
            }
            if (vertical_padding > 0) {
                llTextline.setPadding(
                    llTextline.paddingLeft, vertical_padding.toInt(), llTextline.paddingRight, vertical_padding.toInt()
                )
            }

            showMustFillFlag(isMustFillIn)
//        前面缩进
            if (startIndent > 0) {
                val params: LayoutParams = viewIndent.layoutParams as LayoutParams
                params.width = startIndent.toInt()
            }

            if (valueHeight > 0) {
                val params = etValueMultiLine.layoutParams as RelativeLayout.LayoutParams
                params.height = valueHeight.toInt()
            }

            (maxLength != 0).yes{
                etValueNumber.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
                etValue.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
                etValueMultiLine.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
                etValueNumberOnly.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
            }

            imgClear.visiable(clearAble)
//            tvUnit.visiable(showUnit)
            showUnit.yes {
//                tvUnit.text = unit
                tvKey.text = "${key.self()}($unit)"
            }



            //设置value
            setValueText(value.self())
            setTextHint(hint.self())
            setValueTextColor(valueColor)
            setValueTextHintColor(hintColor)
            showValue()
            setValueIsReadOnly(isReadOnly)
            initClickListener()

            if (viewType == VIEW_LOCATION) {
//                locationUtil = BaiduLocationService(AppContext.app)
//                locationUtil.registerListener3 { p, c, a, addr, la, lo ->
//
//                    lat = la
//                    lng = lo
//                    province = p
//                    city = c
//                    district = a
//                    address = addr
//                    setValueText(address)
//
//                }
//
//                isAutoGetLocation.yes {
//                    locationUtil.start()
//                }
            }

            copyAble?.yes {
                tvValue.setTextIsSelectable(true)
            }


        }
    }


    fun onTextChangedlistener(){
        vb?.etValue!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                "$p0".printMsg()
                if (::onTextChangedBlock.isInitialized) {
                    onTextChangedBlock(p0.toString().self())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    fun showValue() {
        vb?.apply {
            when (viewType) {
                VIEW_EIDT_TEXT_NORMAL -> etValue.show()
                VIEW_EIDT_TEXT_AREA -> etValueMultiLine.show()
                VIEW_EIDT_TEXT_NUMBER -> etValueNumber.show()
                VIEW_EIDT_TEXT_NUMBER_ONLY -> etValueNumberOnly.show()
                else -> tvValue.show()
            }
        }

    }

    fun initClickListener() {
        vb?.apply {
            when (viewType) {
                VIEW_SELECTION_LIST_SINGLE, VIEW_SELECTION_LIST_MULTI -> {

                    llTextline.onClick(1000) {
                        if (::actionBlock.isInitialized) {
                            actionBlock()
                        }else if (!isReadOnly) {
                            showSelectionList()
                        }
                    }
                }

                VIEW_SELECTION_TIME -> {

                    llTextline.onClick(1000) {
                        if (!isReadOnly) {
                            showTimeDialog("请选择时间")
                        }
                    }
                }
                VIEW_LOCATION -> {

                    llTextline.onClick(1000) {

//                        if (getLocationType == 0) {
//                            if (::locationUtil.isInitialized) {
//                                locationUtil.start()
//                            }
//                        } else if (getLocationType == 1) {
//                            if (!::latlngList.isInitialized) {
//                                latlngList = mutableListOf()
//                            }
//
//                            if (isReadOnly && lat != null && lng != null && lat != 0.0 && lng != 0.0) {
//                                latlngList.clear()
//                                latlngList.add(LatLng(lat!!, lng!!))
//                            }
//
//
//                            AppContext.map.put(Keys.POINT_LIST, latlngList)
//                            if (activity == null) {
//                                "activity 参数未设置".printMsg()
//                            }
//                            activity?.startActivityForResult<BaiduMapMeasureActivity>(1001, Keys.INFO to BaiduMapUtil.POINT, Keys.IS_READ_ONLY to isReadOnly.getOne(1, 0))
//                        }
                    }
                }

                VIEW_TEXT -> {

//                    copyAble?.yes {
//                        llTextline.onLongClick {
//                        }
//                    }
//                    tvValue.onClick(1000) {
//                        if (::actionBlock.isInitialized) {
//                            actionBlock()
//                        }
//                    }


                }


                else -> {
                }
            }


            imgClear.onClick(1000) {

                reset()

            }

            imgCustom.onClick(1000){
                if (::imgCustomBlock.isInitialized) {
                    imgCustomBlock()
                }
            }

            tvCustom.onClick(1000){
                if (::tvCustomBlock.isInitialized) {
                    tvCustomBlock()
                }
            }
        }
    }

    lateinit var timeformatList: MutableList<Int>

    fun setCurrentTime(){
        val time = TimeUtil.getDateToString(TimeUtil.getCurrentStamp(), timeFormatType)
        setValueText(time.self())
    }

    private fun showTimeDialog(
        dialogTitle: String = "请选择",
        showUnit: Boolean = false,
        backNow: Boolean = false,
        chooseText: String = "选择",
        showSecond: Boolean = false
    ) {
        if (mContext == null) return


        if (!::timeformatList.isInitialized) {
            timeformatList = mutableListOf()

            when (getTimeType) {
                0 -> {
                    timeformatList.add(DateTimeConfig.YEAR)
                }
                1 -> {
                    timeformatList.add(DateTimeConfig.YEAR)
                    timeformatList.add(DateTimeConfig.MONTH)
                }
                2 -> {
                    timeformatList.add(DateTimeConfig.YEAR)
                    timeformatList.add(DateTimeConfig.MONTH)
                    timeformatList.add(DateTimeConfig.DAY)
                }
                3 -> {
                    timeformatList.add(DateTimeConfig.YEAR)
                    timeformatList.add(DateTimeConfig.MONTH)
                    timeformatList.add(DateTimeConfig.DAY)
                    timeformatList.add(DateTimeConfig.HOUR)
                }
                4 -> {
                    timeformatList.add(DateTimeConfig.YEAR)
                    timeformatList.add(DateTimeConfig.MONTH)
                    timeformatList.add(DateTimeConfig.DAY)
                    timeformatList.add(DateTimeConfig.HOUR)
                    timeformatList.add(DateTimeConfig.MIN)
                }

            }
            if (showSecond) {
                timeformatList.add(DateTimeConfig.SECOND)
            }
        }

        val build = CardDatePickerDialog.builder(mContext).setTitle(dialogTitle).setDisplayType(timeformatList)
            .setBackGroundModel(CardDatePickerDialog.CUBE).showBackNow(backNow)
            .setPickerLayout(R.layout.layout_date_picker_segmentation).setWrapSelectorWheel(false)
            .setThemeColor(findColor(mContext, R.color.dark_blue_dash)).showDateLabel(showUnit)
            .showFocusDateInfo(false).setOnChoose(chooseText) {

                selectTime = it
                val time = TimeUtil.getDateToString(it, timeFormatType)
                "选择的时间：$time".printMsg()
                setValueText(time.self())

                if (::selectTimeActionBlock.isInitialized) {
                    selectTimeActionBlock(it, time.self())
                }

            }.setOnCancel("取消")

        isNotNullObj(maxTime) { build.setMaxTime(maxTime!!) }
        isNotNullObj(miniTime) { build.setMinTime(miniTime!!) }
        isNotNullObj(defTime) { build.setDefaultTime(defTime!!) }

        build.build().show()

    }

    /**
     * 设置值的文本
     */
    fun setValueText(v: String?) {

        if (v != null && v.equals("null")) {
            this.value = ""
        } else {
            this.value = v
        }

        vb!!.apply {
            when (viewType) {
                VIEW_EIDT_TEXT_NORMAL -> etValue.setText(value)
                VIEW_EIDT_TEXT_AREA -> etValueMultiLine.setText(value)
                VIEW_EIDT_TEXT_NUMBER -> etValueNumber.setText(value)
                VIEW_EIDT_TEXT_NUMBER_ONLY -> etValueNumberOnly.setText(value)
                else -> tvValue.text = value
            }
        }
    }

    /**
     * 设置时间 Long值
     */

    fun setTimeValue(time: Long?, format: String = Config.timeFormat2) {
        if (time == null) {
            setValueText("")
        } else {
            selectTime = time
            val strTime = TimeUtil.getDateToString(time, format)
            setValueText(strTime)
        }
    }

    /**
     * 设置列表已选选项
     */

    fun setSelection(obj: Any) {

        datalist?.run {
            when (obj) {
                is Int -> if (size > obj) selectData = get(obj)
                is String -> {
                    CommonUtil.isNotEmpty(obj).yes {
                        isMultiSelect.yes {
                            val array = obj.cutString()

                            array.forEach { item1 ->
                                forEach { item2 ->

                                    if (CommonUtil.isNotNull(item1) && CommonUtil.isNotNull(item2)) {

                                        if (item1 == item2.key || item1 == item2.value) {
                                            item2.select = true
                                            selectList.add(item2)
                                        }

                                    }
                                }
                            }
                            selectList.toJson().printMsg()


                        }.no {
                            forEach {

                                (CommonUtil.isNotEmpty(it.value) && it.value == obj).yes {
                                    selectData = it
                                }

                                (CommonUtil.isNotEmpty(it.key) && it.key == obj).yes {
                                    selectData = it
                                }

                            }
                        }

                    }


                }
            }

            isMultiSelect.yes {
                vb!!.tvValue.setText(getValueData())

                isNotNullObj(multiAdapter) {
                    multiAdapter!!.notifyDataSetChanged()
                }

            }.no {
                selectData?.run {
                    vb!!.tvValue.setText(getText())
                }
            }

            //如果仍然获取不到对应选项，那么就展示obj字符串
            if (vb!!.tvValue.text.isBlank()) {
                vb!!.tvValue.text = obj as String
            }

        }
    }

    /**
     * 设置是否只读
     */
    fun setValueIsReadOnly(v: Boolean) {
        vb?.apply {
            when (viewType) {
                VIEW_EIDT_TEXT_NORMAL -> etValue.isEnabled = !v
                VIEW_EIDT_TEXT_AREA -> etValueMultiLine.isEnabled = !v
                VIEW_EIDT_TEXT_NUMBER -> etValueNumber.isEnabled = !v
                VIEW_EIDT_TEXT_NUMBER_ONLY -> etValueNumberOnly.isEnabled = !v
                else -> {
                }
            }
            isReadOnly = v
        }

    }

    /**
     * 设置值的颜色
     */
    fun setValueTextColor(color: Int) {
        vb!!.apply {
            valueColor = color
            when (viewType) {
                VIEW_EIDT_TEXT_NORMAL -> etValue.setTextColor(valueColor)
                VIEW_EIDT_TEXT_AREA -> etValueMultiLine.setTextColor(valueColor)
                VIEW_EIDT_TEXT_NUMBER -> etValueNumber.setTextColor(valueColor)
                VIEW_EIDT_TEXT_NUMBER_ONLY -> etValueNumberOnly.setTextColor(valueColor)
                else -> tvValue.setTextColor(valueColor)
            }
        }
    }

    /**
     * 设置输入框提示字颜色
     */
    fun setValueTextHintColor(color: Int) {
        vb?.apply {
            if (color > 0) {
                hintColor = color
                when (viewType) {
                    VIEW_EIDT_TEXT_NORMAL -> etValue.setHintTextColor(hintColor)
                    VIEW_EIDT_TEXT_AREA -> etValueMultiLine.setHintTextColor(hintColor)
                    VIEW_EIDT_TEXT_NUMBER -> etValueNumber.setHintTextColor(hintColor)
                    VIEW_EIDT_TEXT_NUMBER_ONLY -> etValueNumberOnly.setHintTextColor(hintColor)
                    else -> tvValue.setHintTextColor(hintColor)
                }
            }
        }
    }

    fun getKeyData(): String {
        return vb!!.tvKey.text.toString()
    }
    /**
     * 获取已填写好的文本
     */
    fun getValueData(): String = when (viewType) {
        VIEW_EIDT_TEXT_NORMAL -> vb!!.etValue.text.toString()
        VIEW_EIDT_TEXT_AREA -> vb!!.etValueMultiLine.text.toString()
        VIEW_EIDT_TEXT_NUMBER -> vb!!.etValueNumber.text.toString()
        VIEW_EIDT_TEXT_NUMBER_ONLY -> vb!!.etValueNumberOnly.text.toString()
        VIEW_SELECTION_LIST_SINGLE, VIEW_SELECTION_LIST_MULTI -> getSelectKey()
        VIEW_LOCATION -> {
            if (lat == null || lng == null || lat == 0.0 || lng == 0.0) ""
            else "${lat},${lng}"
        }

        VIEW_SELECTION_TIME -> {
            var v = this.value.self()

            isNotNullObj(selectTime) {
                v = TimeUtil.getDateToString(selectTime!!, timeFormatType)
            }

            v
        }
        else -> vb!!.tvValue.text.toString()
    }

    /**
     * 获取已选列表的key
     */
    fun getSelectKey(): String {
        selectList.toJson().printMsg()
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
            if(selectData == null){
                sb.append(vb!!.tvValue.text)
            }else{
                sb.append(selectData?.key.self())
            }

        }
        return sb.toString()
    }

    /**
     * 获取已选列表的值
     */
    fun getSelectValue(): String {
        val sb = StringBuffer()
        val valueText = vb!!.tvValue.text.toString()

        if (valueText.isNotEmpty()) {
            sb.append(valueText.self())
        } else {
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
        }

        return sb.toString()
    }


    /**
     * 判断是否已经填了信息
     */
    fun isFilledData(tipMsg: String = "", isNeetTip: Boolean = true): Boolean {

        val flag = CommonUtil.isNotEmpty(getValueData())

        if (isNeetTip && !flag) {
            Toast.makeText(
                context, CommonUtil.isNotEmpty(tipMsg).getOne(tipMsg, hint), Toast.LENGTH_SHORT
            ).show()
        }

        if (flag) {
            return true
        }
        return false
    }


    /**
     * 设置标题字样
     */
    fun setKeyText(v: String?) {

        if (v != null && v.equals("null")) {
            this.value = ""
        } else {
            this.value = v
        }
        vb!!.tvKey.text = value
    }

    /**
     *获取值的视图
     */
    fun getValueView(): Any = when (viewType) {
        VIEW_EIDT_TEXT_NORMAL -> vb!!.etValue
        VIEW_EIDT_TEXT_AREA -> vb!!.etValueMultiLine
        VIEW_EIDT_TEXT_NUMBER -> vb!!.etValueNumber
        VIEW_EIDT_TEXT_NUMBER_ONLY -> vb!!.etValueNumberOnly
        else -> vb!!.tvValue
    }

    /**
     *设置标题
     */
    fun setTitle(title: String) {
        this.key = key
        vb!!.tvKey.text = title

    }

    /**
     *设置输入框提示字
     */
    fun setTextHint(h: String) {
        this.hint = h
        when (viewType) {
            VIEW_EIDT_TEXT_NORMAL -> vb!!.etValue.hint = h
            VIEW_EIDT_TEXT_AREA -> vb!!.etValueMultiLine.hint = h
            VIEW_EIDT_TEXT_NUMBER -> vb!!.etValueNumber.hint = h
            VIEW_EIDT_TEXT_NUMBER_ONLY -> vb!!.etValueNumberOnly.hint = h
            else -> vb!!.tvValue.hint = h
        }

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

    /**
     *设置列表选项数据
     */
    fun setNewLabelArray(strArray: String, isUpdate: Boolean = true) {

        if (CommonUtil.isNotEmpty(strArray)) {
            datalist = mutableListOf()
            val textList = strArray!!.cutString(",")

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
    fun updateList(list: MutableList<SelectionListModel>) {
        datalist = list
        isMultiSelect.yes {
            multiAdapter?.update(datalist)
        }.no {
            listDialog = SingleSelectListDialog(context, datalist!!){obj, position ->
                selectData = obj as SelectionListModel
                setValueText(selectData?.value.self())

                if (!isMultiSelect) {
                    selectPosition = position
                    //单选监听
                    listener?.dialogItemClick(obj, position, -1)

                    if (::selectionActionBlock.isInitialized) {

                        val list = mutableListOf<SelectionListModel>()
                        list.add(selectData!!)
                        selectionActionBlock(list)
                    }
                    if (::selectionActionBlock2.isInitialized) {
                        selectionActionBlock2(position, selectData!!)
                    }
                }
            }
        }
    }

    /**
     *列表选项监听
     */
    override fun dialogItemClick(obj: Any?, position: Int, flag: Int) {

        // 单选 item项 点击监听
        obj?.let {
            selectData = obj as SelectionListModel
            setValueText(selectData?.value.self())
        }


        if (!isMultiSelect) {
            selectPosition = position
            //单选监听
            listener?.dialogItemClick(obj, position, flag)

            if (::selectionActionBlock.isInitialized) {

                val list = mutableListOf<SelectionListModel>()
                list.add(selectData!!)
                selectionActionBlock(list)
            }
            if (::selectionActionBlock2.isInitialized) {
                selectionActionBlock2(position, selectData!!)
            }

        }

    }

    /**
     *展示列表选项 对话框
     */
    fun showSelectionList() {

        datalist?.let {

            if (it.isEmpty()) {
                CommonUtil.tip(context, "暂无可选项")
                return
            }

            if (isMultiSelect) {
                //多选
                if (multiAdapter == null || multiListDialog == null) {
                    multiAdapter = MultiSelectionAdapter<SelectionListModel>(context)
                    multiListDialog = MultiListDialog(
                        context, datalist!!, multiAdapter!!, isCenter
                    ) { text, list ->
                        //选完监听回调
                        setValueText(text)
                        selectList = list
                    }
                }
                multiListDialog?.show()

            } else {
//                单选
                updateList(datalist!!)

                listDialog?.run {
                    setHeightScale(heighScale)
                    if (!isShowing) show()
                }
            }

        }
    }

    //清空数据
    fun reset() {
        when (viewType) {
            VIEW_SELECTION_LIST_SINGLE, VIEW_SELECTION_LIST_MULTI -> {
                selectList.clear()
                selectData = null
            }

            VIEW_SELECTION_TIME -> selectTime = null
            VIEW_LOCATION -> {
                lat = null//纬度
                lng = null//经度
                address = ""//地址
                province = ""//省
                city = ""//市
                district = ""//区县
                town = ""//乡镇
                village = ""//村庄
            }

            else -> {
            }
        }

        setValueText("")
    }


    fun getPointFromMapBack(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == 100) {
            val map = AppContext.map[Keys.pageData] as MutableMap<String, Any>
//            latlngList = map["list"] as MutableList<LatLng>
//            map["addr"]?.let {
//                address = it as String
//                setValueText(address)
//            }

//            CommonUtil.matchList(latlngList).yes {
//                val item = latlngList.last()
//                lat = item.latitude
//                lng = item.longitude
//            }
        }
    }

    fun showAddress(latInfo: Double, lngInfo: Double) {
        this.lat = latInfo
        this.lng = lngInfo

//        val mCoder = GeoCoder.newInstance();
//        mCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
//            override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {
//            }
//
//            override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
//                address = ""
//                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
//                    "没有检测到地址".printMsg()
//
//                } else {
//                    reverseGeoCodeResult?.let {
//                        address = it.address.self()
//                        setValueText(address)
//                    }
//                }
//            }
//
//        })

    }


    fun getTextLineView(): LinearLayout? = vb?.llTextline

    fun setGrayStyle() {

        vb?.apply {
//            llTextline.setBackgroundColor(findColor(context,R.color.bg_gray))
            tvKey.setTextColor(findColor(context, com.jameni.basepage_lib.R.color.txt_gray))
            tvValue.setTextColor(findColor(context, com.jameni.basepage_lib.R.color.txt_gray))
        }

    }

    fun setHighLight() {
        vb?.apply {
            llTextline.setBackgroundColor(findColor(context, R.color.form_highlight))
            etValueMultiLine.setBackgroundColor(findColor(context, R.color.white))
            etValue.setBackgroundColor(findColor(context, R.color.white))
            tvValue.setBackgroundColor(findColor(context, R.color.white))
        }
    }



}