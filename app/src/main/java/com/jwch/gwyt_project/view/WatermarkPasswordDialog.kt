package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.WatermarkPasswordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.NumberDaologBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show

/**
 * 水印密码弹窗
 *
 * 用于水印功能的密码验证，与软件锁屏密码独立，互不影响。
 *
 * 支持三种模式：
 * - ACTION_SET_PASSWORD  : 设置新密码（首次设置，需输入两次确认）
 * - ACTION_VERIFY_PASSWORD : 验证密码（开关水印、编辑水印时调用）
 * - ACTION_MODIFY_PASSWORD : 修改密码（需先验证原密码）
 */
class WatermarkPasswordDialog(
    context: Context,
    var type: Int,
    var onVerified: () -> Unit = {}
) : JameniBaseDialog(context) {

    var vb: NumberDaologBinding? = null

    companion object {
        private const val TAG = "WatermarkPasswordDialog"

        const val ACTION_SET_PASSWORD = 0       //设置新密码
        const val ACTION_VERIFY_PASSWORD = 1    //验证密码
        const val ACTION_MODIFY_PASSWORD = 2    //修改密码

        const val STEP_INPUT_PASSWORD = 0       //输入（新）密码
        const val STEP_CHECK_PASSWORD = 1       //确认新密码
        const val STEP_INPUT_OLD_PASSWORD = 2   //输入旧密码（修改密码时）
        const val STEP_INPUT_NEW_PASSWORD = 3   //输入新密码（修改密码时）
        const val STEP_CHECK_NEW_PASSWORD = 4   //确认新密码（修改密码时）
    }

    var verifyBlock = onVerified
    var oldPassWord = ""    //旧密码
    var startPassword = ""  //新密码
    var endPassword = ""    //确认密码
    var settingStep = 0     //当前步骤
    var passwordInfo: WatermarkPasswordInfo? = null
    var wrongPasswordCount = 0  //错误次数（验证模式用）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.number_daolog, null)
        vb = NumberDaologBinding.bind(view)
        setContentView(vb!!.root)
        setCanceledOnTouchOutside(false)

        initData()
        dialogFitScreen()
    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(context)
        val width = util.windowWidth / 5 * 2
        val height = util.windowHeight / 3 * 2
        vb!!.llDialog.layoutParams.width = width
        vb!!.llDialog.layoutParams.height = height
    }

    fun initData() {

        //查出密码对象
        passwordInfo = db.queryWatermarkPassword()

        //初始化界面展示
        when (type) {
            ACTION_SET_PASSWORD -> {
                vb!!.tvDialogTitle.text = "设置水印密码"
                vb!!.tvTip.text = "请输入密码"
                vb!!.imgClose.show()
            }
            ACTION_MODIFY_PASSWORD -> {
                vb!!.tvDialogTitle.text = "修改水印密码"
                vb!!.tvTip.text = "请输入原密码"
                vb!!.imgClose.show()
                settingStep = STEP_INPUT_OLD_PASSWORD
            }
            ACTION_VERIFY_PASSWORD -> {
                vb!!.tvDialogTitle.text = "水印安全验证"
                vb!!.tvTip.text = "请输入水印密码"
                vb!!.imgClose.show()
            }
        }

        //忘记密码按钮：仅在"验证密码"和"修改密码"时显示（这两种场景用户可能忘记原密码）
        if (type == ACTION_VERIFY_PASSWORD || type == ACTION_MODIFY_PASSWORD) {
            vb!!.tvForgetWatermarkPw.show()
        } else {
            vb!!.tvForgetWatermarkPw.gone()
        }

        //忘记密码点击
        vb!!.tvForgetWatermarkPw.setOnClickListener {
            showForgetDialog()
        }

        //设置密码结果监听
        vb!!.numLockView.setInputListener { result ->

            when (type) {
                ACTION_SET_PASSWORD -> {//设置新密码
                    handleSetPassword(result)
                }

                ACTION_MODIFY_PASSWORD -> {//修改密码
                    handleModifyPassword(result)
                }

                ACTION_VERIFY_PASSWORD -> {//验证密码
                    handleVerifyPassword(result)
                }
            }
        }
        vb!!.imgClose.setOnClickListener { dismiss() }
    }

    /**
     * 处理设置新密码
     */
    private fun handleSetPassword(result: String) {
        if (settingStep == STEP_INPUT_PASSWORD) {
            startPassword = result
            vb!!.numLockView.resetResult()
            vb!!.tvTip.text = "请确认密码"
            settingStep = STEP_CHECK_PASSWORD
        } else if (settingStep == STEP_CHECK_PASSWORD) {
            endPassword = result
            //匹配两次密码
            if (startPassword == endPassword) {
                db.deleteWatermarkPassword()
                db.updateWatermarkPassword(WatermarkPasswordInfo(endPassword))
                Toast.makeText(context, "水印密码设置成功", Toast.LENGTH_SHORT).show()
                verifyBlock()
                dismiss()
            } else {
                endPassword = ""
                vb!!.tvTip.text = "两次密码不一致，请重新输入密码"
                vb!!.numLockView.resetResult()
                vb!!.numLockView.showErrorStatus()
                settingStep = STEP_INPUT_PASSWORD
            }
        }
    }

    /**
     * 处理修改密码
     */
    private fun handleModifyPassword(result: String) {
        if (settingStep == STEP_INPUT_OLD_PASSWORD) {
            oldPassWord = result
            passwordInfo?.let {
                if (oldPassWord == it.passWord) {
                    vb!!.tvTip.text = "请输入新密码"
                    vb!!.numLockView.resetResult()
                    settingStep = STEP_INPUT_NEW_PASSWORD
                } else {
                    oldPassWord = ""
                    vb!!.tvTip.text = "原密码错误，请重新输入"
                    vb!!.numLockView.resetResult()
                    vb!!.numLockView.showErrorStatus()
                    settingStep = STEP_INPUT_OLD_PASSWORD
                }
            }
        } else if (settingStep == STEP_INPUT_NEW_PASSWORD) {
            startPassword = result
            vb!!.numLockView.resetResult()
            vb!!.tvTip.text = "请确认新密码"
            settingStep = STEP_CHECK_NEW_PASSWORD
        } else if (settingStep == STEP_CHECK_NEW_PASSWORD) {
            endPassword = result
            if (startPassword == endPassword) {
                db.deleteWatermarkPassword()
                db.updateWatermarkPassword(WatermarkPasswordInfo(endPassword))
                Toast.makeText(context, "水印密码修改成功", Toast.LENGTH_SHORT).show()
                verifyBlock()
                dismiss()
            } else {
                endPassword = ""
                settingStep = STEP_INPUT_NEW_PASSWORD
                vb!!.tvTip.text = "两次密码不一致，请重新输入新密码"
                vb!!.numLockView.resetResult()
                vb!!.numLockView.showErrorStatus()
            }
        }
    }

    /**
     * 处理验证密码
     */
    private fun handleVerifyPassword(result: String) {
        passwordInfo?.let {
            if (result == it.passWord) {
                wrongPasswordCount = 0
                verifyBlock()
                dismiss()
            } else {
                wrongPasswordCount++
                if (wrongPasswordCount >= 5) {
                    vb!!.tvTip.text = "密码错误次数过多，请稍后再试"
                    vb!!.numLockView.alpha = 0.5f
                    vb!!.numLockView.isEnableClick(false)
                } else {
                    val remainingAttempts = 5 - wrongPasswordCount
                    vb!!.tvTip.text = "密码错误，还有${remainingAttempts}次尝试机会"
                    vb!!.numLockView.resetResult()
                    vb!!.numLockView.showErrorStatus()
                }
            }
        }
    }

    /**
     * 显示忘记密码弹窗
     * 校验成功后：清除原密码 → 关闭当前弹窗 → 弹出"设置新密码"弹窗
     */
    private fun showForgetDialog() {
        WatermarkForgetDialog(context) {
            //重置成功回调：关闭当前验证/修改弹窗，弹出设置新密码弹窗
            dismiss()
            WatermarkPasswordDialog(context, ACTION_SET_PASSWORD, verifyBlock).show()
        }.show()
    }

    override fun show() {
        super.show()
        vb?.numLockView?.resetItem()
    }
}
