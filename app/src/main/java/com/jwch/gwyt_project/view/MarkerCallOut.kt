package com.jwch.gwyt_project.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.Callout
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.Info.MarkerInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.ViewMarkerCalloutBinding
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.NavigationPointModel
import com.jwch.gwyt_project.util.NavigationUtil
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import com.jwch.gwyt_project.util.OperationLogger

class MarkerCallOut : RelativeLayout {

    open fun getLayoutId(): Int = R.layout.view_marker_callout
    var vb: ViewMarkerCalloutBinding? = null

    private var callout : Callout? = null//弹窗对象
    private var markerInfo: MarkerInfo? = null
    lateinit var navigationUtil: NavigationUtil

    constructor(context: Context?) : super(context){
        initViewData(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    fun initViewData(context: Context?, attrs: AttributeSet?){
        initView(context)
        initViewListener()
    }

    fun initView(context: Context?) {
        context?.let {

            val contentView: View = LayoutInflater.from(it).inflate(getLayoutId(), null)
            contentView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            vb = ViewMarkerCalloutBinding.bind(contentView)
            vb?.apply { addView(root) }
            navigationUtil = NavigationUtil(context)
        }
    }

    fun getCenterPoint() : Point?{

        if(markerInfo !=null && Geometry.fromJson(markerInfo!!.centerPointJson) != null){
            val point = Geometry.fromJson(markerInfo!!.centerPointJson) as Point
            return GeometryEngine.project(point, Config.sp4490) as Point
        }else{
            return  null
        }

    }

    fun initViewListener(){
        vb?.apply {

            imgNavigation.onClick {

                if (getCenterPoint() == null) {
                    ToastUtils.show("暂无位置信息，导航失败")
                    return@onClick
                }


                showNormalDialog(context, "确定跳转到导航软件吗？") {
                    it.yes {
                        context.packageManager.getInstalledPackages(0)  //华为手机在获取所有包名时会弹窗询问，所有这里先调用一次，保证导航功能正常
                        val point = NavigationPointModel(markerInfo?.questionType.self(), getCenterPoint()!!.y, getCenterPoint()!!.x)
                        navigationUtil.startNavigation(point)
                    }
                }
            }
            //关闭
            imgClose.onClick {
                if (CommonUtil.isNotNull(callout)) {
                    callout!!.dismiss()
                }
            }

            btnTakePhoto.onClick{
                saveKV("linkId", markerInfo!!.id.toString() + "")
                saveKV("dataType", ImageInfo.IMAGE_MARKER)
                saveKV("enableSelection", false)
                OperationLogger.logOperation(context, "[PhotoDialogActivity] MarkerCallOut 发送GO_PHOTO事件 linkId=${markerInfo?.id}, dataType=${ImageInfo.IMAGE_MARKER}")
                EventBus.getDefault().post(DataEvent(DataEvent.GO_PHOTO)) //打开相册页面对话框
            }
            //附件
            btnMedia.onClick {
                saveKV("linkId", markerInfo!!.id.toString() + "")
                saveKV("dataType", ImageInfo.IMAGE_MARKER)
                EventBus.getDefault().post(DataEvent(DataEvent.GO_MEDIA))
            }
        }
    }

    fun setCalloutData(info : MarkerInfo?) {
        this.markerInfo = info
        if (info == null) return
        vb!!.tvRiverName.text = "河流名称：" + CommonUtil.getSelfValue(info.riverName)
        vb!!.tvCity.text = "所在市：" + CommonUtil.getSelfValue(info.city)
        vb!!.tvCounty.text = "所在县：" + CommonUtil.getSelfValue(info.county)
        vb!!.tvQuestionType.text = "问题分类：" + CommonUtil.getSelfValue(info.questionType)
        vb!!.tvQuestionAttr.text = "问题属性：" + CommonUtil.getSelfValue(info.questionAttr)
        vb!!.tvSeverity.text = "严重程度：" + CommonUtil.getSelfValue(info.severity)
        vb!!.tvDescription.text = "问题描述：" + CommonUtil.getSelfValue(info.description)
        vb!!.tvLocation.text = "发生位置描述：" + CommonUtil.getSelfValue(info.location)
        vb!!.tvLatLng.text = "坐标：" + CommonUtil.getSelfValue(info.latLng)
        vb!!.tvCheckTime.text = "复核时间：" + CommonUtil.getSelfValue(TimeUtil.getDateToString(info.checkTime, Config.timeFormat1))
    }

    fun setCallout(callout: Callout?) { this.callout = callout }



}