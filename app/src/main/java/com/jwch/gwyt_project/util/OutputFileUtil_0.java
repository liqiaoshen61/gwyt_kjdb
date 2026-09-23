package com.jwch.gwyt_project.util;

import android.os.Handler;
import android.os.Message;

import com.jameni.allutillib.common.CommonUtil;
import com.jameni.allutillib.common.PrintUtil;
import com.jameni.allutillib.common.TimeUtil;
import com.jwch.gwyt_project.Info.ImageInfo;
import com.jwch.gwyt_project.Info.MarkerInfo;
import com.jwch.gwyt_project.common.Tools;
import com.jwch.gwyt_project.core.Config;
import com.jwch.gwyt_project.db.DbUtil;
import com.jwch.gwyt_project.model.GeoCollectionModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.DbException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class OutputFileUtil_0 {
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


    /**
     * @param info
     * @return
     */
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
        String filePath = Config.OUTPUT_PATH + "工作记录" +Tools.getTime() + "/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("工作记录");

        HSSFRow row = sheet.createRow(0);

        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFCell cell = null;
        String[] title = {"河流名称",
                "所在市",
                "所在县",
                "问题分类",
                "问题属性",
                "严重程度",
                "问题描述",
                "发生位置描述",
                "坐标",
                "复核时间",
                "图片路径"
        };
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }
        String[][] values = new String[0][];
        try {
            values = markerToStrings(list, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < values.length; i++) {
            row = sheet.createRow(i + 1);
            for (int j = 0; j < values[i].length; j++) {
                row.createCell(j).setCellValue(values[i][j]);
            }
        }
        try {
            OutputStream os = new FileOutputStream(filePath + "工作记录.xls");
            wb.write(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private String[][] markerToStrings(List<GeoCollectionModel> list, String path) throws IOException {
        String[][] strs = new String[list.size()][11];
        new File(path + "照片/").mkdirs();
//        new File(path + "附件/").mkdirs();·
        for (int i = 0; i < list.size(); i++) {
            MarkerInfo markerInfo = list.get(i).getMarkerInfo();

            strs[i][0] = markerInfo.getRiverName(); //河流名称
            strs[i][1] = markerInfo.getCity();  //所在市
            strs[i][2] = markerInfo.getCounty();  //所在县
            strs[i][3] = markerInfo.getQuestionType();
            strs[i][4] = markerInfo.getQuestionAttr();
            strs[i][5] = markerInfo.getSeverity();
            strs[i][6] = markerInfo.getDescription();
            strs[i][7] = markerInfo.getLocation();
            strs[i][8] = markerInfo.getGeometryJson();
            strs[i][9] = TimeUtil.getDateToString(markerInfo.getCheckTime(), Config.timeFormat1);

            strs[i][10]  = "";//相册

            try {

//                strs[i][3] = typeChange(list.get(i).getMarkerInfo().getGeoType());
//                strs[i][4] = GeoToWKT(list.get(i).getMarkerInfo());GeoToWKT(markerInfo);

                List<ImageInfo> imgList = DbUtil.Companion.getDb().queryImageInfoByLinkIdAndDataType(list.get(i).getMarkerInfo().getId() + "", ImageInfo.IMAGE_MARKER);
                if (CommonUtil.matchList(imgList)) {
                    StringBuffer sb = new StringBuffer();

                    for (int j = 0; j < imgList.size(); j++) {
                        ImageInfo item = imgList.get(j);
                        File file = new File(item.getFilePath());
                        String fileName = file.getName();
                        PrintUtil.printMsg("照片文件名：" + fileName);
                        copyFile(item.getFilePath(), path + "图片/" + fileName);
                        sb.append(fileName);
                        if (j < imgList.size() - 1) {
                            sb.append(",");
                        }
                    }
                    strs[i][10] = sb.toString();//相册
                }

                //没有附件
//                strs[i][10] = "";
//                List<AccessoryInfo> accessoryList = DbUtil.Companion.getDb().queryAccessoryInfoByLinkIdAndDataType(list.get(i).getMarkerInfo().getId() + "", AccessoryInfo.ACCESSORY_MARKER);
//                if (CommonUtil.matchList(accessoryList)) {
//                    StringBuffer sb = new StringBuffer();
//
//                    for (int j = 0; j < accessoryList.size(); j++) {
//                        AccessoryInfo item = accessoryList.get(j);
//                        File file = new File(item.getPath());
//                        String fileName = file.getName();
//                        PrintUtil.printMsg("附件文件名：" + fileName);
//                        copyFile(item.getPath(), path + "附件/" + fileName);
//                        sb.append(fileName);
//                        if (j < imgList.size() - 1) {
//                            sb.append(",");
//                        }
//                    }
//                    strs[i][10] = sb.toString();//相册
//                }



            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return strs;
    }

    /**
     * @param type
     * @return
     */
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


    /**
     * @throws IOException
     */
    private void copyFile(String sourcePath, String destPath)
            throws IOException {
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
