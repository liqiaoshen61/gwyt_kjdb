package com.jwch.gwyt_project.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.Info.PasswordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.NumberDaologBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.printMsg


/**
 * 锁屏对话框
 */
class NumLockDialog(context: Context, var type: Int, block: () -> Unit = {}) :
    JameniBaseDialog(context) {

    var vb: NumberDaologBinding? = null

    companion object {
        const val ACTION_SET_NEW_PASSWORD = 0//设置新密码
        const val ACTION_MODIFY_PASSWORD = 1//修改密码
        const val ACTION_INPUT_PASSWORD_UNLOCK = 2//输入密码--用于：锁屏解锁
        const val ACTION_INPUT_PASSWORD_CANCLE_PW = 3//输入密码--用于：取消密码功能

        const val STEP_INPUT_PASSWORD = 0//输入新密码
        const val STEP_CHECK_PASSWORD = 1//确认新密码
        const val STEP_INPUT_NEW_PASSWORD = 2//请输入新密码
        const val STEP_CHECK_NEW_PASSWORD = 3//请确认新密码
    }

    var actionBlock = block
    var errorMaxBlock = block
    var dialog: AlertDialog? = null
    var oldPassWord = ""//旧密码
    var startPassword = ""//新密码
    var endPassword = ""//确认密码
    var settingStep = 0//设置步骤
    var passwordInfo: PasswordInfo? = null
    var wrongPasswordCount = 0

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
        passwordInfo = db.queryPassword()

        //初始化界面展示
        if (type == ACTION_SET_NEW_PASSWORD) {
            vb!!.tvDialogTitle.text = "设置密码"
            vb!!.tvTip.text = "请输入密码"
            vb!!.imgClose.show()
        } else if (type == ACTION_MODIFY_PASSWORD) {
            vb!!.tvDialogTitle.text = "修改密码"
            vb!!.tvTip.text = "请输入原密码"
            vb!!.imgClose.show()
        } else if (type == ACTION_INPUT_PASSWORD_UNLOCK) {
            vb!!.tvDialogTitle.text = "解锁"
            vb!!.tvTip.text = "请输入密码"
            vb!!.imgClose.gone()
        } else if (type == ACTION_INPUT_PASSWORD_CANCLE_PW) {
            vb!!.tvDialogTitle.text = "验证"
            vb!!.tvTip.text = "请输入原密码"
            vb!!.imgClose.gone()
        }

        //设置密码结果监听
        vb!!.numLockView.setInputListener { result ->

            when (type) {
                ACTION_SET_NEW_PASSWORD -> {//设置新密码

                    if (settingStep == STEP_INPUT_PASSWORD) { //输入密码

                        startPassword = result
                        vb!!.numLockView.resetResult()//重置输入
                        vb!!.tvTip.text = "请确认密码"

                        settingStep = STEP_CHECK_PASSWORD

                    } else if (settingStep == STEP_CHECK_PASSWORD) { //确认密码

                        endPassword = result
                        //匹配两次密码
                        if (startPassword == endPassword) {

                            db.deleteAllPassword()
                            db.updatePassword(PasswordInfo(endPassword, PasswordInfo.PASSWORD_SHOW))
                            Toast.makeText(context, "密码设置成功", Toast.LENGTH_SHORT).show()
                            actionBlock()
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

                ACTION_MODIFY_PASSWORD -> {//修改密码

                    if (settingStep == STEP_INPUT_PASSWORD) {//输入旧密码
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
                                settingStep = STEP_INPUT_PASSWORD

                            }

                        }
                    } else if (settingStep == STEP_INPUT_NEW_PASSWORD) {//输入新密码

                        startPassword = result
                        vb!!.numLockView.resetResult()
                        vb!!.tvTip.text = "请确认新密码"
                        settingStep = STEP_CHECK_NEW_PASSWORD

                    } else if (settingStep == STEP_CHECK_NEW_PASSWORD) {//确认新密码

                        endPassword = result

                        if (startPassword == endPassword) {//两次密码匹配
                            db.deleteAllPassword()
                            db.updatePassword(PasswordInfo(endPassword, PasswordInfo.PASSWORD_SHOW))
                            Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show()
                            actionBlock()
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
                ACTION_INPUT_PASSWORD_CANCLE_PW ->{
                    passwordInfo?.let {
                        if (result == it.passWord) {
                            actionBlock()
                            dismiss()
                        } else {
                            vb!!.tvTip.text = "密码错误，请重新输入"
                            vb!!.numLockView.resetResult()
                            vb!!.numLockView.showErrorStatus()
                        }
                    }
                }

                ACTION_INPUT_PASSWORD_UNLOCK -> {//登录解锁密码,取消密码

                    passwordInfo?.let {
                        if (result == it.passWord) {
                            wrongPasswordCount = 0
                            actionBlock()
                            dismiss()
                        } else {
                            wrongPasswordCount++

                            if (wrongPasswordCount >= 5) {
                                vb!!.tvTip.text = "密码错误次数过多，启动数据销毁"
                                vb!!.numLockView.alpha = 0.5f
                                vb!!.numLockView.isEnableClick(false)
                                errorMaxBlock()
                            } else {
                                val remainingAttempts = 5 - wrongPasswordCount
                                vb!!.tvTip.text = "密码错误，还有${remainingAttempts}次尝试机会\n错误次数过多，数据将被销毁"
                                vb!!.numLockView.resetResult()
                                vb!!.numLockView.showErrorStatus()
                            }
                        }
                    }
                }
            }
        }
        vb!!.imgClose.setOnClickListener { dismiss() }
    }

    override fun onBackPressed() {
        (type == 2).yes {

        }.no {
            super.onBackPressed()
        }
    }


    override fun show() {
        super.show()

        vb!!.numLockView.resetItem()
    }

}