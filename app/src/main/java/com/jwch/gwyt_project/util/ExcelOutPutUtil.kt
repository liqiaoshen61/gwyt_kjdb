package  com.jwch.gwyt_project.util

import com.jameni.allutillib.common.TimeUtil
import com.jwch.gwyt_project.core.Config
import org.apache.poi.xslf.model.geom.Context
import java.io.File
import java.util.ArrayList

object ExcelOutPutUtil {

    /**
     * 导出excel
     * @param dictionaryPath 文件夹路径
     * @param fileName 文件名
     * @param title 标题
     * @param valueListList 多行的数据
     */
    fun export(dictionaryPath : String, fileName: String, title: MutableList<String>, valueListList:MutableList<MutableList<String>>) {
        val dictionary = File(dictionaryPath)
        if (!dictionary.exists()) {
            dictionary.mkdirs()
        }
        val fileNameTime = fileName + TimeUtil.getCurrentDate("yyyy-MM-dd HH_mm_ss")
        val fileFullPath = "$dictionary/${fileNameTime}.xlsx"
        JxlExcelUtil.initExcel(fileFullPath, title as ArrayList<String>?)
        JxlExcelUtil.writeObjListToExcel(valueListList, fileFullPath)
    }
}