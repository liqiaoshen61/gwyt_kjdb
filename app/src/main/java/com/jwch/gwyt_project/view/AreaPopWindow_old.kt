package com.jwch.gwyt_project.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.AreaAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewAreaPopWindowBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.MapEvent
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import java.text.Collator
import java.util.Locale


class AreaPopWindow_old(context: Context?, width: Int, height: Int) : PopupWindow(context) {

    constructor(context: Context?) : this(
        context, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ) {
    }

    private val context: Context?
    private val view: View

    lateinit var actionBlock: (DistrictsInfo) -> Unit

    var vb: ViewAreaPopWindowBinding? = null

    var dataList: MutableList<DistrictsInfo>? = null//行政区划数据
    var selectData: DistrictsInfo? = null //当前选中的行政区

    private var areaAdapter: AreaAdapter? = null
    var initCode = Config.AreaCode
    var maxLevel = DistrictsInfo.LEVEL_COUNTY  //行政区划最大级别设置
    var minLevel = DistrictsInfo.LEVEL_TOWN  //行政区划最小级别设置

    var isProvinceLevel = false

    init {
        this.context = context
        setWidth(width)
        setHeight(height)
        isFocusable = true
        isOutsideTouchable = true
        isTouchable = true
        setBackgroundDrawable(BitmapDrawable())
        view = LayoutInflater.from(context).inflate(R.layout.view_area_pop_window, null)

        vb = ViewAreaPopWindowBinding.bind(view)
        contentView = vb!!.root

        contentView = view

        isProvinceLevel = initCode == "350000"

        initList()
        initData(initCode)

        initViewListener()

    }

    fun initList() {
        vb!!.lvMain.layoutManager = GridLayoutManager(context, 3)
        areaAdapter = AreaAdapter()
        vb!!.lvMain.adapter = areaAdapter
        vb!!.lvMain.gone()

    }

    fun initData(distCode: String) {

        val item = db.queryDistrictByCode(distCode)
        if (item != null) {
            maxLevel = item.distLevel
            selectData = item
            displayCurrentArea()
            dataList = getChildDistrictList(selectData!!)
            areaAdapter!!.update(dataList)

            vb!!.lvMain.visiable(CommonUtil.matchList(dataList))
        }

        areaAdapter!!.setOnItemClickListener { adapter, view, position ->

            selectData = adapter.getItem(position) as DistrictsInfo
            if (::actionBlock.isInitialized) {
                actionBlock(selectData!!)
            }


            displayCurrentArea()

            if(selectData!!.distLevel < minLevel){
                dataList = getChildDistrictList(selectData!!)
                CommonUtil.matchList(dataList).yes {
                    vb!!.lvMain.show()
                }.no {
                    vb!!.lvMain.gone()
                }
                areaAdapter!!.update(dataList)
            }

            //判断返回按钮是否展示
//            vb.tvAreaBack.visiable(selectData!!.distLevel != maxLevel)

            //查询gdb 绘制行政区划范围
            val map = mutableMapOf<String, String>()
            map.put("name", selectData!!.distName)
            map.put("code", getDistrictCode(selectData!!))
            when (selectData!!.distLevel) {
                DistrictsInfo.LEVEL_PROVINCE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_PROVINCE, map, selectData!!))
                DistrictsInfo.LEVEL_CITY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_CITY, map, selectData, false))
                DistrictsInfo.LEVEL_COUNTY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_AREA, map, selectData, false))
                DistrictsInfo.LEVEL_TOWN -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_COUNTY, map, selectData, false))
                DistrictsInfo.LEVEL_VILLAGE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_VILLAGE, map, selectData, false))
            }

        }
    }

    fun initViewListener(){
        vb!!.llProvince.onClick {

            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_PROVINCE){
                val name = vb!!.tvProvince.text.toString()
                queryDistName(name)

                if (::actionBlock.isInitialized) {
                    actionBlock(selectData!!)
                }
            }
        }

        vb!!.llCity.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_CITY){
                val name = vb!!.tvCity.text.toString()
                queryDistName(name)

                if (::actionBlock.isInitialized) {
                    actionBlock(selectData!!)
                }
            }
        }
        vb!!.llCounty.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_COUNTY){
                val name = vb!!.tvCounty.text.toString()
                queryDistName(name)

                if (::actionBlock.isInitialized) {
                    actionBlock(selectData!!)
                }
            }

        }
        vb!!.llTown.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_TOWN){
                val name = vb!!.tvTown.text.toString()
                queryDistName(name)

                if (::actionBlock.isInitialized) {
                    actionBlock(selectData!!)
                }
            }

        }
    }

    fun queryDistName(name :String){
        val item = db.queryDistrictByDistName(name)
        if (item != null) {
            selectData = item
            displayCurrentArea()
            dataList = getChildDistrictList(selectData!!)
            areaAdapter!!.update(dataList)
            vb!!.lvMain.visiable(CommonUtil.matchList(dataList))

            //查询gdb 绘制行政区划范围
            val map = mutableMapOf<String, String>()
            map.put("name", selectData!!.distName)
            map.put("code", getDistrictCode(selectData!!))
            when (selectData!!.distLevel) {
                DistrictsInfo.LEVEL_PROVINCE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_PROVINCE, map, selectData!!,false))
                DistrictsInfo.LEVEL_CITY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_CITY, map, selectData, false))
                DistrictsInfo.LEVEL_COUNTY -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_AREA, map, selectData, false))
                DistrictsInfo.LEVEL_TOWN -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_COUNTY, map, selectData, false))
                DistrictsInfo.LEVEL_VILLAGE -> EventBus.getDefault().post(MapEvent(MapEvent.QUERY_AREA_VILLAGE, map, selectData, false))
            }
        }
    }

    fun displayCurrentArea(){

        when (selectData!!.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> {
                vb!!.tvProvince.text = selectData!!.distName
                vb!!.tvCity.text = ""
                vb!!.tvCounty.text = ""
                vb!!.tvTown.text = ""

                vb!!.ivProvince.show()
                vb!!.ivCity.gone()
                vb!!.ivCounty.gone()
                vb!!.ivTown.gone()

                vb!!.llProvince.show()
                vb!!.llCity.gone()
                vb!!.llCounty.gone()
                vb!!.llTown.gone()
            }
            DistrictsInfo.LEVEL_CITY -> {
                vb!!.tvCity.text = selectData!!.distName
                vb!!.tvCounty.text = ""
                vb!!.tvTown.text = ""

                vb!!.ivProvince.gone()
                vb!!.ivCity.show()
                vb!!.ivCounty.gone()
                vb!!.ivTown.gone()


                vb!!.llProvince.visiable(Config.userLevel <= Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.gone()
                vb!!.llTown.gone()
            }
            DistrictsInfo.LEVEL_COUNTY ->{
                vb!!.tvCounty.text = selectData!!.distName
                vb!!.tvTown.text = ""

                vb!!.ivProvince.gone()
                vb!!.ivCity.gone()
                vb!!.ivCounty.show()
                vb!!.ivTown.gone()

                vb!!.llProvince.visiable(Config.userLevel <=  Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.visiable(Config.userLevel <= Config.USER_LEVEL_COUNTY)
                vb!!.llTown.gone()
            }
            DistrictsInfo.LEVEL_TOWN -> {
                vb!!.tvTown.text = selectData!!.distName

                vb!!.ivProvince.gone()
                vb!!.ivCity.gone()
                vb!!.ivCounty.gone()
                vb!!.ivTown.show()

                vb!!.llProvince.visiable(Config.userLevel <=  Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.visiable(Config.userLevel <= Config.USER_LEVEL_COUNTY)
                vb!!.llTown.show()
            }
            DistrictsInfo.LEVEL_VILLAGE -> {

            }
        }
    }


    fun getChildDistrictList(model: DistrictsInfo): MutableList<DistrictsInfo>? {
        var districtList: MutableList<DistrictsInfo>? = null

        val nextLevel = model.distLevel + 1
        val nextList = db.queryDistrictListByLevel(nextLevel)

        val formatCode = getDistrictCode(model)
        CommonUtil.matchList(nextList).yes {
            districtList = nextList?.filter { it.distCode.startsWith(formatCode) } as MutableList<DistrictsInfo>
        }
        //拼音排序
        sortChineseList(districtList)
        return districtList
    }

    // 排序函数
    fun sortChineseList(list: MutableList<DistrictsInfo>?) {
        CommonUtil.matchList(list).yes {
            val collator = Collator.getInstance(Locale.CHINA) // 使用中文排序规则
            list!!.sortWith { o1, o2 ->
                collator.compare(o1.distName, o2.distName)
            }
        }
    }

    /**
     * 规范化 行政区code
     */
    private fun getDistrictCode(model: DistrictsInfo): String {
        var code = ""
        when (model.distLevel) {
            DistrictsInfo.LEVEL_PROVINCE -> code = model.distCode.substring(0, 2) //省 2位
            DistrictsInfo.LEVEL_CITY -> code = model.distCode.substring(0, 4) //市 4位
            DistrictsInfo.LEVEL_COUNTY -> code = model.distCode.substring(0, 6) //县 6位
            DistrictsInfo.LEVEL_TOWN -> code = model.distCode.substring(0, 9) //乡镇 9位
            DistrictsInfo.LEVEL_VILLAGE -> code = model.distCode.substring(0, 12) //村 12位
        }
        return code
    }


}

