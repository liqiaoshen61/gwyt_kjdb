package com.jwch.gwyt_project.util.mpchart;


import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jwch.gwyt_project.util.mpchart.model.ChartData;


import java.text.DecimalFormat;
import java.util.List;

public class ChartValuePercentFormatUtil extends ValueFormatter {

    private List<ChartData> list;

    public ChartValuePercentFormatUtil(List<ChartData> list) {
        this.list = list;
    }

    @Override
    public String getFormattedValue(float value) {
        DecimalFormat mFormat = new DecimalFormat("###,###,##0");
        if(value==0){
            return "0";
        }else {
            return mFormat.format(value) + " %";
        }

    }



}
