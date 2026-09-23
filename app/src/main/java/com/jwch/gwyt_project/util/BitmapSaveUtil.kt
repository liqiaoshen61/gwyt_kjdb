package com.jwch.gwyt_project.util

import android.graphics.Bitmap
import com.jameni.allutillib.common.FileUtil
import com.jameni.allutillib.common.PrintUtil
import com.jameni.allutillib.common.TimeUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BitmapSaveUtil {

    companion object{

        fun saveMyBitmap(mBitmap: Bitmap): String {


            val fileName = "wk_${TimeUtil.getCurrentStamp()}"
            val path = FileUtil.getSDPath() + File.separator + "temp" + File.separator + fileName + ".jpg"
            PrintUtil.printMsg("sd卡路径：$path")

            val f = File(path)
            try {

                if (f.isDirectory) {
                    f.mkdirs()
                }

                f.createNewFile()

                var fOut = FileOutputStream(f)
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: IOException) {
                PrintUtil.printMsg("在保存图片时出错：${e.message}")
            }
            return path
        }
    }
}