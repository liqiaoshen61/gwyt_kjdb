package com.jwch.gwyt_project.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.*
import android.hardware.SensorManager
import android.net.Uri
import android.os.Handler
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.FullScreenActivity
import com.jwch.gwyt_project.databinding.PageWatermarkCamera2Binding
import com.jwch.gwyt_project.databinding.PageWatermarkCameraBinding
import com.jwch.gwyt_project.ext.*
import com.jwch.gwyt_project.util.BitmapManager
import com.jwch.gwyt_project.util.ImagePathUriUtil
import com.jwch.gwyt_project.util.SystemUtil
import com.jwch.gwyt_project.util.UnifiedLocationManager
import com.jwch.gwyt_project.view.CameraPreview
import com.jwch.gwyt_project.view.OverCameraView
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class WaterMarkCameraActivity : FullScreenActivity<PageWatermarkCameraBinding>(),
    View.OnTouchListener {

    private var mFile: File? = null
    private var fileName: String? = null
    private var mOverCameraView: OverCameraView? = null
    private var mCamera: Camera? = null
    private var isFlashing = false
    private var isTakePhoto = false
    private var isFoucing = false
    private val mHandler = Handler()
    private var mRunnable: Runnable? = null
    private var newBitmap: Bitmap? = null
    private var cameraPreview: CameraPreview? = null

    private var bitmapManager: BitmapManager? = null
    private var position = "" //定位信息
    private val remark = "" //备注信息
    private var screenHeight = 0
    private var screenWidth = 0
    private var oldOrientation = 90
    private val photoMessageHeight = 0
    private val photoMessageWidth = 0
    private lateinit var watermarkBitmap: Bitmap
    private var is16_9 = true

    private var mCameraId = CameraInfo.CAMERA_FACING_BACK

    //增加传感器
    private var mOrientationEventListener: OrientationEventListener? = null

    //拍照时的传感器方向
    private var takePhotoOrientation = 0

    lateinit var locationManager: UnifiedLocationManager

    override fun initView() {

        getScreenBrightness()
        screenHeight = SystemUtil.getScreenHeight(this)
        screenWidth = SystemUtil.getScreenWidth(this)
        initOrientate()

        vb.stvTime.text = SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(Date())

        vb.cameraPreviewLayout.setOnTouchListener(this)

        dynamicCameraPreview()
        initLocation()

    }

    //根据设备屏幕的宽度 动态设置高度 保证相机的预览尺寸为16:9 或者4:3
    fun dynamicCameraPreview() {
        val lp = vb.cameraPreviewLayout.layoutParams
        lp.width = screenWidth
        lp.height = (screenWidth * CameraPreview.SCALE_16_9).toInt()
        if (lp.height > screenHeight) {
            lp.height = (screenWidth * CameraPreview.SCALE_4_3).toInt()
            is16_9 = false
        }
        vb.cameraPreviewLayout.layoutParams = lp
    }

    private fun initLocation() {
        locationManager = UnifiedLocationManager(context!!)
        locationManager.requestSingleLocation { province, city, area, address, lat, lng ->
            position = if (address.isNotEmpty()) address else "纬度: ${String.format("%.6f", lat)}, 经度: ${String.format("%.6f", lng)}"
        }

    }

    override fun initViewListener() {
        super.initViewListener()

        vb.takePhoto.onClick {
            takePhoto()
        }
        vb.switchFlash.onClick {
            switchFlash()
        }
        vb.ivBack.onClick {
            finish()
        }
        vb.switchCamera.onClick {
            switchCamera()
        }
        vb.delete.onClick {
            cancleSavePhoto()
        }
        vb.save.onClick {
            bitmapManager!!.saveBitmapFile(mFile, newBitmap)
            mFile?.absolutePath.printMsg()
            val intent = Intent()
            intent.putExtra("imagePath", mFile!!.absolutePath)
            val uri: Uri = ImagePathUriUtil.path2Uri(context, mFile!!.absolutePath) //拍完照插入到数据库

            intent.putExtra("imageUri", uri.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    //获取view转换成bitmap
    private fun getView2BitMap(waterPhoto: View): Bitmap {
        val view: View = waterPhoto
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        var bitmap = view.drawingCache
        val width = view.width
        val height = view.height
        val saveBitmap = Bitmap.createBitmap(bitmap!!, 0, 0, width, height)
        view.destroyDrawingCache()
        bitmap = null
        return saveBitmap
    }

    private fun takePhoto() {

        //如果没有输入备注 则隐藏掉备注view
        vb.stvRemark.isNotEmpty().no {
            vb.stvRemark.gone()
        }

        vb.llTakePhoto.visibility = View.GONE
        vb.llPhotoMessage.visibility = View.GONE
        watermarkBitmap = getView2BitMap(vb.llPhotoMessage)
        isTakePhoto = true

        //调用相机拍照
        mCamera!!.takePicture(null, null, null, { data: ByteArray?, camera1: Camera? ->
            //停止预览
            mCamera!!.stopPreview()
            fileName = System.currentTimeMillis().toString() + ".jpg"
            mFile = bitmapManager!!.createFile(fileName, "Camera2Basic")
            bitmapManager!!.getPhoto(
                mFile,
                data,
                mCameraId,
                takePhotoOrientation,
                watermarkBitmap,
                cameraPreview!!.parameters

            )
        })
        vb.llSaveDelete.visibility = View.VISIBLE
    }

    private fun switchFlash() {
        isFlashing = !isFlashing
        vb.switchFlash.setImageResource(if (isFlashing) R.mipmap.flash_open else R.mipmap.flash_close)
        try {
            val parameters = mCamera!!.parameters
            parameters.flashMode =
                if (isFlashing) Parameters.FLASH_MODE_TORCH else Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = parameters
        } catch (e: java.lang.Exception) {
            tip("该设备不支持闪光灯")
        }
    }

    private fun cancleSavePhoto() {
        vb.llSaveDelete.visibility = View.GONE
        vb.llTakePhoto.visibility = View.VISIBLE
        vb.llPhotoMessage.visibility = View.VISIBLE
        vb.cameraPreviewLayout.visibility = View.VISIBLE
        vb.switchFlash.visibility = View.GONE
        if (mFile!!.exists()) mFile!!.delete()
        vb.preview.visibility = View.GONE

        vb.stvRemark.setText("")
        vb.stvRemark.hint = "添加备注"
        vb.stvRemark.show()

        //开始预览
        mCamera!!.startPreview()
        isTakePhoto = false
    }

    private fun switchCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
            mHandler.removeCallbacks(mRunnable!!)
        }
        //在Android P之前 Android设备仍然最多只有前后两个摄像头，在Android p后支持多个摄像头 用户想打开哪个就打开哪个
        mCameraId = (mCameraId + 1) % getNumberOfCameras()
        //打开摄像头
        //是否支持前后摄像头
        val isSupportCamera = cameraPreview!!.isSupport(mCameraId)
        //如果支持
        if (isSupportCamera) {
            try {
                openCamera()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        openCamera()
        bitmapManager = BitmapManager(this)
        bitmapManager!!.setOnBitmapCompleteListener(onBitmapCompleteListener)
    }

    var onBitmapCompleteListener: BitmapManager.OnBitmapCompleteListener =
        BitmapManager.OnBitmapCompleteListener { bitmap ->
            newBitmap = bitmap
            setPreview(newBitmap!!)
        }

    private fun setPreview(bitmap: Bitmap) {
        this.runOnUiThread(Runnable {
            vb.llTakePhoto.visibility = View.GONE
            vb.cameraPreviewLayout.visibility = View.GONE
            vb.switchFlash.visibility = View.GONE
            vb.llPhotoMessage.visibility = View.GONE
            vb.llSaveDelete.visibility = View.VISIBLE
            vb.preview.visibility = View.VISIBLE
            vb.preview.setImageBitmap(bitmap)
            //                Glide.with(getContext()).load(bitmap).into(preview);
        })
    }

    private fun initOrientate() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener =
                object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
                    override fun onOrientationChanged(orientation: Int) {
                        var orientation = orientation

                        // i的范围是0-359
                        // 屏幕左边在顶部的时候 i = 90;
                        // 屏幕顶部在底部的时候 i = 180;
                        // 屏幕右边在底部的时候 i = 270;
                        // 正常的情况默认i = 0;
//                    System.out.println("orientation------>"+orientation);

                        //拍照方向设置
                        takePhotoOrientation = if (45 <= orientation && orientation < 135) {
                            180
                        } else if (135 <= orientation && orientation < 225) {
                            270
                        } else if (225 <= orientation && orientation < 315) {
                            0
                        } else {
                            90
                        }
//                        System.out.println("takePhotoOrientation------>"+takePhotoOrientation);
                        if (orientation == ORIENTATION_UNKNOWN) {
                            return  // 手机平放时，检测不到有效的角度
                        }

                        //预览方向设置
                        orientation =
                            if (orientation > 30 && orientation < 60) { // 动画0度与接口360度相反,增加下限抵消0度影响
                                0
                            } else if (orientation > 70 && orientation < 110) { // 动画90度与接口270度相反
                                270
                            } else if (orientation > 160 && orientation < 200) { // 180度
                                180
                            } else if (orientation > 240 && orientation < 300) {
                                90
                            } else if (orientation > 320 && orientation < 340) { // 减少上限减少360度的影响
                                0
                            } else {
                                return
                            }


                        if (oldOrientation != orientation) {
                            val rotation = ObjectAnimator.ofFloat(
                                vb.llPhotoMessage, "Rotation", oldOrientation.toFloat(),
                                orientation.toFloat()
                            ).setDuration(200)
                            val photoMessageHeight = vb.llPhotoMessage.height
                            //System.out.println("ll_photo_message--h---->"+photoMessageHeight+"--w---->"+screenWidth);
                            //System.out.println("orientation------>"+orientation);
                            if (orientation == 270) {
                                vb.llPhotoMessage.pivotX = (screenWidth / 2).toFloat()
                                vb.llPhotoMessage.pivotY =
                                    -(screenWidth / 2 - photoMessageHeight).toFloat()
                            } else if (orientation == 90) {
                                vb.llPhotoMessage.pivotX = (screenWidth / 2).toFloat()
                                vb.llPhotoMessage.pivotY =
                                    -(screenWidth / 2 - photoMessageHeight).toFloat()
                            } else if (orientation == 180) {
                                vb.llPhotoMessage.pivotX = (screenWidth / 2).toFloat()
                                vb.llPhotoMessage.pivotY =
                                    -(screenWidth / 2 - photoMessageHeight).toFloat()
                            } else {
                                vb.llPhotoMessage.pivotX = 0f
                                vb.llPhotoMessage.pivotY = 0f
                            }
                            rotation.start()
                            if (orientation == 270 || orientation == 0) {
                                val translationY: ObjectAnimator = ObjectAnimator.ofFloat(
                                    vb.llPhotoMessage, "translationY",
                                    -(cameraPreview!!.height - screenWidth).toFloat(), 0F
                                )
                                translationY.duration = 0
                                translationY.start()
                            } else if (orientation == 90 || orientation == 180) {
                                val translationY: ObjectAnimator = ObjectAnimator.ofFloat(
                                    vb.llPhotoMessage, "translationY",
                                    0F, -(cameraPreview!!.height - screenWidth).toFloat()
                                )
                                translationY.duration = 0
                                translationY.start()
                            }
                            vb.llPhotoMessage.clearAnimation()
                            oldOrientation = orientation
                        }
                    }
                }
        }
        mOrientationEventListener!!.enable()
    }

    private fun getScreenBrightness() {
        val lp: WindowManager.LayoutParams = window.attributes
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = java.lang.Float.valueOf(200f) * (1f / 255f)
        window.attributes = lp
    }

    private fun openCamera() {
        vb.cameraPreviewLayout.removeAllViews()
        mCamera = open(mCameraId)

        cameraPreview = CameraPreview(this, mCamera, is16_9)
        cameraPreview!!.setmCameraId(mCameraId)
        if (mOverCameraView == null) {
            mOverCameraView = OverCameraView(this)
        }
        vb.cameraPreviewLayout.addView(cameraPreview)
        vb.cameraPreviewLayout.addView(mOverCameraView)
    }

    private val autoFocusCallback =
        AutoFocusCallback { success, camera ->
            isFoucing = false
            mOverCameraView!!.isFoucuing = false
            mOverCameraView!!.disDrawTouchFocusRect()
            //停止聚焦超时回调
            mHandler.removeCallbacks(mRunnable!!)
        }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p0!!.id) {
            R.id.camera_preview_layout -> {
                setOverCameraView(p1!!)
            }
        }
        return false
    }

    private fun setOverCameraView(event: MotionEvent) {
        if (!isFoucing) {
            isFoucing = true
            if (mCamera != null && !isTakePhoto) {
                mOverCameraView!!.setTouchFoucusRect(mCamera, autoFocusCallback, event.x, event.y)
            }
            mRunnable = Runnable {
                isFoucing = false
                mOverCameraView!!.isFoucuing = false
                mOverCameraView!!.disDrawTouchFocusRect()
            }
            //设置聚焦超时
            mHandler.postDelayed(mRunnable!!, 3000)
        }
    }

}
