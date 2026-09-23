package com.jwch.gwyt_project.util

import android.app.Activity
import androidx.core.content.ContextCompat
import cn.addapp.pickers.common.LineConfig
import cn.addapp.pickers.picker.DateTimePicker
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R

class PickerUtil(activity: Activity) {
    var activity: Activity

    init {
        this.activity = activity
    }


    fun showDateTimePicker(
        listener: DateTimePicker.OnYearMonthDayTimePickListener,
        title: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ) {

        var picker = DateTimePicker(activity, DateTimePicker.HOUR_24)
        picker.setActionButtonTop(true)

        picker.setDateRangeStart(year, 1, 1)
        picker.setDateRangeEnd(year, month, day)

        if (minute == 0) {
            //等于0的时候会奔溃
            picker.setSelectedItem(year, month, day, hour, minute + 1)
        } else {
            picker.setSelectedItem(year, month, day, hour, minute)
        }
        picker.setCanLinkage(false)
        picker.setTitleText(title)
        picker.setWeightEnable(true)
        picker.setWheelModeEnable(true)
        val config = LineConfig()
        config.color = ContextCompat.getColor(activity, R.color.primaryColor) //线颜色
        config.alpha = 120 //线透明度
        config.isVisible = true //线不显示 默认显示
        picker.setLineConfig(config)
        picker.setLabel(null, null, null, null, null)
        if (CommonUtil.isNotNull(listener)) {
            picker.setOnDateTimePickListener(listener)
        }
        picker.show()
    }
}