package com.jwch.gwyt_project.common;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;


import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.jameni.allutillib.common.PrintUtil;
import com.jwch.gwyt_project.Info.PasswordInfo;
import com.jwch.gwyt_project.Info.PoisInfo;
import com.jwch.gwyt_project.core.Config;
import com.jwch.gwyt_project.core.AppContext;
import com.jwch.gwyt_project.db.DbUtil;
import com.jwch.gwyt_project.util.CryptoUtils;

import org.xutils.ex.DbException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Tools {

    public static String getVersionName() {
        PackageManager manager = AppContext.app.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = manager.getPackageInfo(
                    AppContext.app.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;

    }

    /**
     * 是否开启锁屏密码对话框
     *
     * @return
     */
    public static Boolean getIsOpenLock() {
        boolean flag = false;
        try {
            PasswordInfo p = DbUtil.Companion.getDb().getPtDb().selector(PasswordInfo.class).findFirst();
            if (p != null) {
                if (p.getIs_show() == 1) {
                    flag = true;
                } else {
                    flag = false;
                }
            } else {
                flag = false;
            }
        } catch (DbException e) {

        }
        return flag;
    }

    /**
     * @param path
     * @return
     */

    public static String getTpkPath(String path) {
        if (!path.contains(".tpk")) {
            if (new File(Config.EMAPDATA_PATH + path + "/layers").exists()) {
                path = path + "/layers";
            } else if (new File(Config.EMAPDATA_PATH + path + "v101/图层").exists()) {
                path = path + "/图层";
            } else if (new File(Config.EMAPDATA_PATH + path + "/Layers").exists()) {
                path = path + "/Layers";
            }
        }
        return path;
    }


    public static String getTpkFilePath(String folderPath, String fileName) {
        File folder = new File(folderPath);
        String tpkFileName = fileName + ".tpk"; // 拼接.tpk后缀
        File tpkFile = new File(folder, tpkFileName);

        // 检查文件夹是否存在（folderPath + fileName）
        File nameFolder = new File(folder, fileName);
        if (nameFolder.exists() && nameFolder.isDirectory()) {
            // 如果文件夹存在，返回 "文件名/v101/图层" 的绝对路径
            File newPath = new File(nameFolder, "v101/图层");
            return newPath.getAbsolutePath();
        } else if (tpkFile.exists()) {
            // 如果.tpk文件存在，返回其绝对路径
            return tpkFile.getAbsolutePath();
        } else {
            // 否则返回空字符串
            return "";
        }
    }

    public static String getVtpkFilePath(String folderPath, String fileName) {
        File folder = new File(folderPath);
        String tpkFileName = fileName + ".vtpk"; // 拼接.tpk后缀
        File tpkFile = new File(folder, tpkFileName);

        // 检查文件夹是否存在（folderPath + fileName）
        File nameFolder = new File(folder, fileName);
        if (nameFolder.exists() && nameFolder.isDirectory()) {
            // 如果文件夹存在，返回 "文件名/v101/图层" 的绝对路径
            File newPath = new File(nameFolder, "v101/图层");
            return newPath.getAbsolutePath();
        } else if (tpkFile.exists()) {
            // 如果.tpk文件存在，返回其绝对路径
            return tpkFile.getAbsolutePath();
        } else {
            // 否则返回空字符串
            return "";
        }
    }

    public static ArcGISTiledLayer getTiledLayerByPath(String folderPath, String fileName) {

        String layerPath = getTpkFilePath(folderPath, fileName);
        if (layerPath.contains(".tpk")) {
            return new ArcGISTiledLayer(layerPath);
        } else {
            TileCache tileCache = new TileCache(layerPath);
            return new ArcGISTiledLayer(tileCache);
        }
    }

    public static ArcGISVectorTiledLayer getVectorTiledLayerByPath(String folderPath, String fileName) {
        String layerPath = getVtpkFilePath(folderPath, fileName);

        if (layerPath.contains(".vtpk")) {
            String realPath = CryptoUtils.INSTANCE.decryptTpk(layerPath);
            return new ArcGISVectorTiledLayer(realPath);
        }
        return null;
    }


    public static List<PoisInfo> changData(Cursor cursor) {
        List<PoisInfo> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {

            do {
                PoisInfo poi = new PoisInfo();

                poi.setPoiName(cursor.getString(cursor
                        .getColumnIndex("PoiName")));
                poi.setX(cursor.getDouble(cursor
                        .getColumnIndex("X")));
                poi.setY(cursor.getDouble(cursor
                        .getColumnIndex("Y")));
                poi.setTypeName(cursor.getString(cursor
                        .getColumnIndex("TypeName")));
                poi.setAddress(cursor.getString(cursor
                        .getColumnIndex("Address")));
                poi.setCityName(cursor.getString(cursor
                        .getColumnIndex("CityName")));
                poi.setCountyName(cursor.getString(cursor
                        .getColumnIndex("CountyName")));
                poi.setTownName(cursor.getString(cursor
                        .getColumnIndex("TownName")));
                poi.setDistCode(cursor.getString(cursor
                        .getColumnIndex("DistCode")));
//                poi.setId(cursor.getInt(cursor.getColumnIndex("Id")));
                poi.setId(cursor.getString(cursor.getColumnIndex("Id")));
                list.add(poi);
            } while (cursor.moveToNext());

        }
        return list;
    }

    public static String getPhotoPath() {
        Date date = new Date();
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeString = dateformat1.format(date);
        String filename = Config.FILE_CAMERA_PATH + timeString + ".png";

        File file_2 = new File(Config.FILE_CAMERA_PATH);
        if (!file_2.exists()) {
            file_2.mkdirs();
        }
        return filename;
    }

    /**
     * @return
     */
    public static String getTime() {
        Date date = new Date();
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateformat1.format(date);
    }

    public static String getPhotoOnlyPath(String name) {
        String filename = Config.FILE_CAMERA_PATH + name + ".png";
        File file_2 = new File(Config.FILE_CAMERA_PATH);
        if (!file_2.exists()) {
            file_2.mkdirs();
        }
        return filename;
    }

    //文件重命名
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);

        if (oleFile.exists()) {
            PrintUtil.printMsg("old 存在");
        } else {
            PrintUtil.printMsg("old 不存在");
        }
        if (newFile.exists()) {
            PrintUtil.printMsg("newFile 存在");
        } else {
            PrintUtil.printMsg("newFile 不存在");
        }
        boolean flag = oleFile.renameTo(newFile);

        if (flag) {
            PrintUtil.printMsg("成功");
        } else {
            PrintUtil.printMsg("失败");
        }

    }

    /**
     * 复制文件
     *
     * @param oldPath 源文件路径
     * @param newPath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        // 检查源文件是否存在
        if (!oldFile.exists()) {
            System.out.println("源文件不存在: " + oldPath);
            return false;
        }

        // 检查目标文件的目录是否存在，如果不存在则创建
        File parentDir = newFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirCreated = parentDir.mkdirs();
            if (!dirCreated) {
                System.out.println("创建目录失败: " + parentDir.getAbsolutePath());
                return false;
            }
        }

        // 如果目标文件已存在，可以选择删除或跳过
        if (newFile.exists()) {
            System.out.println("目标文件已存在，将被覆盖");
            boolean deleted = newFile.delete();
            if (!deleted) {
                System.out.println("删除已存在文件失败");
                return false;
            }
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(oldFile);
            outputStream = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            System.out.println("文件复制成功: " + oldPath + " -> " + newPath);
            return true;

        } catch (IOException e) {
            System.out.println("文件复制失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 使用 try-with-resources 的版本（需要 Java 7+）
     */
    public static  boolean copyFileModern(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        // 检查源文件是否存在
        if (!oldFile.exists()) {
            System.out.println("源文件不存在: " + oldPath);
            return false;
        }

        // 检查目标文件的目录是否存在，如果不存在则创建
        File parentDir = newFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirCreated = parentDir.mkdirs();
            if (!dirCreated) {
                System.out.println("创建目录失败: " + parentDir.getAbsolutePath());
                return false;
            }
        }

        // 如果目标文件已存在，可以选择删除或跳过
        if (newFile.exists()) {
            System.out.println("目标文件已存在，将被覆盖");
            boolean deleted = newFile.delete();
            if (!deleted) {
                System.out.println("删除已存在文件失败");
                return false;
            }
        }

        try (InputStream inputStream = new FileInputStream(oldFile);
             OutputStream outputStream = new FileOutputStream(newFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            System.out.println("文件复制成功: " + oldPath + " -> " + newPath);
            return true;

        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;


        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getFileType(String filename) {
        for (String[] strings : MATCH_ARRAY) {

            if (filename.contains(strings[0])) {
                return strings[1];
            }
        }
        return null;
    }

    private static final String[][] MATCH_ARRAY = {

            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    public static Bitmap decodeSampleBitmapFromResource(String path, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);


        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;


        return BitmapFactory.decodeFile(path, options);

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);


            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

        }
        return inSampleSize;

    }

    /**
     * @param path
     * @return
     */
    public static String getFileSize(String path) {
        String size = "";
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            long length = f.length();
            DecimalFormat df = new DecimalFormat("#.00");
            if (length < 1024) {
                size = df.format((double) length) + "BT";
            } else if (length < 1048576) {
                size = df.format((double) length / 1024) + "KB";
            } else if (length < 1073741824) {
                size = df.format((double) length / 1048576) + "MB";
            } else {
                size = df.format((double) length / 1073741824) + "GB";
            }
        }
        return size;
    }
}