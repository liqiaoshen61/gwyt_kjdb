package com.jwch.gwyt_project.activity

import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.databinding.ActivityMeasureCameraBinding
import com.jwch.gwyt_project.view.DrawOverlayView


class MeasureCameraActivity : BaseActivity<ActivityMeasureCameraBinding>() {


    // 🔑 比例 = 真实毫米 / 像素长度
    private var mmPerPx = 0f       // 校准后得到



    override fun initView() {
        startCamera()

        // ⚠️ 调一次校准方法（放一个已知长的物体，如1元硬币直径=25mm）
        calibrate(25f)       // 可改为 UI 输入

        vb.drawView.onMeasureFinish = { pixelDist ->
            val realLength = pixelDist * mmPerPx    // 单位: mm
            Toast.makeText(this, "长度 = %.2f mm".format(realLength), Toast.LENGTH_LONG).show()
            vb.drawView.reset()
        }

        vb.previewView.setOnTouchListener { _, event ->
            Log.d("PreviewView", "Touch on CAM! x=${event.x}, y=${event.y}")  // 看日志
            false   // true 表示吃掉事件；false 让事件向下层传递
        }
    }

    /** ====== 相机启动 ====== */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider( vb.previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview
            )
        }, ContextCompat.getMainExecutor(this))
    }

    /** ====== 校准方法 ======
     * @param realMM   实际长度（例如25mm）
     */
    private fun calibrate(realMM: Float) {
        vb.drawView.onMeasureFinish = { pxDist ->
            mmPerPx = realMM / pxDist
            Toast.makeText(this,
                "校准完成：1px = %.4f mm".format(mmPerPx),
                Toast.LENGTH_LONG
            ).show()
            vb.drawView.reset()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // ⭐ 把事件转发给 drawView，不让 PreviewView 吃掉
        findViewById<DrawOverlayView>(R.id.drawView)?.let {
            it.onTouchEvent(ev)
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

}
