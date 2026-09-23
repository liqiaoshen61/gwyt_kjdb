package com.jwch.gwyt_project.activity

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.jameni.allutillib.app.SystemUtil
import com.jameni.allutillib.common.GetWindowSize
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.databinding.PageLoginBinding
import com.jwch.gwyt_project.ext.checkEmpty
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.showSingleDialog
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.model.UserModel
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.startActivity

//
class LoginActivity : FullScreenActivity<PageLoginBinding>() {


    var isPwVisiable = false //是否展示密码

    lateinit var windowSize: GetWindowSize


    override fun beforeLayout() {
        super.beforeLayout()
    }

    override fun initView() {
        windowSize = GetWindowSize(this)

        vb.tvVersion.text = "版本:${SystemUtil.getVersionName(context)}"
        setViewSize()

    }


    override fun onResume() {
        super.onResume()

    }


    private fun setViewSize() {
        val sw = windowSize.windowWidth //屏幕宽
        val sh = windowSize.windowHeight//屏幕高

//        val rlCard = vb.rlCard.layoutParams
//        rlCard.width = sw * 88 / 100
//        rlCard.height = ScaleUtil.getScaleY(rlCard.width, 3537, 2215)

//        val llLoginInfo = vb.llLoginInfo.layoutParams
//        llLoginInfo.width = sw * 44 / 100
//
//        val size = llLoginInfo.width * 7 / 10
//
//        val llAccount = vb.llAccount.layoutParams
//        llAccount.width = size
//        val llpassword = vb.llpassword.layoutParams
//        llpassword.width = size
//
//        val tvLogin = vb.tvLogin.layoutParams
//        tvLogin.width = size

    }

    override fun initViewListener() {

        vb.apply {


            tvLogin.onClick(1000) {
                val strAccount = vb.etAcc.checkEmpty("请输入账号") ?: return@onClick
                val strPw = vb.etPw.checkEmpty("请输入密码") ?: return@onClick
                login(strAccount, strPw)

            }

            //密码显示隐藏
            imgViewPw.onClick {
                isPwVisiable.yes {
                    vb.etPw.transformationMethod = PasswordTransformationMethod.getInstance()
                    vb.imgViewPw.setImageResource(R.mipmap.logo_pw_invisiable)
                    isPwVisiable = false
                }.no {
                    vb.etPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    vb.imgViewPw.setImageResource(R.mipmap.logo_pw_visiable)
                    isPwVisiable = true
                }
            }

            tvTest1.onClick {
                login("admin", "zzLINYE@2023")
            }

            test.onClick {
                login("admin", "jingwei123")
            }

        }
    }


    private fun login(acc: String, pw: String) {

        if (acc != "admin" || pw != "jingwei123") {
            showSingleDialog(context, "账号或密码错误")
            return
        }

        val user = UserModel(acc, pw)
        user.apply {
            AppContext.app.saveUser(this)
            startActivity<MainActivity>()
            finish()
        }

    }
}


