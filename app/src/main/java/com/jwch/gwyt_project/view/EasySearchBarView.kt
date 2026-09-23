package com.jwch.gwyt_project.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.gson.reflect.TypeToken
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.HistroyKeywordAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewEasySeacrhBarBinding
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getObjByType
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isEmpty
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.SoftUtil
import com.qmuiteam.qmui.kotlin.onClick
import java.util.Calendar


class EasySearchBarView : LinearLayout {

    val layoutId: Int = R.layout.view_easy_seacrh_bar
    var vb: ViewEasySeacrhBarBinding? = null

    val keyboard = SoftUtil()

    lateinit var actionBlock: (String, Int, Int, Int, String, String, String, String) -> Unit

    var historyKeywordList : MutableList<String>? = null

    lateinit var accountAdapter : HistroyKeywordAdapter

    // 筛选条件
    var isProblem: Int = 0  // 0:全部, 1:是, 2:否, 3:待复核
    var isFinished: Int = 0 // 0:全部, 1:是, 2:否
    var year: Int = Calendar.getInstance().get(Calendar.YEAR)  // 默认选中今年
    var problemType: String = ""  // 问题类型：空字符串为全部，其他为具体类型（乱建/乱占/乱堆/乱采/其他）

    // 行政区划筛选（使用名称）
    var cityName: String = ""    // 市级名称
    var countyName: String = ""  // 区县名称
    var townName: String = ""    // 乡镇名称

    var isExpanded = false

    // 年份列表
    private val yearList = mutableListOf<String>()

    // 问题类型列表
    private val problemTypeList = mutableListOf<String>()

    // 行政区划数据
    private var cityList: MutableList<DistrictsInfo>? = null
    private var countyList: MutableList<DistrictsInfo>? = null
    private var townList: MutableList<DistrictsInfo>? = null

    // 当前选中的行政区划
    private var selectedCity: DistrictsInfo? = null
    private var selectedCounty: DistrictsInfo? = null
    private var selectedTown: DistrictsInfo? = null

    // 父级行政区划（用于初始化）
    private var provinceItem: DistrictsInfo? = null


    fun initView(context: Context?) {
        if(context !=null){
            mContext = context
            val contentView: View = LayoutInflater.from(mContext).inflate(layoutId, null)
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params
            vb = ViewEasySeacrhBarBinding.bind(contentView)
            vb?.apply {
                addView(root)
            }
        }
    }


    fun initHistoryList(){

        val historyJson = getKV(Keys.HISTORY_SEARCH_KEYWORD,"")
        "HISTORY_SEARCH_KEYWORD = $historyJson".printMsg()
        historyJson.isNotEmpty().yes {
            historyKeywordList = getObjByType(historyJson, object : TypeToken<MutableList<String>>() {}.type)
        }.no {
            historyKeywordList = mutableListOf()
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


    /**
     * 切换更多筛选条件的显隐
     */
    fun toggleFilter() {
        isExpanded = !vb!!.llFilterContainer.isShown
        if (vb!!.llFilterContainer.isShown) {
            vb!!.llFilterContainer.gone()
        } else {
            vb!!.llFilterContainer.show()
        }
        updateToggleIcon()
    }

    /**
     * 根据展开状态更新下拉箭头方向
     */
    private fun updateToggleIcon() {
        vb?.let {
            it.ivToggle.setImageResource(
                if (it.llFilterContainer.isShown) R.mipmap.icon_arrow_up else R.mipmap.icon_arrow_down
            )
        }
    }

    fun initViewData() {

        // 初始化年份列表
        initYearList()

        // 初始化问题类型列表
        initProblemTypeList()

//         初始化行政区划筛选（暂时隐藏）
         initDistrictFilter()


        // 下拉箭头点击：切换更多筛选条件（llFilterContainer）的显隐
        // （注意：不给 llGlobalSearch 整体加点击，否则点击输入框会展开筛选）
        vb!!.ivToggle.onClick {
            toggleFilter()
        }

        // 搜索按钮点击事件
        vb!!.tvSearch.onClick {
            performSearch("请输入编号")
        }

        //软键盘回车
        vb!!.etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                performSearch("请输入编号")
                return@OnKeyListener true
            }
            false
        })

        // 限制输入：仅允许数字和英文字母，禁止特殊符号、中文、表情
        vb!!.etSearch.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null // 允许删除操作
            val allowed = source.filter { it in '0'..'9' || it in 'a'..'z' || it in 'A'..'Z' }
            if (allowed.isNotEmpty()) allowed.toString() else ""
        })

        vb!!.tvClear.onClick {
            vb!!.etSearch.setText("")
        }


        // 是否问题筛选 - chips点击事件
        val problemChips = listOf(vb!!.tvProblemAll, vb!!.tvProblemYes, vb!!.tvProblemNo, vb!!.tvProblemPending)
        vb!!.tvProblemAll.isSelected = true // 默认选中全部
        problemChips.forEachIndexed { index, chip ->
            chip.onClick {
                // 更新选中状态
                problemChips.forEach { it.isSelected = false }
                chip.isSelected = true
                isProblem = index
            }
        }

        // 是否办结筛选 - chips点击事件
        val finishedChips = listOf(vb!!.tvFinishedAll, vb!!.tvFinishedYes, vb!!.tvFinishedNo)
        vb!!.tvFinishedAll.isSelected = true // 默认选中全部
        finishedChips.forEachIndexed { index, chip ->
            chip.onClick {
                // 更新选中状态
                finishedChips.forEach { it.isSelected = false }
                chip.isSelected = true
                isFinished = index
            }
        }

        // 清空筛选按钮
        vb!!.tvClearFilters.onClick {
            resetFilters()
            tip(context, "已清空所有筛选条件")
        }

        // 确定按钮（作用和搜索按钮相同）
        vb!!.tvConfirm.onClick {
            performSearch("请选择筛选条件")
        }

        // 默认选中今年
        vb!!.tvYearValue.text = year.toString()

        // 年份点击选择
        vb!!.tvYearValue.onClick {
            showYearSelectionDialog()
        }

        // 问题类型点击选择
        vb!!.tvProblemTypeValue.onClick {
            showProblemTypeSelectionDialog()
        }

        // 市级点击选择
        vb!!.tvCityValue.onClick {
            showCitySelectionDialog()
        }

        // 区县点击选择
        vb!!.tvCountyValue.onClick {
            showCountySelectionDialog()
        }

        // 乡镇点击选择
        vb!!.tvTownValue.onClick {
            showTownSelectionDialog()
        }
    }

    /**
     * 初始化年份选择列表
     * 第一项为"全部"，之后从当前年份倒序到2022年
     */
    private fun initYearList() {
        // 获取当前年份
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startYear = 2022

        // 构建年份列表
        yearList.clear()

        // 第一项为"全部"（对应 year=0）
        yearList.add("全部")

        // 从当前年份倒序添加到2022年
        for (y in currentYear downTo startYear) {
            yearList.add(y.toString())
        }
    }

    /**
     * 显示年份选择对话框
     */
    private fun showYearSelectionDialog() {
        AlertDialog.Builder(mContext)
            .setTitle("选择年份")
            .setItems(yearList.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    // 选择"全部"（year=0，不参与筛选）
                    year = 0
                    vb!!.tvYearValue.text = "全部"
                    "年份筛选: 全部".printMsg()
                } else {
                    year = yearList[which].toInt()
                    vb!!.tvYearValue.text = yearList[which]
                    "年份筛选: $year".printMsg()
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 初始化问题类型选择列表
     * 选项：全部、乱建、乱占、乱堆、乱采、其他
     */
    private fun initProblemTypeList() {
        // 构建问题类型列表
        problemTypeList.clear()
        problemTypeList.add("全部")
        problemTypeList.add("乱建")
        problemTypeList.add("乱占")
        problemTypeList.add("乱堆")
        problemTypeList.add("乱采")
        problemTypeList.add("其他")
    }

    /**
     * 显示问题类型选择对话框
     */
    private fun showProblemTypeSelectionDialog() {
        AlertDialog.Builder(mContext)
            .setTitle("选择问题类型")
            .setItems(problemTypeList.toTypedArray()) { dialog, which ->
                problemType = if (which == 0) {
                    "" // 全部
                } else {
                    problemTypeList[which] // 具体类型
                }
                vb!!.tvProblemTypeValue.text = problemTypeList[which]
                "问题类型筛选: $problemType".printMsg()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 初始化行政区划筛选
     * 根据用户级别显示不同的筛选层级
     */
    private fun initDistrictFilter() {
        val userLevel = Config.userLevel
        "用户级别: $userLevel, AreaCode: ${Config.AreaCode}".printMsg()

        // 获取当前用户所属行政区划
        val currentUserDistrict = AppContext.app.districtHelper.queryDistrictByCode(Config.AreaCode)
        if (currentUserDistrict == null) {
            "无法获取当前用户行政区划".printMsg()
            return
        }

        when (userLevel) {
            Config.USER_LEVEL_PROVINCE -> {
                // 省级账号：显示市级、区县、乡镇
                vb!!.llCityFilter.visiable(true)
                vb!!.llCountyFilter.visiable(true)
                vb!!.llTownFilter.visiable(true)

                // 获取省级信息，用于获取市级列表
                provinceItem = currentUserDistrict
                cityList = AppContext.app.districtHelper.getNextLevelDistricts(currentUserDistrict.distCode)

                // 初始化显示
                vb!!.tvCityValue.text = "全部市"
                vb!!.tvCountyValue.text = "全部县"
                vb!!.tvTownValue.text = "全部镇"
            }
            Config.USER_LEVEL_CITY -> {
                // 市级账号：显示区县、乡镇
                vb!!.llCityFilter.visiable(false)
                vb!!.llCountyFilter.visiable(true)
                vb!!.llTownFilter.visiable(true)

                // 当前用户就是市级，设置市级名称并初始化区县列表
                selectedCity = currentUserDistrict
                cityName = currentUserDistrict.distName
                countyList = AppContext.app.districtHelper.getNextLevelDistricts(currentUserDistrict.distCode)

                // 显示市级值（不可选择）
                vb!!.tvCityValue.text = truncateText(cityName)

                // 初始化区县和乡镇显示
                vb!!.tvCountyValue.text = "全部县"
                vb!!.tvTownValue.text = "全部镇"
            }
            Config.USER_LEVEL_COUNTY -> {
                // 区县级账号：只显示乡镇
                vb!!.llCityFilter.visiable(false)
                vb!!.llCountyFilter.visiable(false)
                vb!!.llTownFilter.visiable(true)

                // 当前用户就是区县级，设置区县名称并初始化乡镇列表
                // 需要先获取市级信息
                val parentDistricts = AppContext.app.districtHelper.getParentDistricts(currentUserDistrict.distCode)
                if (parentDistricts.isNotEmpty()) {
                    selectedCity = parentDistricts.find { it.distLevel == DistrictsInfo.LEVEL_CITY }
                    cityName = selectedCity?.distName ?: ""
                    vb!!.tvCityValue.text = truncateText(cityName)
                }
                selectedCounty = currentUserDistrict
                countyName = currentUserDistrict.distName
                townList = AppContext.app.districtHelper.getNextLevelDistricts(currentUserDistrict.distCode)

                // 显示市级和区县值（不可选择）
                vb!!.tvCountyValue.text = truncateText(countyName)

                // 初始化乡镇显示
                vb!!.tvTownValue.text = "全部镇"
            }
            else -> {
                // 其他级别不显示行政区划筛选
                vb!!.llCityFilter.visiable(false)
                vb!!.llCountyFilter.visiable(false)
                vb!!.llTownFilter.visiable(false)
            }
        }
    }

    /**
     * 截断文本，最多显示3个字
     */
    private fun truncateText(text: String): String {
        return if (text.length > 3) {
            text.substring(0, 3) + "…"
        } else {
            text
        }
    }

    /**
     * 显示市级选择对话框
     */
    private fun showCitySelectionDialog() {
        if (!CommonUtil.matchList(cityList)) {
            tip(mContext, "暂无市级数据")
            return
        }

        val items = mutableListOf("全部")
        cityList!!.forEach { items.add(it.distName) }

        AlertDialog.Builder(mContext)
            .setTitle("选择市级")
            .setItems(items.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    // 选择全部
                    selectedCity = null
                    cityName = ""
                    vb!!.tvCityValue.text = "全部"
                    // 清空区县和乡镇
                    countyList = null
                    selectedCounty = null
                    countyName = ""
                    vb!!.tvCountyValue.text = "全部"
                    townList = null
                    selectedTown = null
                    townName = ""
                    vb!!.tvTownValue.text = "全部"
                } else {
                    // 选择具体市级
                    selectedCity = cityList!![which - 1]
                    cityName = selectedCity!!.distName
                    vb!!.tvCityValue.text = truncateText(cityName)
                    "选择市级: $cityName".printMsg()
                    // 初始化区县列表
                    countyList = AppContext.app.districtHelper.getNextLevelDistricts(selectedCity!!.distCode)
                    countyName = ""
                    vb!!.tvCountyValue.text = "全部"
                    townList = null
                    townName = ""
                    vb!!.tvTownValue.text = "全部"
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 显示区县选择对话框
     */
    private fun showCountySelectionDialog() {
        if (!CommonUtil.matchList(countyList)) {
            tip(mContext, "请先选择市级")
            return
        }

        val items = mutableListOf("全部")
        countyList!!.forEach { items.add(it.distName) }

        AlertDialog.Builder(mContext)
            .setTitle("选择区县")
            .setItems(items.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    // 选择全部
                    selectedCounty = null
                    countyName = ""
                    vb!!.tvCountyValue.text = "全部"
                    // 清空乡镇
                    townList = null
                    selectedTown = null
                    townName = ""
                    vb!!.tvTownValue.text = "全部"
                } else {
                    // 选择具体区县
                    selectedCounty = countyList!![which - 1]
                    countyName = selectedCounty!!.distName
                    vb!!.tvCountyValue.text = truncateText(countyName)
                    "选择区县: $countyName".printMsg()
                    // 初始化乡镇列表
                    townList = AppContext.app.districtHelper.getNextLevelDistricts(selectedCounty!!.distCode)
                    townName = ""
                    vb!!.tvTownValue.text = "全部"
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 显示乡镇选择对话框
     */
    private fun showTownSelectionDialog() {
        if (!CommonUtil.matchList(townList)) {
            tip(mContext, "请先选择区县")
            return
        }

        val items = mutableListOf("全部")
        townList!!.forEach { items.add(it.distName) }

        AlertDialog.Builder(mContext)
            .setTitle("选择乡镇")
            .setItems(items.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    // 选择全部
                    selectedTown = null
                    townName = ""
                    vb!!.tvTownValue.text = "全部"
                } else {
                    // 选择具体乡镇
                    selectedTown = townList!![which - 1]
                    townName = selectedTown!!.distName
                    vb!!.tvTownValue.text = truncateText(townName)
                    "选择乡镇: $townName".printMsg()
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 判断是否有任意筛选参数
     */
    private fun hasAnyFilter(): Boolean {
        return isProblem != 0 ||
               isFinished != 0 ||
               year != 0 ||
               problemType.isNotBlank() ||
               cityName.isNotBlank() ||
               countyName.isNotBlank() ||
               townName.isNotBlank()
    }

    /**
     * 执行搜索操作
     */
    private fun performSearch(str: String) {
        val hasFilters = hasAnyFilter()
        if (vb!!.etSearch.isEmpty() && !hasFilters) {
            ToastUtils.show(str)
            return
        }
        keyboard.hideKeyboard(mActivity)
        val keyword = vb!!.etSearch.text.toString()
        if (keyword.isNotBlank()) {
            saveData(keyword)
        }
        actionBlock(keyword, isProblem, isFinished, year, cityName, countyName, townName, problemType)
    }

    fun initView(activity :Activity, str :String = ""){
        this.mActivity = activity
        setSearchBarHint(str)
        initHistoryList()
    }

    fun setSearchBarHint(str: String) {
        if (str.isNotBlank()) {
            vb!!.etSearch.hint = str
        }
    }


    fun saveData(kw :String){


        historyKeywordList!!.removeIf { it == kw }

        if(historyKeywordList!!.size > 5){
            historyKeywordList!!.removeAt(historyKeywordList!!.size -1)
        }
        historyKeywordList?.add(0, kw)
        saveKV(Keys.HISTORY_SEARCH_KEYWORD, historyKeywordList.toJson())
    }


    fun isEnable(enable :Boolean){
        vb!!.etSearch.isEnabled = enable
    }

    // 重置筛选条件
    fun resetFilters() {
        isProblem = 0
        isFinished = 0
        year = Calendar.getInstance().get(Calendar.YEAR)
        problemType = ""
        cityName = ""
        countyName = ""
        townName = ""
        selectedCity = null
        selectedCounty = null
        selectedTown = null
        // 重置是否问题chips
        vb!!.tvProblemAll.isSelected = true
        vb!!.tvProblemYes.isSelected = false
        vb!!.tvProblemNo.isSelected = false
        vb!!.tvProblemPending.isSelected = false
        // 重置是否办结chips
        vb!!.tvFinishedAll.isSelected = true
        vb!!.tvFinishedYes.isSelected = false
        vb!!.tvFinishedNo.isSelected = false
        vb!!.tvProblemTypeValue.text = "全部"
        vb!!.tvYearValue.text = Calendar.getInstance().get(Calendar.YEAR).toString()

        // 根据用户级别重置行政区划显示
        val userLevel = Config.userLevel
        when (userLevel) {
            Config.USER_LEVEL_PROVINCE -> {
                vb!!.tvCityValue.text = "全部市"
                vb!!.tvCountyValue.text = "全部县"
                vb!!.tvTownValue.text = "全部镇"
                countyList = null
                townList = null
            }
            Config.USER_LEVEL_CITY -> {
                // 市级不变，只重置区县和乡镇
                vb!!.tvCountyValue.text = "全部县"
                vb!!.tvTownValue.text = "全部镇"
                townList = null
                // 重新获取区县列表
                val currentUserDistrict = AppContext.app.districtHelper.queryDistrictByCode(Config.AreaCode)
                if (currentUserDistrict != null) {
                    countyList = AppContext.app.districtHelper.getNextLevelDistricts(currentUserDistrict.distCode)
                }
            }
            Config.USER_LEVEL_COUNTY -> {
                // 区县不变，只重置乡镇
                vb!!.tvTownValue.text = "全部镇"
                // 重新获取乡镇列表
                val currentUserDistrict = AppContext.app.districtHelper.queryDistrictByCode(Config.AreaCode)
                if (currentUserDistrict != null) {
                    townList = AppContext.app.districtHelper.getNextLevelDistricts(currentUserDistrict.distCode)
                }
            }
        }
    }

}