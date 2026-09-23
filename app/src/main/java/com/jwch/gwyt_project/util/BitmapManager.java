package com.jwch.gwyt_project.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapManager {
    private Context context;

    public BitmapManager(Context context) {
        this.context = context;
    }

    public  File createFile(String fileName, String dirName) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsoluteFile() + File.separator + dirName;
        File mIVMSFolder = new File(path);
        if (!mIVMSFolder.exists()) {
            mIVMSFolder.mkdirs();
        }
        return new File(mIVMSFolder.getAbsolutePath(), fileName);
    }
    public Uri getUriFromFile(Context context, File file){
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context,context.getPackageName()+".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
    public void getPhoto(File mFile, byte[] imageData, int mCameraId, int takePhotoOrientation,
                         Bitmap watermarkBitmap, Camera.Parameters parameter) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            fos.write(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    // 获得图片
                    Bitmap mBitmap = BitmapFactory.decodeFile(mFile.getPath());
                    //添加水印
                    Bitmap newBitmap = AddTimeWatermark(mBitmap, mCameraId, takePhotoOrientation, watermarkBitmap, parameter);
                    if (onBitmapCompleteListener != null)
                        onBitmapCompleteListener.OnBitmapComplete(newBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Bitmap AddTimeWatermark(Bitmap mBitmap, int mCameraId, int takePhotoOrientation,
                                   Bitmap watermarkBitmap,Camera.Parameters parameter) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) takePhotoOrientation);
        //前置摄像头
        if(mCameraId == 1){
            if(takePhotoOrientation == 90){
                matrix.postRotate(180f);
            }
        }

        //由于通过BitmapFactory得到的位图不能通过矩阵进行一系列转换，因此必须通过Bitmap.createBitmap重新创建
        //createBitmap参数：原图、裁剪的起点x坐标、裁剪的起点y坐标、裁剪的宽度、裁剪的高度、矩阵（用于旋转）、过滤器？（如果为true，源图要被过滤）
        Bitmap mNewBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        //如果是前置摄像头 需要镜面翻转处理
        if(mCameraId == 1){
            Matrix matrix1 = new Matrix();
            matrix1.postScale(-1f,1f);
            mNewBitmap = Bitmap.createBitmap(mNewBitmap, 0, 0,
                    mNewBitmap.getWidth(), mNewBitmap.getHeight(), matrix1, true);
        }

        //创建一个尺寸与原图相同的空位图 用于创建画布
        Bitmap blankBitmap = Bitmap.createBitmap(mNewBitmap.getWidth(), mNewBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(blankBitmap);
        //向位图中开始画入MBitmap原始图片
        mCanvas.drawBitmap(mNewBitmap,0,0,null);


        // 计算缩放比例
        float scaleWidth = ((float) parameter.getPictureSize().width) / parameter.getPreviewSize().width;
        float scaleHeight = ((float) parameter.getPictureSize().height) /parameter.getPreviewSize().height;
        // 取得想要缩放的matrix参数
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(scaleWidth, scaleHeight);
        // 得到新的水印图片
        Bitmap newWatermarkBitmap = Bitmap.createBitmap(watermarkBitmap, 0, 0,
                watermarkBitmap.getWidth(), watermarkBitmap.getHeight(), scaleMatrix, true);

        int px10 = SystemUtil.dp2px(context,10);
        //吧水印图片画到照片上
        mCanvas.drawBitmap(newWatermarkBitmap,px10,
                mNewBitmap.getHeight()- (watermarkBitmap.getHeight()*scaleHeight)- px10,null);


        mCanvas.save();
        mCanvas.restore();
        return blankBitmap;
    }

    public  void drawPosition(Bitmap mNewBitmap, Canvas mCanvas, String str, int drawable,
                              int x, int y, int type) {
        if (TextUtils.isEmpty(str)) return;

        Bitmap resource = BitmapFactory.decodeResource(context.getResources(), drawable);
        int width = resource.getWidth();
        int height = resource.getHeight();
        // 设置想要的大小
        int newWidth = 0;
        int newHeight = 0;
        int disX = 0;
        int disY = 0;
        switch (type){
            case 0:
                newWidth = SystemUtil.dp2px(context,3);
                newHeight = SystemUtil.dp2px(context,15);
                disX = SystemUtil.dp2px(context,6);
                disY = SystemUtil.dp2px(context,5);
                break;
            case 1:
                newWidth = SystemUtil.dp2px(context,15);
                newHeight = SystemUtil.dp2px(context,15);
                disX = SystemUtil.dp2px(context,0);
                disY = SystemUtil.dp2px(context,4);
                break;
            case 2:
                newWidth = SystemUtil.dp2px(context,5);
                newHeight = SystemUtil.dp2px(context,5);
                disX = SystemUtil.dp2px(context,6);
                disY = SystemUtil.dp2px(context,5);
                break;
        }
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix2 = new Matrix();
        matrix2.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        resource = Bitmap.createBitmap(resource, 0, 0, width, height, matrix2, true);
        mCanvas.drawBitmap(resource, x+disX, y+disY, null);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(type == 0 ?
                SystemUtil.dp2px(context,16) : SystemUtil.dp2px(context,12));
        //StaticLayout 处理了文字换行的问题
        //参数：字符串str 、画笔对象textPaint、宽度width（超出时换行）、对齐方式、相对行间距spacingmult、
        //基础行距上增加spacingadd、是否留白 includepad
        StaticLayout staticLayout = new StaticLayout(str, textPaint,
                mNewBitmap.getWidth() - SystemUtil.dp2px(context,100),
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0, true);
        mCanvas.save();
        mCanvas.translate(x * 1.6F, y);
        staticLayout.draw(mCanvas);
        mCanvas.restore();

    }
    public void saveBitmapFile(File mFile, Bitmap bitmap){
        if (null == bitmap) return;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnBitmapCompleteListener{
        void OnBitmapComplete(Bitmap bitmap);
    }
    public OnBitmapCompleteListener onBitmapCompleteListener;

    public void setOnBitmapCompleteListener(OnBitmapCompleteListener onBitmapCompleteListener) {
        this.onBitmapCompleteListener = onBitmapCompleteListener;
    }
}
