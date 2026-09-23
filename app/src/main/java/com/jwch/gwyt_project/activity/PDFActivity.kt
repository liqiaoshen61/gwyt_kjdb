package com.jwch.gwyt_project.activity

import android.os.AsyncTask
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.jwch.gwyt_project.Info.FileInfo
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.databinding.PagePdfBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.model.FileModel
import com.qmuiteam.qmui.kotlin.onClick
import java.io.File
import java.io.IOException

class PDFActivity : FullScreenActivity<PagePdfBinding>(), OnLoadCompleteListener, OnErrorListener {


    override fun initView() {
        getPageDatas("pdf_info")?.let {
            val item = it as FileModel
//            setPageTitle(item.name)
            loadPdf(item.filePath)
        }
    }

    override fun initViewListener() {
        vb.ivBack.onClick {
            finish()
        }
    }


    private fun loadPdf(url: String) {

        AsyncTask.execute {
            try {
                //在线文件
//                val input: InputStream = URL(url).openStream()
                //本地文件
                val input = File(url)
                runOnUiThread {
                    vb.pdfView.fromFile(input)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .onLoad(this)
                        .onError(this)
                        .enableAnnotationRendering(true)
                        .scrollHandle(DefaultScrollHandle(this))
                        .spacing(10)
                        .pageFitPolicy(FitPolicy.BOTH) //模式，以适应视图中的页面
                        .load()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun loadComplete(nbPages: Int) {
        vb.llLoading.gone()
        "加载完成".printMsg()
    }

    override fun onError(t: Throwable?) {
        vb.llLoading.gone()
        "加载失败 ${t?.message}".printMsg()
    }


}