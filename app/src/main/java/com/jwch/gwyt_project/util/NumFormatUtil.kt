package com.jwch.gwyt_project.util

import java.text.DecimalFormat

class NumFormatUtil {

    companion object {

        fun formatFloat(num: Float): String = DecimalFormat("###################.##").format(num)
        fun formatFloat(num: Double): String = DecimalFormat("###################.##").format(num)
    }
}