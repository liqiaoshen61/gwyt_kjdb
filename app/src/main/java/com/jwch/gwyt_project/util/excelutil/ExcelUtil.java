package com.jwch.gwyt_project.util.excelutil;

import android.util.Log;

import com.jameni.allutillib.common.PrintUtil;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * 读取excel   （xls和xlsx）
     *
     * @return
     */
    public static List<Map<String, String>> readExcel(File file, String columns[]) {

        String filePath = file.getAbsolutePath();
        Sheet sheet = null;
        Row row = null;
        Row rowHeader = null;
        List<Map<String, String>> list = null;
        String cellData = null;
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
//                wb = WorkbookFactory.create(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            }
            if (wb != null) {
                // 用来存放表中数据
                list = new ArrayList<Map<String, String>>();
                // 获取第一个sheet
                sheet = wb.getSheetAt(0);
                // 获取最大行数
                int rownum = sheet.getPhysicalNumberOfRows();
                // 获取第一行
                rowHeader = sheet.getRow(0);
                row = sheet.getRow(0);
                // 获取最大列数
                int colnum = row.getPhysicalNumberOfCells();
                for (int i = 1; i < rownum; i++) {
                    Map<String, String> map = new LinkedHashMap<String, String>();
                    row = sheet.getRow(i);
                    if (row != null) {
                        for (int j = 0; j < colnum; j++) {
                            if (columns[j].equals(getCellFormatValue(rowHeader.getCell(j)))) {
                                cellData = (String) getCellFormatValue(row
                                        .getCell(j));
                                map.put(columns[j], cellData);
                                /*DecimalFormat df = new DecimalFormat("#");
                                System.out.println(    df.format(cellData));*/
                                Log.e("yy", "cellData=" + cellData);
                                Log.e("yy", "map=" + map);
                            }
                        }
                    } else {
                        break;
                    }
                    list.add(map);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取单个单元格数据
     *
     * @param cell
     * @return
     * @author lizixiang ,2018-05-08
     */
    public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            // 判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        // 数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }


    public static boolean writeToExcel(File file, String sheetName, List<String> titleList, List<Map<String, String>> datalist) {
        boolean flag = false;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

        //加入头部
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());//设置背景颜色
        titleStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);//设置水平方向字体居中
        titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//设置垂直方向字体居中


        Row rowTitle = sheet.createRow(0);
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(titleList.get(i));
            cell.setCellStyle(titleStyle);
        }

        for (int i = 0; i < datalist.size(); i++) {

            Map<String, String> item = datalist.get(i);

            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < titleList.size(); j++) {

                Cell cell = row.createCell(j);
                String key = titleList.get(j);
                String value = item.get(key);
                cell.setCellValue(value);
            }
        }

        try {
            OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
            PrintUtil.printMsg("导出excel失败 " + e.getMessage());
        }

        return flag;
    }

}
