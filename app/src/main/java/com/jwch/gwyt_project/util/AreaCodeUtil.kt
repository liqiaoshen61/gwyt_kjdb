package com.jwch.gwyt_project.util

import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.yes

class AreaCodeUtil {

    companion object {

        //这个方法是兼容 超过12位的行政区code，将前面12位截取出来
        fun getAreaCode(code: String, level: Int): String {
            var result = ""

            CommonUtil.isNotEmpty(code).yes {
                val len = code.length
                "行政区代码code 长度：$len".printMsg()
                var subLen = 0 // 长度3 是省级， 6是市级， 9是乡镇级，12村级
                when (level) {
                    0 -> subLen = 3
                    1 -> subLen = 6
                    2 -> subLen = 9
                    3 -> subLen = 12
                }

                if (len > subLen) {
                    val str = code.subSequence(0, subLen)
                    "截取行政区代码 ：$str".printMsg()
                    result = str.toString()
                } else {
                    result = code
                }
            }
            return result
        }
    }
}