package com.jwch.gwyt_project.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Window
import com.jameni.jamenidialoglib.dialog.JameniBaseDialog
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.databinding.ViewUpdateDataDialogBinding
import com.jwch.gwyt_project.util.FileDeleteUtil
import com.jwch.gwyt_project.util.TextFileReader
import com.qmuiteam.qmui.kotlin.onClick
import java.text.DecimalFormat
import java.util.concurrent.Executors


//数据更新dialog
class FileDeleteDialog(context: Context,  private val ids: List<String>, private val onDeleteCompleted: ((successCount: Int, failedCount: Int) -> Unit)? = null) :
    JameniBaseDialog(context) {

    var vb: ViewUpdateDataDialogBinding? = null

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private var totalFiles = 0
    private var deletedCount = 0
    private var successCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.view_update_data_dialog, null)
        vb = ViewUpdateDataDialogBinding.bind(view)
        setContentView(vb!!.root)

        setCanceledOnTouchOutside(false)

        initViewListener()
    }


    fun initViewListener(){

        vb!!.imgClose.onClick {
            dismiss()
        }

        vb!!.buttonStart.onClick(1000) {
            // 开始删除过程
            startDeletion()
        }
    }

    private fun startDeletion() {
        // 获取要删除的文件列表
        val filesToDelete = FileDeleteUtil.getFilesByIds(ids)
        totalFiles = filesToDelete.size

        if (totalFiles == 0) {
            vb!!.statusText.text = "没有找到要删除的文件"
            handler.postDelayed({ dismiss() }, 1500)
            return
        }

        // 初始化进度条
        vb!!.progressBar.max = totalFiles
        vb!!.progressBar.progress = 0
        updateProgressText()

        // 在后台线程执行删除
        executor.execute {
            filesToDelete.forEachIndexed { index, file ->
                // 删除文件
                val isSuccess = FileDeleteUtil.deleteFile(file)

                // 更新计数
                deletedCount++
                if (isSuccess) successCount++

                // 更新UI（切换到主线程）
                handler.post {
                    vb!!.progressBar.progress = deletedCount
                    updateProgressText()

                    // 显示当前正在删除的文件
                    vb!!.statusText.text = "正在删除: ${file.name}"
                }

                // 模拟延迟，以便观察进度（实际使用时可以移除）
                Thread.sleep(50)
            }

            // 删除完成，切换到主线程显示结果
            handler.post {
                val failedCount = totalFiles - successCount
                vb!!.statusText.text = "删除完成: $successCount 成功, $failedCount 失败"

                // 调用完成回调
                onDeleteCompleted?.invoke(successCount, failedCount)

                // 2秒后自动关闭对话框
                handler.postDelayed({ dismiss() }, 2000)
            }
        }
    }

    private fun updateProgressText() {
        val percentage = if (totalFiles > 0) {
            (deletedCount * 100 / totalFiles)
        } else {
            0
        }
        vb!!.progressText.text = "$percentage% ($deletedCount/$totalFiles)"
    }

    companion object {
        // 简化调用方式
        fun showDeleteDialog(
            context: Context,
            ids: List<String>,
            onDeleteCompleted: ((successCount: Int, failedCount: Int) -> Unit)? = null
        ) {
            FileDeleteDialog(context, ids, onDeleteCompleted).show()
        }

        fun showDeleteDialog(
            context: Context,
            id: String,
            onDeleteCompleted: ((successCount: Int, failedCount: Int) -> Unit)? = null
        ) {
            FileDeleteDialog(context, listOf(id), onDeleteCompleted).show()
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 确保资源被正确释放
        executor.shutdownNow()
        handler.removeCallbacksAndMessages(null)
    }



    // 格式化文件大小显示
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }




    //重写这个方法可以防止用回退建关闭窗口
    override fun onBackPressed() {

    }


}
