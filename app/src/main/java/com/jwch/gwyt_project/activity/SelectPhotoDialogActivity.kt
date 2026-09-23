package com.jwch.gwyt_project.activity


import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jameni.allutillib.common.GetWindowSize
import com.jwch.gwyt_project.Info.ImageInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.adapter.GridSelectImageAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.databinding.ViewSelectPhotoBinding
import com.jwch.gwyt_project.db.DbUtil.Companion.db
import com.jwch.gwyt_project.ext.getKV
import com.jwch.gwyt_project.ext.getListFromJson
import com.jwch.gwyt_project.ext.self
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.fragment.CollectionFragment
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.GeoCollectionModel
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.util.Keys
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xutils.ex.DbException

/**
 * 照片的Activity (Dialog样式) --用于从已经拍摄的照片中选择
 */
class SelectPhotoDialogActivity : FullScreenActivity<ViewSelectPhotoBinding>(), OnItemClickListener,
    OnItemChildClickListener {


    var mLinkId: String = ""//数据关联id
    var mDataType = 0//图片类型
    var listFormLastPage = mutableListOf<ImageModel>()//从上一个页面传来的 已选择的图片

    lateinit var adapter: GridSelectImageAdapter
    var photoList: MutableList<ImageInfo>? = null
    var datalist: MutableList<ImageModel> = mutableListOf()

    override fun initView() {
        dialogFitScreen()

        mLinkId = getKV("linkId", "")
        mDataType = getKV("dataType", 0)
        val listJson = getKV("pageData", "")
        if(listJson.isNotBlank()){
            listFormLastPage = getListFromJson(listJson)
        }

        photoList = mutableListOf()


        initList()
        updatePhotoList()


    }

    fun initList() {

        vb.lvPic.layoutManager = LinearLayoutManager(context)
        adapter = GridSelectImageAdapter()
        adapter.setOnItemClickListener(this)
        adapter.setOnItemChildClickListener(this)
        vb.lvPic.adapter = adapter
        adapter.update(photoList)

    }

    private fun dialogFitScreen() {
        val util = GetWindowSize(this)
        val width = util.windowWidth / 2 * 1
        val height = util.windowHeight / 5 * 3
        vb.llDialog.layoutParams.width = width
        vb.llDialog.layoutParams.height = height
    }

    override fun onItemClick(adapter1: BaseQuickAdapter<*, *>, view: View, position: Int) {

        val item = adapter.getItem(position) as ImageModel
        item.isSelect = !item.isSelect
        adapter.update(datalist)
    }

    override fun onItemChildClick(adapter1: BaseQuickAdapter<*, *>, view: View, position: Int) {
        when (view.id) {
            R.id.imgPic -> {
                AppContext.map[Keys.IMG_MODEL_LIST] = datalist
                AppContext.map[Keys.INDEX] = position
                AppContext.map[Keys.IMAGE_DES_VISIABLE] = false
                startActivity<BigImageActivity>()
            }
        }
    }

    /**
     * 获取图片数据
     */
    private fun updatePhotoList() {

        photoList?.clear()

        try {
            //根据pid和type 查询数据库
            photoList = db.queryImageInfoByLinkIdAndDataType(mLinkId, mDataType)
        } catch (e: DbException) {
            e.printStackTrace()
        }
        //列表展示
        matchList(photoList).yes {
            photoList?.forEach {
                addImage(ImageModel(it.filePath, it.remark.self(), it.name))
            }

            listFormLastPage.forEach { it ->
                datalist.forEach { item ->
                    if(item.imgUrl == it.imgUrl){
                        item.isSelect = true
                    }
                }
            }

            adapter.update(datalist)
        }
    }


    fun addImage(img: ImageModel?) {
        img?.let {
            datalist.add(it)
        }
    }



    override fun initViewListener() {
        vb.tvCloseDialogAcitvity.onClick {
            finish()
        }
        //取消
        vb.tvCancle.onClick {
            finish()
        }

        //确认
        vb.tvOk.onClick {
            val selectList = datalist.filter { it.isSelect }
            EventBus.getDefault().post(DataEvent(DataEvent.GO_BACK_REVIEW_RECORD_DIALOG, selectList))//打开相册页面对话框
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}
