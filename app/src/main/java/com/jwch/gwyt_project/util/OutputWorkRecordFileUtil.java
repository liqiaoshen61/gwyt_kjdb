package com.jwch.gwyt_project.util;

import android.os.Handler;
import android.os.Message;

import com.jameni.allutillib.common.CommonUtil;
import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.Info.ImageInfo;
import com.jwch.gwyt_project.Info.MarkerInfo;
import com.jwch.gwyt_project.common.Tools;
import com.jwch.gwyt_project.core.Config;
import com.jwch.gwyt_project.model.GeoCollectionModel;
import com.jwch.gwyt_project.db.DbUtil;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class OutputWorkRecordFileUtil {
    List<GeoCollectionModel> list = new ArrayList<>();

    public void output(List<GeoCollectionModel> l, Handler handler) {
        list = l;
        new Thread() {
            public void run() {
                final String filePath = outputExcel(list);
                Message msg = new Message();
                msg.obj = filePath;
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }.start();
    }

    private String GeoToWKT(MarkerInfo info) {
        if (info == null) return null;

        try {
            JSONObject obj = new JSONObject(info.getGeometryJson());
            switch (info.getGeoType()) {
                case 0:
                    return "POINT (" + obj.get("x") + " " + obj.get("y") + ")";
                case 1:
                    return "LINESTRING " + obj.get("paths").toString().replace("[[[", "(").replace("]]]", ")").replace(",", " ").replace("] [", ",");
                case 2:
                    return "POLYGON " + obj.get("rings").toString().replace("[[[", "((").replace("]]]", "))").replace(",", " ").replace("] [", ",");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String outputExcel(List<GeoCollectionModel> list) {
        String filePath = Config.OUTPUT_PATH + "工作记录" + Tools.getTime() + "/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("工作记录");

        // 创建标题行
        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        String[] title = {
                "河流名称", "所在市", "所在县", "所在镇","所在村","问题分类", "问题属性",
                "严重程度", "问题描述", "发生位置描述", "坐标", "是否整改","图斑整改情况","复核时间", "图片"
        };

        for (int i = 0; i < title.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }

        try {
            // 处理数据行
            for (int i = 0; i < list.size(); i++) {
                row = sheet.createRow(i + 1);
                MarkerInfo markerInfo = list.get(i).getMarkerInfo();

                // 添加文本数据
                row.createCell(0).setCellValue(markerInfo.getRiverName());
                row.createCell(1).setCellValue(markerInfo.getCity());
                row.createCell(2).setCellValue(markerInfo.getCounty());
                row.createCell(3).setCellValue(markerInfo.getTown());
                row.createCell(4).setCellValue(markerInfo.getVillage());
                row.createCell(5).setCellValue(markerInfo.getQuestionType());
                row.createCell(6).setCellValue(markerInfo.getQuestionAttr());
                row.createCell(7).setCellValue(markerInfo.getSeverity());
                row.createCell(8).setCellValue(markerInfo.getDescription());
                row.createCell(9).setCellValue(markerInfo.getLocation());
                row.createCell(10).setCellValue(markerInfo.getGeometryJson());
                row.createCell(11).setCellValue(markerInfo.getIsFinish());
                row.createCell(12).setCellValue(markerInfo.getSpotRectificationSituation());
                row.createCell(13).setCellValue(TimeUtil.getDateToString(markerInfo.getCheckTime(), Config.timeFormat1));

                // 添加图片
                List<ImageInfo> imgList = DbUtil.Companion.getDb().queryImageInfoByLinkIdAndDataType(
                        markerInfo.getId() + "", ImageInfo.IMAGE_MARKER);

                if (CommonUtil.matchList(imgList)) {
                    // 设置行高以容纳图片
                    row.setHeight((short) (20 * 40)); // 大约2厘米高

                    // 创建绘图对象
                    Drawing drawing = sheet.createDrawingPatriarch();


                    // 添加多张图片
                    for (int j = 0; j < imgList.size(); j++) {
                        ImageInfo imageInfo = imgList.get(j);
                        // 计算图片位置 - 每行最多显示3张图片
                        int colOffset = j % 9;
                        addImageToExcel2(wb, drawing, imageInfo.getFilePath(), i + 1, 14 + colOffset, colOffset);
                    }
                }
            }


            // 写入文件
            OutputStream os = new FileOutputStream(filePath + "工作记录.xls");
            wb.write(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }


    /**
     * 将图片添加到Excel指定位置
     * @param workbook 工作簿对象
     * @param drawing 绘图对象
     * @param imagePath 图片路径
     * @param rowNum 行号(0-based)
     * @param startColNum 起始列号(0-based)
     * @param colOffset 列偏移量
     */
    private void addImageToExcel2(HSSFWorkbook workbook, Drawing drawing,
                                 String imagePath, int rowNum,
                                 int startColNum, int colOffset) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) return;

            // 读取图片文件
            InputStream is = new FileInputStream(imageFile);
            byte[] bytes = IOUtils.toByteArray(is);
            is.close();

            // 添加图片到工作簿
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);

            // 创建锚点，设置图片位置
            ClientAnchor anchor = new HSSFClientAnchor(
                    0, 0, 0, 0,
                    (short) (startColNum + colOffset), rowNum,
                    (short) (startColNum + colOffset + 1), rowNum + 1
            );

            // 插入图片
            drawing.createPicture(anchor, pictureIdx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 保留其他原有方法...
    private String typeChange(int type) {
        String str = "";
        if (type == 0) {
            str = "点";
        } else if (type == 1) {
            str = "线";
        } else if (type == 2) {
            str = "面";
        } else if (type == 3) {
            str = "文字";
        }
        return str;
    }

    private void copyFile(String sourcePath, String destPath) throws IOException {
        File source = new File(sourcePath);
        if (!source.exists()) {
            return;
        }
        File dest = new File(destPath);
        dest.createNewFile();
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
}