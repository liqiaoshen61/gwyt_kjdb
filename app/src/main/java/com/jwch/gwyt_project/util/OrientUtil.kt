package com.jwch.gwyt_project.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.jameni.allutillib.common.CommonUtil
import com.jameni.allutillib.common.TimeUtil
import com.jameni.basepage_lib.baseactivity.FinalActivity
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.AppContext
import com.jwch.gwyt_project.ext.no
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes

class OrientUtil(val context: Context, val block: (Int, String) -> Unit) : SensorEventListener {
    var time = 0L
    var directionResult =0
    lateinit var sensorManager: SensorManager
    var mTargetDirection = 0.0f
    var mDirection = 0.0f
    var mHorizontal = true

    init {
        time = TimeUtil.getCurrentStamp()
        sensorManager = context.getSystemService(FinalActivity.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.sensor?.let {
            when (it.type) {

                Sensor.TYPE_ORIENTATION -> {
                    //
                    checkTime().yes {
                        //方向
                        val direction = event.values[0] * -1.0f
                        val temp = normalizeDegree(direction)
                        if (Math.abs(temp - mTargetDirection) > 0.9) {
                            if (Math.abs(event.values[1]) > 60 && Math.abs(event.values[1]) < 120 || Math.abs(event.values[1]) > 240 && Math.abs(event.values[1]) < 300) {
                                mHorizontal = false
                                mTargetDirection = event.values[0] % 360
                            } else {
                                mHorizontal = true
                                mTargetDirection = temp - 90f
                            }
                        }
                        updateDirection()
                    }
                }

                else -> {}
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun checkTime(): Boolean {
        val current = TimeUtil.getCurrentStamp()
        if (current.minus(time) >= 300) {
            time = current
            return true
        }
        return false
    }

    public fun unRregist() {
        sensorManager.unregisterListener(this)
    }


    private fun normalizeDegree(degree: Float): Float = (degree + 720) % 360
    private fun calculatorDegree(degree: Float): Int = (((degree + 360) % 360)).toInt()

    private fun updateDirection() {

        mHorizontal.yes {
            directionResult = calculatorDegree(mTargetDirection * -1.0f)
//            vb.tvDirection.text = "$direction°"

            var directionTxt = ""
            if (directionResult >= 337.5f || directionResult <= 22.5f) {
                directionTxt = context.getString(R.string.north)
            } else if (directionResult > 22.5f && directionResult < 67.5f) {
                directionTxt = context.getString(R.string.east_north)
            } else if (directionResult >= 67.5f && directionResult <= 112.5f) {
                directionTxt = context.getString(R.string.east)
            } else if (directionResult > 112.5f && directionResult < 157.5f) {
                directionTxt = context.getString(R.string.east_south)
            } else if (directionResult >= 157.5f && directionResult <= 202.5f) {
                directionTxt = context.getString(R.string.south)
            } else if (directionResult > 202.5 && directionResult < 247.5f) {
                directionTxt = context.getString(R.string.west_south)
            } else if (directionResult >= 247.5f && directionResult <= 292.5f) {
                directionTxt = context.getString(R.string.west)
            } else if (directionResult > 292.5f && directionResult < 337.5f) {
                directionTxt = context.getString(R.string.west_north)
            }

            block(directionResult, directionTxt)

        }.no {

            val direction = mTargetDirection
            var directionTxt = ""
            if (direction >= 337.5f || direction <= 22.5f) {
                directionTxt = context.getString(R.string.ch_vertical_north)
            } else if (direction > 22.5f && direction < 67.5f) {
                directionTxt = context.getString(R.string.ch_vertical_east_north)
            } else if (direction >= 67.5f && direction <= 112.5f) {
                directionTxt = context.getString(R.string.ch_vertical_east)
            } else if (direction > 112.5f && direction < 157.5f) {
                directionTxt = context.getString(R.string.ch_vertical_east_south)
            } else if (direction >= 157.5f && direction <= 202.5f) {
                directionTxt = context.getString(R.string.ch_vertical_south)
            } else if (direction > 202.5 && direction < 247.5f) {
                directionTxt = context.getString(R.string.ch_vertical_west_south)
            } else if (direction >= 247.5f && direction <= 292.5f) {
                directionTxt = context.getString(R.string.ch_vertical_west)
            } else if (direction > 292.5f && direction < 337.5f) {
                directionTxt = context.getString(R.string.ch_vertical_west_north)
            }
            block(direction.toInt(), directionTxt)

        }
    }
}