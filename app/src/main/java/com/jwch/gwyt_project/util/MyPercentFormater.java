package com.jwch.gwyt_project.util;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.DecimalFormat;

public class MyPercentFormater extends PercentFormatter {


    public MyPercentFormater(PieChart pieChart) {
     // super(pieChart);
        mFormat = new DecimalFormat("###,###,##0.00");
    }
}
