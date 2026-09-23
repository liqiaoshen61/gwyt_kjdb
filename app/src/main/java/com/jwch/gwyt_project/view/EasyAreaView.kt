package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.reflect.TypeToken
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.CommonUtil.tip
import com.jwch.gwyt_project.Info.DistrictsInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.AreaAdapter
import com.jwch.gwyt_project.adapter.HistroyKeywordAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewEasyAreaBinding
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getObjByType
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.isEmpty
import com.jwch.gwyt_project.ext.isShow
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.toJson
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.MapEvent
import com.jwch.gwyt_project.util.GetJsonUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.RegionConfigManager
import com.jwch.gwyt_project.util.SoftUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


class EasyAreaView : LinearLayout {

    val layoutId: Int = R.layout.view_easy_area
    var vb: ViewEasyAreaBinding? = null

    lateinit var actionBlock: (String) -> Unit


    var dataList: MutableList<DistrictsInfo>? = null//行政区划数据
    var selectData: DistrictsInfo? = null //当前选中的行政区

    private var areaAdapter: AreaAdapter? = null
    var maxLevel = DistrictsInfo.LEVEL_COUNTY  //行政区划最大级别设置
    var minLevel = DistrictsInfo.LEVEL_VILLAGE  //行政区划最小级别设置

    var firstClick = true



    fun initView(context: Context?) {
//        EventBus.getDefault().register(this)
        if(context !=null){
            mContext = context
            val contentView: View = LayoutInflater.from(mContext).inflate(layoutId, null)
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params
            vb = ViewEasyAreaBinding.bind(contentView)
            vb?.apply {
                addView(root)
            }

            initList()
            initData(Config.AreaCode)
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




    fun initList() {
        vb!!.lvMain.layoutManager = GridLayoutManager(context, 3)
        areaAdapter = AreaAdapter()
        vb!!.lvMain.adapter = areaAdapter
        vb!!.lvMain.gone()
    }

    fun initData(distCode: String) {

        val item = AppContext.app.districtHelper.queryDistrictByCode(distCode)
        if (item != null) {
            maxLevel = item.distLevel
            selectData = item
            displayCurrentArea()

            dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
            areaAdapter!!.update(dataList)

            vb!!.lvMain.visiable(CommonUtil.matchList(dataList))
        }

        areaAdapter!!.setOnItemClickListener { adapter, view, position ->

            selectData = adapter.getItem(position) as DistrictsInfo
            vb?.tvArea?.text = selectData?.distName


            displayCurrentArea()

            if(selectData!!.distLevel < minLevel){
                dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
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


    fun initViewData() {

        vb!!.llEasyAreaView.onClick {}


        vb!!.llArea.onClick {
            if(firstClick){
                val regionInfoJson = GetJsonUtil.getJsonFromFile(Config.APPDB_PATH +"region_info.txt").self().trim()
                val regionInfo = try {
                    org.json.JSONObject(regionInfoJson).optString("region", "")
                } catch (e: Exception) {
                    regionInfoJson
                }
                RegionConfigManager.setRegion(regionInfo)
                initData(Config.AreaCode)
                firstClick = false
            }

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

        vb!!.llProvince.onClick {

            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_PROVINCE){
                val name = vb!!.tvProvince.text.toString()
                queryDistName(name)

                vb?.tvArea?.text = name
            }
        }

        vb!!.llCity.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_CITY){
                val name = vb!!.tvCity.text.toString()
                queryDistName(name)

                vb?.tvArea?.text = name
            }
        }
        vb!!.llCounty.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_COUNTY){
                val name = vb!!.tvCounty.text.toString()
                queryDistName(name)

                vb?.tvArea?.text = name
            }

        }
        vb!!.llTown.onClick {
            if(selectData!!.distLevel >  DistrictsInfo.LEVEL_TOWN){
                val name = vb!!.tvTown.text.toString()
                queryDistName(name)

                vb?.tvArea?.text = name
            }

        }

        vb!!.llVillage.onClick {
            if(selectData!!.distLevel > DistrictsInfo.LEVEL_VILLAGE){
                val name = vb!!.tvVillage.text.toString()
                queryDistName(name)

                vb?.tvArea?.text = name
            }
        }

    }



    fun initView(activity :Activity, str :String = ""){
        this.mActivity = activity


    }

    fun queryDistName(name :String){

        val item =  AppContext.app.districtHelper.getDistrictByName(name)
        if (item != null) {
            selectData = item
            displayCurrentArea()

            dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
            areaAdapter!!.update(dataList)
            vb!!.lvMain.visiable(CommonUtil.matchList(dataList))
//            // 村级是最底层，点击村级导航按钮后不显示列表
//            if(selectData!!.distLevel < DistrictsInfo.LEVEL_VILLAGE){
//                dataList = AppContext.app.districtHelper.getNextLevelDistricts(selectData!!.distCode)
//                areaAdapter!!.update(dataList)
//                vb!!.lvMain.visiable(CommonUtil.matchList(dataList))
//            } else {
//                // 村级隐藏列表
//                vb!!.lvMain.gone()
//            }

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
                vb!!.tvVillage.text = ""

                vb!!.ivProvince.show()
                vb!!.ivCity.gone()
                vb!!.ivCounty.gone()
                vb!!.ivTown.gone()
                vb!!.ivVillage.gone()

                vb!!.llProvince.show()
                vb!!.llCity.gone()
                vb!!.llCounty.gone()
                vb!!.llTown.gone()
                vb!!.llVillage.gone()
            }
            DistrictsInfo.LEVEL_CITY -> {
                vb!!.tvCity.text = selectData!!.distName
                vb!!.tvCounty.text = ""
                vb!!.tvTown.text = ""
                vb!!.tvVillage.text = ""

                vb!!.ivProvince.gone()
                vb!!.ivCity.show()
                vb!!.ivCounty.gone()
                vb!!.ivTown.gone()
                vb!!.ivVillage.gone()


                vb!!.llProvince.visiable(Config.userLevel <= Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.gone()
                vb!!.llTown.gone()
                vb!!.llVillage.gone()
            }
            DistrictsInfo.LEVEL_COUNTY ->{
                vb!!.tvCounty.text = selectData!!.distName
                vb!!.tvTown.text = ""
                vb!!.tvVillage.text = ""

                vb!!.ivProvince.gone()
                vb!!.ivCity.gone()
                vb!!.ivCounty.show()
                vb!!.ivTown.gone()
                vb!!.ivVillage.gone()

                vb!!.llProvince.visiable(Config.userLevel <=  Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.visiable(Config.userLevel <= Config.USER_LEVEL_COUNTY)
                vb!!.llTown.gone()
                vb!!.llVillage.gone()
            }
            DistrictsInfo.LEVEL_TOWN -> {
                vb!!.tvTown.text = selectData!!.distName
                vb!!.tvVillage.text = ""

                vb!!.ivProvince.gone()
                vb!!.ivCity.gone()
                vb!!.ivCounty.gone()
                vb!!.ivTown.show()
                vb!!.ivVillage.gone()

                vb!!.llProvince.visiable(Config.userLevel <=  Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.visiable(Config.userLevel <= Config.USER_LEVEL_COUNTY)
                vb!!.llTown.show()
                vb!!.llVillage.gone()
            }
            DistrictsInfo.LEVEL_VILLAGE -> {
                vb!!.tvVillage.text = selectData!!.distName

                vb!!.ivProvince.gone()
                vb!!.ivCity.gone()
                vb!!.ivCounty.gone()
                vb!!.ivTown.gone()
                vb!!.ivVillage.show()

                vb!!.llProvince.visiable(Config.userLevel <= Config.USER_LEVEL_PROVINCE)
                vb!!.llCity.visiable(Config.userLevel <= Config.USER_LEVEL_CITY)
                vb!!.llCounty.visiable(Config.userLevel <= Config.USER_LEVEL_COUNTY)
                vb!!.llTown.visiable(Config.userLevel <= Config.USER_LEVEL_TOWN)
                vb!!.llVillage.visiable(Config.userLevel <= Config.USER_LEVEL_VILLAGE)

                // 选到村级后隐藏列表（村级是最底层，没有下一级了）
                vb!!.lvMain.gone()
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