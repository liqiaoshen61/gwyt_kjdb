package com.jwch.gwyt_project.util.mpchart;


import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jwch.gwyt_project.util.mpchart.model.ChartData;


import java.util.List;

public class ChartYAxisValueFormatUtil2 extends ValueFormatter {

    private List<ChartData> list;

    public ChartYAxisValueFormatUtil2(List<ChartData> list) {
        this.list = list;
    }

    //输出小数
    @Override
    public String getFormattedValue(float value) {

        String result = "";

        if(value == 0){
            //result = (int) value + "";
            result = "";
        }else {
            result = (float) value + "";
        }

        return result;
    }




}
