package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Window
import android.widget.LinearLayout
import com.hjq.toast.ToastUtils
import com.jameni.allutillib.app.SystemUtil
import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.PasswordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.LocationDetailActivity
import com.jwch.gwyt_project.activity.LocationTestActivity
import com.jwch.gwyt_project.activity.LoginActivity
import com.jwch.gwyt_project.activity.SatelliteActivity
import com.jwch.gwyt_project.common.Tools
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.DialogSettingBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getListFromJson
import com.jwch.gwyt_project.ext.getOne
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.saveKV
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.util.FunctionControlUtil
import com.jwch.gwyt_project.util.GetJsonUtil
import com.jwch.gwyt_project.util.Keys
import com.jwch.gwyt_project.util.TextFileReader
import com.jwch.gwyt_project.view.NumLockDialog.Companion.ACTION_INPUT_PASSWORD_CANCLE_PW
import com.jwch.gwyt_project.view.NumLockDialog.Companion.ACTION_MODIFY_PASSWORD
import com.jwch.gwyt_project.view.NumLockDialog.Companion.ACTION_SET_NEW_PASSWORD
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 * 设置Dialog
 */
class SettingDialog(context: Context) : JameniBaseDialog(context) {

    companion object {
        private const val TAG = "SettingDialog"
    }

    var numLockPanelUtil: NumLockDialog? = null
    var vb: DialogSettingBinding? = null
    private var passwordInfo: PasswordInfo? = null
    var isContinuousLocation = false //是否开启了持续定位
    var isWatermarkEnabled = false //是否启用水印
    var debugUnlocked = false //是否已解锁开发入口（连点版本号5次；本次设置对话内存活，长按版本号3秒可关闭）
    private var versionTapCount = 0
    private val versionHandler = Handler(Looper.getMainLooper())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_setting, null)
        vb = DialogSettingBinding.bind(view)
        setContentView(vb!!.root)
        setCanceledOnTouchOutside(false)
        EventBus.getDefault().register(this)


        initView()
        setOnClick()

        vb?.llpw?.visiable(FunctionControlUtil.instances.MODULE_SETTING_NUMBER_PW)



        vb!!.tvDeviceId.text = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    fun initView() {



        passwordInfo = db.queryPassword()

        isContinuousLocation = getKV(Keys.IS_CONTINUOUS_LOCATION, false)
        setContinuousLocationStatus(isContinuousLocation)

        //初始化水印状态
        isWatermarkEnabled = getKV(Keys.WATERMARK_ENABLED, false)
        setWatermarkStatus(isWatermarkEnabled)

        //根据水印状态显示/隐藏密码与编辑按钮
        updateWatermarkButtonsVisibility()

        //先从本地看看是否有密码，如果有，判断是否展示密码
        if (Tools.getIsOpenLock()) {
            setPasswordStatus(true)
            vb!!.llEditPassword.show()
        } else {
            setPasswordStatus(false)
            vb!!.llEditPassword.gone()
        }

        vb!!.tvVersion.text = SystemUtil.getVersionName(context)
        //开发入口默认隐藏，需连点版本号5次解锁；长按版本号3秒关闭
        debugUnlocked = false
        applyDebugEntriesVisibility()
        getDataUpdateTime()

        vb!!.imgPassword.onClick {
            //查询数据库，判断是否有密码

            if (passwordInfo == null) {
                //从未设置过密码情况
                showNumberLockDialog(ACTION_SET_NEW_PASSWORD)
            } else {
                //关闭锁屏功能
                showNormalDialog(context, "是否取消锁屏密码？") {
                    it.yes {
                        showNumberLockDialog(ACTION_INPUT_PASSWORD_CANCLE_PW)
                    }
                }
            }
        }


        vb!!.imgContinuousLocation.onClick {
            isContinuousLocation = !isContinuousLocation
            //设置保存到到本地
            saveKV(Keys.IS_CONTINUOUS_LOCATION, isContinuousLocation)
            //改变按钮样式
            setContinuousLocationStatus(isContinuousLocation)

            EventBus.getDefault()
                .post(DataEvent(DataEvent.GPS_CONTINUOUS_LOCATION, isContinuousLocation))

        }

        //水印开关点击事件
        vb!!.imgWatermark.onClick {
            //开关水印需要密码验证
            toggleWatermarkWithVerify()
        }

        //水印编辑按钮点击事件（需要密码验证）
        vb!!.tvWatermarkEdit.onClick {
            Log.d(TAG, "水印编辑按钮点击")
            editWatermarkWithVerify()
        }

        //水印密码按钮点击事件（设置/修改密码）
        vb!!.tvWatermarkPassword.onClick {
            Log.d(TAG, "水印密码按钮点击")
            val passwordInfo = db.queryWatermarkPassword()
            if (passwordInfo == null) {
                //未设置密码，引导设置
                showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_SET_PASSWORD)
            } else {
                //已设置密码，直接弹出修改密码弹窗（修改模式已包含验证原密码步骤）
                showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_MODIFY_PASSWORD)
            }
        }
    }

    /**
     * 根据水印开关状态显示/隐藏密码与编辑按钮
     */
    private fun updateWatermarkButtonsVisibility() {
        if (isWatermarkEnabled) {
            vb!!.tvWatermarkPassword.show()
            vb!!.tvWatermarkEdit.show()
        } else {
            vb!!.tvWatermarkPassword.gone()
            vb!!.tvWatermarkEdit.gone()
        }
    }

    /**
     * 开关水印（带密码验证）
     */
    private fun toggleWatermarkWithVerify() {
        val passwordInfo = db.queryWatermarkPassword()
        if (passwordInfo == null) {
            //未设置密码，引导用户先设置水印密码
            "请先设置水印密码".tip()
            showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_SET_PASSWORD) {
                //密码设置成功后，直接执行开关操作
                doToggleWatermark()
            }
            return
        }
        //已设置密码，验证后执行
        showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_VERIFY_PASSWORD) {
            doToggleWatermark()
        }
    }

    /**
     * 执行水印开关切换
     */
    private fun doToggleWatermark() {
        isWatermarkEnabled = !isWatermarkEnabled
        Log.d(TAG, "水印开关点击: $isWatermarkEnabled")
        //保存到本地
        saveKV(Keys.WATERMARK_ENABLED, isWatermarkEnabled)
        //改变按钮样式
        setWatermarkStatus(isWatermarkEnabled)

        //根据水印状态显示/隐藏密码与编辑按钮
        updateWatermarkButtonsVisibility()

        //发送事件通知Activity更新水印
        Log.d(TAG, "发送水印状态改变事件: $isWatermarkEnabled")
        EventBus.getDefault()
            .post(DataEvent(DataEvent.WATERMARK_STATE_CHANGED, isWatermarkEnabled))
    }

    /**
     * 编辑水印（带密码验证）
     */
    private fun editWatermarkWithVerify() {
        val passwordInfo = db.queryWatermarkPassword()
        if (passwordInfo == null) {
            //未设置密码，引导用户先设置水印密码
            "请先设置水印密码".tip()
            showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_SET_PASSWORD) {
                //密码设置成功后，直接打开编辑弹窗
                showWatermarkEditDialog()
            }
            return
        }
        //已设置密码，验证后编辑
        showWatermarkPasswordDialog(WatermarkPasswordDialog.ACTION_VERIFY_PASSWORD) {
            showWatermarkEditDialog()
        }
    }

    /**
     * 显示水印密码弹窗
     * @param type 操作类型（设置/验证/修改）
     * @param onVerified 验证成功回调
     */
    private fun showWatermarkPasswordDialog(type: Int, onVerified: () -> Unit = {}) {
        val dialog = WatermarkPasswordDialog(context, type, onVerified)
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    /**
     * 显示水印编辑对话框
     */
    private fun showWatermarkEditDialog() {
        val dialog = WatermarkEditDialog(context)
        dialog.show()
    }


    fun getDataUpdateTime() {
        try {
            val gson = GetJsonUtil.getJsonFromFile(Config.APPDB_PATH + "app_update_log.json")
            if (gson.isNotBlank()) {
                vb!!.llDataUpdateTime.show()
                val list = getListFromJson<UpdateLog>(gson)
                val dataUpdatetime =
                    list.filter { it.update_type == "data" }.maxByOrNull { it.update_time }?.version
                vb!!.tvDataUpdateTime.text = dataUpdatetime
            } else {
                vb!!.llDataUpdateTime.gone()
            }
        } catch (e: Exception) {
            // JSON解析异常时隐藏更新时间区域
            vb!!.llDataUpdateTime.gone()
        }
    }

    private fun setOnClick() {

        //连点版本号5次解锁开发入口（卫星信号 / 定位详细信息 / 定位测试）
        vb!!.tvVersion.onClick {
            versionTapCount++
            if (versionTapCount >= 5) {
                debugUnlocked = true
                "开发入口已解锁".tip()
                applyDebugEntriesVisibility()
            }
        }
        //长按版本号3秒关闭开发入口
        vb!!.tvVersion.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    versionHandler.removeCallbacksAndMessages(null)
                    versionHandler.postDelayed({ closeDebugEntries() }, 3000L)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    versionHandler.removeCallbacksAndMessages(null)
                }
                else -> {}
            }
            // 不消费事件，让单击（连点5次解锁）仍能正常触发
            false
        }
        vb!!.llEditPassword.onClick {
            showNumberLockDialog(ACTION_MODIFY_PASSWORD)
        }
        //卫星信号检测
        vb!!.llSatellite.onClick {
            dismiss()
            context.startActivity<SatelliteActivity>()
        }
        //定位详细信息
        vb!!.llLocationDetail.onClick {
            dismiss()
            context.startActivity<LocationDetailActivity>()
        }
        //定位测试
        vb!!.llLocationTest.onClick {
            dismiss()
            context.startActivity<LocationTestActivity>()
        }
        //关闭dialog
        vb!!.imgClose.onClick { dismiss() }

        vb!!.llLogout.onClick {
            showNormalDialog(context!!, "是否退出登录？") {
                it.yes {
                    "退出成功".tip()
                    AppContext.app.logout()
                    context.startActivity<LoginActivity>()
                }
            }
        }

    }

    private fun showNumberLockDialog(actionType: Int) {

        numLockPanelUtil = NumLockDialog(context, actionType) {


            when (actionType) {
                ACTION_SET_NEW_PASSWORD -> {
                    passwordInfo = db.queryPassword()
                    setPasswordStatus(true)
                    vb!!.llEditPassword.show()
                }

                ACTION_MODIFY_PASSWORD -> {

                }

                ACTION_INPUT_PASSWORD_CANCLE_PW -> {
                    db.deleteAllPassword()//清空密码
                    passwordInfo = null
                    setPasswordStatus(false)
                    vb!!.llEditPassword.gone()
                }
            }

            if (actionType == ACTION_INPUT_PASSWORD_CANCLE_PW) {

            }
        }
        if (!numLockPanelUtil!!.isShowing) {
            numLockPanelUtil!!.show()
        }
    }


    private fun setPasswordStatus(isSetted: Boolean) {
        vb!!.imgPassword.setImageResource(
            isSetted.getOne(
                R.mipmap.icon_switch_on,
                R.mipmap.icon_switch_off
            )
        )
    }

    private fun setContinuousLocationStatus(isSetted: Boolean) {
        vb!!.imgContinuousLocation.setImageResource(
            isSetted.getOne(
                R.mipmap.icon_switch_on,
                R.mipmap.icon_switch_off
            )
        )
    }

    /**
     * 根据解锁状态显示/隐藏开发入口（卫星信号 / 定位详细信息 / 定位测试）
     */
    private fun applyDebugEntriesVisibility() {
        if (debugUnlocked) {
            vb!!.llSatellite.show()
            vb!!.llLocationDetail.show()
            vb!!.llLocationTest.show()
        } else {
            vb!!.llSatellite.gone()
            vb!!.llLocationDetail.gone()
            vb!!.llLocationTest.gone()
        }
    }

    /** 长按版本号3秒后调用：关闭开发入口 */
    private fun closeDebugEntries() {
        debugUnlocked = false
        versionTapCount = 0
        applyDebugEntriesVisibility()
        "开发入口已关闭".tip()
    }

    private fun setWatermarkStatus(isSetted: Boolean) {
        vb!!.imgWatermark.setImageResource(
            isSetted.getOne(
                R.mipmap.icon_switch_on,
                R.mipmap.icon_switch_off
            )
        )
    }

    //操作界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlePageView(event: DataEvent?) {
        if (event == null) return
        when (event.actionType) {


        }
    }


    data class UpdateLog(
        val id: String,
        val update_time: String,
        val update_type: String,
        val version: String,
        val description: List<String>,
        val status: String
    )

}