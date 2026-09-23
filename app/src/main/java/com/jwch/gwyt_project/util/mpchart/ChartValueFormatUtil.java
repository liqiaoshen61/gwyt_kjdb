package com.jwch.gwyt_project.util.mpchart;


import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jameni.allutillib.common.CommonUtil;
import com.jwch.gwyt_project.util.mpchart.model.ChartData;


import java.util.List;

public class ChartValueFormatUtil extends ValueFormatter {

    private List<ChartData> list;

    public ChartValueFormatUtil(List<ChartData> list) {
        this.list = list;
    }

    @Override
    public String getFormattedValue(float value) {

        String result = "";
        int position = (int) value;
        if (position > -1 && CommonUtil.matchList(list) && list.size() > position) {
            result = CommonUtil.getSelfValue(list.get(position).getxLable());
        }

        if(result.contains(":00:00")){
            result = result.substring(0,result.indexOf(":")) +"时";
        }
        return result+"";
    }



}
