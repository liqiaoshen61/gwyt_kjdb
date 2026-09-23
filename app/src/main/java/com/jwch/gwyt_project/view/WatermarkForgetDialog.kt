package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.DialogWatermarkForgetBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.tip

/**
 * 忘记水印密码 - 安全校验弹窗
 *
 * 输入安全校验码，校验正确后清除原水印密码，并回调让用户重新设置新密码。
 * 输入仅支持数字和字母（特殊符号/表情会被过滤）。
 */
class WatermarkForgetDialog(
    context: Context,
    private val onResetSuccess: () -> Unit = {}
) : JameniBaseDialog(context) {

    companion object {
        private const val TAG = "WatermarkForgetDialog"

        // 安全校验码（纯字母数字，不含特殊符号）
        private const val SECURITY_CODE = "fjwxsj123"

        // 仅允许数字和字母（@ 不能输入，所以校验码实际无法通过键盘打出 @，
        // 这里在代码层额外过滤，输入框只接收数字+字母；校验时对输入做严格匹配）
        private const val ALLOW_REGEX = "^[0-9a-zA-Z]+$"
    }

    private var vb: DialogWatermarkForgetBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = layoutInflater.inflate(R.layout.dialog_watermark_forget, null)
        vb = DialogWatermarkForgetBinding.bind(view)
        setContentView(view)
        setCanceledOnTouchOutside(false)

        initView()
        setOnClick()
    }

    private fun initView() {
        // 输入监听：实时过滤掉非数字字母字符
        vb?.etCode?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString() ?: ""
                // 过滤掉除数字和字母外的所有字符（含特殊符号、表情）
                val filtered = input.replace(Regex("[^0-9a-zA-Z]"), "")
                if (filtered != input) {
                    vb?.etCode?.setText(filtered)
                    vb?.etCode?.setSelection(filtered.length)
                }
                // 清空提示
                if (vb?.tvTip?.text?.isNotEmpty() == true) {
                    vb?.tvTip?.text = ""
                }
            }
        })
    }

    private fun setOnClick() {
        vb?.imgClose?.setOnClickListener {
            dismiss()
        }

        vb?.tvConfirm?.setOnClickListener {
            verifyAndReset()
        }
    }

    /**
     * 校验并重置
     */
    private fun verifyAndReset() {
        val input = vb?.etCode?.text?.toString() ?: ""

        if (input.isBlank()) {
            vb?.tvTip?.text = "请输入安全校验码"
            return
        }

        // 严格格式校验（只允许数字+字母）
        if (!input.matches(Regex(ALLOW_REGEX))) {
            vb?.tvTip?.text = "校验码格式不正确"
            return
        }

        // 校验码比对
        if (input == SECURITY_CODE) {
            Log.d(TAG, "安全校验码正确，清除水印密码")
            // 清除原始水印密码
            db.deleteWatermarkPassword()
            "校验成功，请设置新密码".tip()
            dismiss()
            // 回调：让外层弹出"设置新密码"弹窗
            onResetSuccess()
        } else {
            vb?.tvTip?.text = "校验码错误，请重新输入"
            vb?.etCode?.setText("")
        }
    }


}
