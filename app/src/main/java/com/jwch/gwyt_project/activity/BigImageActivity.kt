package com.jwch.gwyt_project.activity

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.databinding.PageBigPicBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.view.PinchImageView
import com.jwch.gwyt_project.adapter.BigImagePagerAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.util.Keys
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.find

/**
 * 查看大图 竖屏
 */
class BigImageActivity : FullScreenActivity<PageBigPicBinding>() {

    override fun getPageTitle(): String = "查看大图"

    lateinit var urlList: MutableList<String>
    lateinit var imgListData: MutableList<ImageModel>
    var imgList = mutableListOf<View>()
    lateinit var adapter: BigImagePagerAdapter
    var index = 0
    var desVisiable = false
    var loadFromCache = false //从缓存加载

    override fun initView() {

//        urlList = getPageDatas(Keys.IMG_URL_LIST) as MutableList<String>
        index = getPageDatas(Keys.INDEX) as Int
        desVisiable = getPageDatas(Keys.IMAGE_DES_VISIABLE) as Boolean
        imgListData = getPageDatas(Keys.IMG_MODEL_LIST) as MutableList<ImageModel>
        getPageDatas(Keys.IMAGE_LOAD_FROM_CACHE)?.let {
            loadFromCache = it as Boolean
        }

    }

    override fun initViewListener() {
        vb.ivBack.onClick {
            finish()
        }
    }

    override fun initPageData(data: Any?) {

        if (::urlList.isInitialized && matchList(urlList)) {
            urlList.forEachIndexed { _, url ->
                imgList.add(getImageView(url))
            }
        }

        if (::imgListData.isInitialized && matchList(imgListData)) {
            imgListData.forEachIndexed { position, imgModle ->

                if (!imgModle.imgUrl.equals("addPic")) {
                    imgList.add(getImageView(imgModle.imgUrl.self(), imgModle.des.self()))
                }
            }
        }

        adapter = BigImagePagerAdapter(imgList)

        if (!matchList(imgList) || imgList.size <= index) {
            index = 0
        }

        vb.vpMain.adapter = adapter
        vb.vpMain.currentItem = index


        vb.vpMain.onPageSelect {
            index = it
//            print("当前页$it")
        }
    }

//    private fun getImageView(imgUrl: String, des: String=""): View {
//
//        kotlin.io.print("url:$imgUrl")
//
//        var view = LayoutInflater.from(context!!).inflate(R.layout.view_flow_img, null)
//        var img = view.find<PinchImageView>(R.id.imgPic)
//        var tvdes = view.find<TextView>(R.id.tvDes)
//        tvdes.text = des
//
//        loadFromCache.yes {
//            Glide.with(AppContext.app).load(imgUrl).thumbnail(0.35f).into(img)
//        }.no {
//            Glide.with(context).load(imgUrl).skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE).into(img)
//        }
//
//
//
//        val tvDes = view.find<TextView>(R.id.tvDes)
//        tvDes.setOnClickListener {
//        }
//
//        tvdes.visiable(desVisiable)
//        return view
//    }

    private fun getImageView(imgUrl: String, des: String = ""): View {
        kotlin.io.print("url:$imgUrl")

        val view = LayoutInflater.from(context!!).inflate(R.layout.view_flow_img, null)
        val img = view.find<PinchImageView>(R.id.imgPic)
        val tvdes = view.find<TextView>(R.id.tvDes)
        tvdes.text = des

        // 添加错误处理和图片优化配置
        val requestOptions = RequestOptions()
//            .placeholder(R.mipmap.no_data) // 添加加载占位图
            .error(R.mipmap.no_data) // 添加错误占位图
            .format(DecodeFormat.PREFER_RGB_565) // 使用更省内存的格式
            .disallowHardwareConfig() // 避免硬件加速问题

        if (loadFromCache) {
            Glide.with(AppContext.app)
                .load(imgUrl)
                .apply(requestOptions)
                .thumbnail(0.25f) // 降低缩略图比例
                .override(1200, 1200) // 限制最大尺寸
                .into(img)
        } else {
            Glide.with(context)
                .load(imgUrl)
                .apply(requestOptions)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(1200, 1200) // 限制最大尺寸
                .into(img)
        }

        val tvDes = view.find<TextView>(R.id.tvDes)
        tvDes.setOnClickListener {
            // 点击事件
        }

        tvdes.visibility = if (desVisiable) View.VISIBLE else View.GONE
        return view
    }






}

