package com.jwch.gwyt_project.activity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.jameni.allutillib.common.GetWindowSize
import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.activity.base.BaseActivity
import com.jwch.gwyt_project.databinding.PageTestSensorBinding
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.ext.yes
import com.jwch.gwyt_project.util.UnifiedLocationManager
import com.qmuiteam.qmui.kotlin.onClick
import org.jetbrains.anko.startActivity

class TestSensorActivity : BaseActivity<PageTestSensorBinding>(), SensorEventListener {

    var time = 0L
    var mTargetDirection = 0.0f
    var mDirection = 0.0f
    var mHorizontal = true
    lateinit var locationManager: UnifiedLocationManager
    override fun initView() {
        time = TimeUtil.getCurrentStamp()
        initCompassView()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
//        sensorList.forEach {
//            vb.tv.append(it.name + "\n")
//        }

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, 30000000)

        vb.tvY.onClick {
            startActivity<WaterMarkCameraActivity2>()
        }

        locationManager = UnifiedLocationManager(context!!)
        vb.tvZ.onClick {
            locationManager.requestSingleLocation { province, city, area, address, lat, lng ->
                val locationInfo = if (address.isNotEmpty()) address else "纬度: ${String.format("%.6f", lat)}, 经度: ${String.format("%.6f", lng)}"
                locationInfo.tip()
            }
        }
    }

    private fun initCompassView() {
        val getWindowSize = GetWindowSize(this)
        vb.compassDetail.layoutParams.width = getWindowSize.windowWidth
        vb.compassDetail.layoutParams.height = vb.compassDetail.layoutParams.width

    }

    //传感器精度变化时回调
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    //传感器数据变化时回调
    override fun onSensorChanged(event: SensorEvent?) {
        event?.sensor?.let {
            when (it.type) {
                Sensor.TYPE_ACCELEROMETER -> {

                    val X_lateral = event?.values?.get(0)
                    val Y_longitudinal = event?.values?.get(1)
                    val Z_vertical = event?.values?.get(2)
//                    val text = "X_lateral：$X_lateral   Y_longitudinal：$Y_longitudinal   Z_vertical：$Z_vertical   "+ "\n"+ vb.tv.text.toString()
//                    vb.tv.append(text)
                    val text = "X_lateral：$X_lateral   Y_longitudinal：$Y_longitudinal   Z_vertical：$Z_vertical   "
                    text.printMsg()
                }

                Sensor.TYPE_GYROSCOPE -> {
                    //陀螺仪

                    val X = Math.toDegrees(event?.values?.get(0)?.toDouble()!!)
                    val Y = Math.toDegrees(event?.values?.get(1)?.toDouble()!!)
                    val Z = Math.toDegrees(event?.values?.get(2)?.toDouble()!!)
                    val text = "X：$X   Y：$Y   Z：$Z   "
                    text.printMsg()
                }

                Sensor.TYPE_ORIENTATION -> {
                    //
                    checkTime().yes {
                        //方向
                        val fangweijiao = (Math.round(event.values[0] * 100)) / 100 // X轴
                        val qingxie = (Math.round(event.values[1] * 100)) / 100 // Y轴
                        val gundong = (Math.round(event.values[2] * 100)) / 100 // Z轴


                        val direction = event.values[0] * -1.0f
                        val temp = normalizeDegree(direction)
                        if (Math.abs(temp - mTargetDirection) > 0.9) {
                            if (Math.abs(event.values[1]) > 60 && Math.abs(event.values[1]) < 120 || Math.abs(event.values[1]) > 240 && Math.abs(event.values[1]) < 300) {
                                mHorizontal = false
                                mTargetDirection = event.values[0] % 360
                            } else {
                                mHorizontal = true
                                mTargetDirection = temp -90f
                            }
                        }

//                        val text = "1\n方位角：$fangweijiao\n倾斜角：$qingxie\n" + "滚动角：$gundong\n" + "方向：$mTargetDirection"
//                        text.printMsg()
//                        vb.tv.text = text

                        vb.tvY.text = "Y:${qingxie}"
                        vb.tvZ.text = "Z:${gundong}"
                        updateDirection()

                    }
                }

                else -> {}
            }
        }
    }

    private fun checkTime(): Boolean {
        val current = TimeUtil.getCurrentStamp()
        if (current.minus(time) >= 1000) {
            time = current
            return true
        }
        return false
    }

    private fun normalizeDegree(degree: Float): Float = (degree + 720) % 360
    private fun calculatorDegree(degree: Float): Int = (((degree + 360) % 360)).toInt()

    private fun updateDirection() {

        mHorizontal.yes {
            var direction = calculatorDegree(mTargetDirection * -1.0f)
            vb.tvDirection.text = "$direction°"

            if (direction >= 337.5f || direction <= 22.5f) {
                vb.tv.setText(getString(R.string.north));
            } else if (direction > 22.5f && direction < 67.5f) {
                vb.tv.setText(getString(R.string.east_north));
            } else if (direction >= 67.5f && direction <= 112.5f) {
                vb.tv.setText(getString(R.string.east));
            } else if (direction > 112.5f && direction < 157.5f) {
                vb.tv.setText(getString(R.string.east_south));
            } else if (direction >= 157.5f && direction <= 202.5f) {
                vb.tv.setText(getString(R.string.south));
            } else if (direction > 202.5 && direction < 247.5f) {
                vb.tv.setText(getString(R.string.west_south));
            } else if (direction >= 247.5f && direction <= 292.5f) {
                vb.tv.setText(getString(R.string.west));
            } else if (direction > 292.5f && direction < 337.5f) {
                vb.tv.setText(getString(R.string.west_north));
            }
        }.no {

            val direction = mTargetDirection

            if (direction >= 337.5f || direction <= 22.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_north));
            } else if (direction > 22.5f && direction < 67.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_east_north));
            } else if (direction >= 67.5f && direction <= 112.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_east));
            } else if (direction > 112.5f && direction < 157.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_east_south));
            } else if (direction >= 157.5f && direction <= 202.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_south));
            } else if (direction > 202.5 && direction < 247.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_west_south));
            } else if (direction >= 247.5f && direction <= 292.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_west));
            } else if (direction > 292.5f && direction < 337.5f) {
                vb.tv.setText(getString(R.string.ch_vertical_west_north));
            }


        }
    }
}