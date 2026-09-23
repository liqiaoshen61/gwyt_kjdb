package com.jwch.gwyt_project.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jameni.allutillib.common.CommonUtil
import com.jameni.jamenidialoglib.JameniDialog
import com.jameni.jamenidialoglib.dialog.SelectImageDialog
import com.jwch.gwyt_project.Info.ReviewRecordInfo
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.PhotoDialogActivity
import com.jwch.gwyt_project.activity.BigImageActivity
import com.jwch.gwyt_project.databinding.ViewAddImageListBinding
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.i.ActionListener
import com.jwch.gwyt_project.model.ImageModel
import com.jwch.gwyt_project.util.image_selector.PicSelectUtil
import com.jwch.gwyt_project.util.image_selector.SelectPicListener
import com.jwch.gwyt_project.adapter.GridAddImageAdapter
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.ext.findColor
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.showNormalDialog
import com.jwch.gwyt_project.ext.showSelectionDialog
import com.jwch.gwyt_project.util.Keys
import com.luck.picture.lib.entity.LocalMedia
import org.jetbrains.anko.startActivity

/**
 * 添加图片
 */
class GridAddImage : LinearLayout, OnItemClickListener, OnItemChildClickListener, SelectPicListener {

    var vb: ViewAddImageListBinding? = null
    val layoutId: Int = R.layout.view_add_image_list
    val addPic = "addPic"
    val addItem = ImageModel(addPic)
    var datalist: MutableList<ImageModel> = mutableListOf()
    lateinit var adapter: GridAddImageAdapter
    lateinit var mContext: Context
    var activity: Activity? = null

    var imgDialog: SelectImageDialog? = null
    var listener: ActionListener? = null
    var deleteSelectListener: ActionListener? = null
    var imageDesVisiable = false
    var isLoadFromMemory = true//是否从缓存加载图片
    var isBackgroundTrans = false //是否透明背景

    lateinit var picSelectUtil: PicSelectUtil
    var fileType = 1
    var selectPiclistener: SelectPicListener? = null
    lateinit var selectPicBlock: () -> Unit
    lateinit var resultPicBlock: (String) -> Unit

    /**
     * 与 [resultPicBlock] 相同，但额外带回图片来源：
     * [com.jwch.gwyt_project.util.image_selector.PicSelectUtil.SOURCE_CAMERA] 或
     * [com.jwch.gwyt_project.util.image_selector.PicSelectUtil.SOURCE_ALBUM]。
     * 设置后优先于 [resultPicBlock] 生效，供调用方按来源差异化处理（例如相册图不展示"同时保存截屏地图"）。
     */
    var resultPicBlockWithSource: ((String, Int) -> Unit)? = null

    var picNameColor = 0


    fun initView(context: Context?) {
        context?.let {
            mContext = context
            val contentView: View = LayoutInflater.from(it).inflate(layoutId, null)
            var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            contentView.layoutParams = params

            vb = ViewAddImageListBinding.bind(contentView)

            vb?.apply {
                addView(root)
            }
        }
    }

    constructor(context: Context?) : super(context) {
        initViewData(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewData(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViewData(context, attrs)
    }

    override fun onFinishInflate() {
        initViewData()
        super.onFinishInflate()
    }

    var title: String? = null
    var maxCount: Int = 0
    var columnCount: Int = 4
    var titleVisiable: Boolean = true
    var enableSelection = false //启用选择模式

    fun initViewData(context: Context?, attrs: AttributeSet?) {
        initView(context)

        if (context == null || attrs == null) return


        val array = context.obtainStyledAttributes(attrs, R.styleable.GridAddImage)

        array.apply {
            title = getString(R.styleable.GridAddImage_title_value)
            titleVisiable = getBoolean(R.styleable.GridAddImage_title_visiable, true)
            isLoadFromMemory = getBoolean(R.styleable.GridAddImage_isLoadFromMemory, true)
            isBackgroundTrans = getBoolean(R.styleable.GridAddImage_background_trans, false)
            maxCount = getColor(R.styleable.GridAddImage_max_count, 3)
            columnCount = getColor(R.styleable.GridAddImage_column_count, 4)
            enableSelection = getBoolean(R.styleable.GridAddImage_enable_selection, false)
            picNameColor = getInt(R.styleable.GridAddImage_nameColor, 0)
        }

    }

    fun initViewData() {

        print("  initViewData   initViewData")
        vb!!.tvTitle.key = title
        vb!!.tvTitle.visibility = if (titleVisiable) View.VISIBLE else View.GONE

        vb!!.lvPic.layoutManager = GridLayoutManager(context, columnCount)
        datalist.add(addItem)
        adapter = GridAddImageAdapter()
        adapter.enableSelection = enableSelection
        adapter.isLoadFromMemory = isLoadFromMemory
        adapter.nameColor = picNameColor
        adapter.setOnItemClickListener(this)
        adapter.setOnItemChildClickListener(this)
        vb!!.lvPic.adapter = adapter
        adapter.update(datalist)
        activity?.let {
            picSelectUtil = PicSelectUtil(it, this, fileType)
        }

        if(isBackgroundTrans){
            vb!!.llAddImage.setBackgroundColor(findColor(context, R.color.transparent))
            vb!!.lvPic.setBackgroundColor(findColor(context, R.color.transparent))
        }



    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {


        if (datalist.get(position).imgUrl.equals(addPic)) {

            if (datalist.size - 1 < maxCount) {
                if (::selectPicBlock.isInitialized) {
                    selectPicBlock()
                }else {
                    if (!::picSelectUtil.isInitialized) {

                        if (selectPiclistener == null) {
                            picSelectUtil = PicSelectUtil(activity, this, fileType)
                        } else {
                            picSelectUtil = PicSelectUtil(activity, selectPiclistener, fileType)
                        }
                    }

                    if (::picSelectUtil.isInitialized) {

                        (maxCount - datalist.size + 1 > 0).yes {
                            //选择拍照或从相册选择
                            showSelectionDialog(context, "拍照,从相册选择") { _, which ->
                                when (which) {
                                    0 -> picSelectUtil.selectImageFromCamera(1)
                                    1 -> picSelectUtil.selectImageFromAlbum(1)
                                }
                            }
                        }
                    }
                }

            } else {
                JameniDialog.Builder(context).setMsg("最多添加${maxCount}张图片").showSingleDialog()
            }

        } else {
            AppContext.map[Keys.IMG_MODEL_LIST] = datalist
            datalist.contains(addItem).yes {
                AppContext.map[Keys.INDEX] = position - 1
            }.no {
                AppContext.map[Keys.INDEX] = position
            }

            AppContext.map[Keys.IMAGE_DES_VISIABLE] = imageDesVisiable
            mContext.startActivity<BigImageActivity>()
        }
    }


    override fun onItemChildClick(adapter2: BaseQuickAdapter<*, *>, view: View, position: Int) {

        when (view.id) {

            R.id.imgDelect -> {
                showNormalDialog(context, "是否删除照片"){
                    it.yes {
                        datalist.removeAt(position)
                        adapter.update(datalist)
                        //listener监听删除按钮 去操作删除数据库和本地图片
                        (listener != null).yes{
                            //由于datalist第一个项是addPic 真正的图片序号为 position-1
                            listener?.onAction(position-1, PhotoDialogActivity.ACTION_DELETE)
                        }
                        (deleteSelectListener != null).yes{
                            //由于datalist第一个项是addPic 真正的图片序号为 position-1
                            deleteSelectListener?.onAction(position, SaveReviewRecordDialog.ACTION_DELETE)
                        }
                    }
                }
            }
            R.id.cbSelect ->{
                val item = adapter.getItem(position) as ImageModel
                item.isSelect = !item.isSelect
                "$=== ${item.isSelect}".printMsg()
            }
        }
    }


    fun addImage(img: ImageModel?) {

        if (datalist.size - 1 < maxCount) {

            img?.let {
                datalist.add(it)
                adapter.notifyDataSetChanged()
                adapter.update(datalist)
            }
        }
    }

    fun setShowOnly(canDelete :Boolean = false) {
        adapter.isDelete = canDelete

        if (datalist.size > 0) {
            if (datalist[0].imgUrl == addPic) {
                datalist.removeAt(0)
            }
        }
        adapter.update(datalist)
    }

    //只展示图片
    fun showGrid(list: MutableList<ImageModel>?,canDelete :Boolean = false) {
        datalist.clear()
        list?.let {
            datalist.addAll(it)
            adapter.isDelete = canDelete
            adapter.update(datalist)
        }
    }



    fun clearDataList(){
        datalist.clear()
        adapter.update(datalist)
    }


    override fun onSelectPicSuccess(resultList: MutableList<LocalMedia>?, flag: Int) {
        CommonUtil.matchList(resultList).yes {
            val url = picSelectUtil.getUploadUrl(resultList)
            resultPicBlockWithSource?.let { it(url, flag) }
                    ?: if (::resultPicBlock.isInitialized) {
                        resultPicBlock(url)
                    }
        }
    }


    fun setImageSize(imageWidth :Int , imageHeight :Int ){
        adapter.imageWidth = imageWidth
        adapter.imageHeight = imageHeight
    }

    fun getSelectedData() : MutableList<ImageModel>?{
        val filterList =  datalist.filter { it.isSelect } as MutableList<ImageModel>
        if(CommonUtil.matchList(filterList)){
            return filterList
        } else{
            return null
        }
    }

    fun setEnableSelectionModel(enable : Boolean){
        enableSelection = enable
        adapter.enableSelection = enableSelection
        adapter.update(datalist)
    }


}