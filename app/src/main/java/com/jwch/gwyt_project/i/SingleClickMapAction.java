package com.jwch.gwyt_project.i;

import android.content.Context;
import android.view.MotionEvent;

import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class SingleClickMapAction extends DefaultMapViewOnTouchListener {

    private SingleClickMapListener singleClickMapListener;
    private DoubleClickMapListener doubleClickMapListener;
    private int tag;
    private int actionType;
    public static final int ACTION_CLICK = 0;//单击地图
    public static final int ACTION_DRAW_GEOMETY = 1; //在地图上画圈

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public SingleClickMapAction(Context context, MapView mapView, SingleClickMapListener singleClickMapListener) {
        super(context, mapView);
        this.singleClickMapListener = singleClickMapListener;
    }

    public SingleClickMapAction(Context context, MapView mapView, SingleClickMapListener singleClickMapListener, int tag) {
        super(context, mapView);
        this.singleClickMapListener = singleClickMapListener;
        this.tag = tag;
    }



    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (this.singleClickMapListener != null) {
            singleClickMapListener.onMapSingleClick(e, tag);
        }
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onRotate(MotionEvent event, double rotationAngle) {
//        return super.onRotate(event, rotationAngle);
        return false;//精致旋转
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {

        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (this.doubleClickMapListener != null) {
            doubleClickMapListener.onMapDoubleClick(e, tag);
        }
        return super.onDoubleTap(e);
    }
}
