package com.jwch.gwyt_project.util.mpchart;


import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jwch.gwyt_project.util.mpchart.model.ChartData;

import java.util.List;


public class ChartYAxisValueFormatUtil extends ValueFormatter {

    private List<ChartData> list;

    public ChartYAxisValueFormatUtil(List<ChartData> list) {
        this.list = list;
    }

    //截取整数
    @Override
    public String getFormattedValue(float value) {

        String result = "";

//        if(value != 0){
//            result = (int) value + "";
//        }

        result = (int) value + "";

        return result;
    }



}
