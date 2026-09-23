package com.jwch.gwyt_project.util;

import android.graphics.Color;

//import com.github.mikephil.charting.components.LegendEntry;
//import com.github.mikephil.charting.data.PieEntry;


/**
 * 问题统计饼状图
 */
public class PieUtil {

    private int pieColor1 = Color.parseColor("#c2a381");//
    private int pieColor2 = Color.parseColor("#b11211");//
    private int pieColor3 = Color.parseColor("#a1cfbb");//
    private int pieColor4 = Color.parseColor("#9a652c");//
    private int pieColor5 = Color.parseColor("#c87858");//
    private int pieColor6 = Color.parseColor("#709ea8");//
    private int pieColor7 = Color.parseColor("#e5b175");//
    private int pieColor8 = Color.parseColor("#7e9f85");
    private int pieColor9 = Color.parseColor("#334453");
    private int pieColor10 = Color.parseColor("#69310b");
    private int blackColor = Color.rgb(80, 80, 80);
    private int animateTime = 900;
    private float legendTextSize = 13f;
    private float margin = 10f;//饼状图距离边缘
    private float legendSpace = 15f;//图例距离边缘距离
    private int holeSize = 110;//中间镂空的大小

    //初始化饼状图 含折线
//    public void initPiePolylineChart(PieChart chart, List<ChartKVModel> dataList) {
//
//        if (chart == null || dataList == null) return;
//
//        chart.setNoDataText("暂无数据");
//        chart.setUsePercentValues(true);//展示百分号
//        chart.getDescription().setEnabled(false);
////        chart.setExtraOffsets(60.f, 0.f, 120.f, 0.f);
//        chart.setDragDecelerationFrictionCoef(0.95f);
//
//        chart.setCenterText("");
//        chart.setDrawCenterText(true);
//
//
//        chart.setDrawHoleEnabled(true);//支持画中间空心
//        chart.setHoleColor(Color.TRANSPARENT);//空心颜色
//
////        chart.setTransparentCircleColor(Color.TRANSPARENT);
//        chart.setTransparentCircleAlpha(holeSize);
//
//        chart.setHoleRadius(28f);
//        chart.setTransparentCircleRadius(31f);
//
//        chart.setRotationAngle(0);
//        chart.setHighlightPerTapEnabled(true);
//
//        Legend l = chart.getLegend();
//        l.setEnabled(false);
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        l.setOrientation(Legend.LegendOrientation.VERTICAL);
//        l.setForm(Legend.LegendForm.SQUARE);
//        l.setTextColor(blackColor);
//        l.setTextSize(legendTextSize);
////        l.setXOffset(legendSpace);
////        l.setFormSize(12f);
////        l.setXEntrySpace(4f);
//        l.setDrawInside(false);
//        l.setYOffset(10f);
//
//
//        setPieData(chart, dataList);
//        //和边缘的距离
//        chart.setExtraOffsets(margin * 2, margin, margin * 2, margin * 1.5f);
//        //中间镂空的颜色
//        chart.setHoleColor(Color.WHITE);
//        //在数值后面添加百分号%
//        chart.setUsePercentValues(true);
//        //饼状图里面的lable字的颜色
//        chart.setEntryLabelColor(blackColor);
//        chart.setDrawEntryLabels(false);//饼状图一块块里面不写文本
//
//        //说明
////        Description description = new Description();
////        description.setText("dddddddddddddd");
////        chart.setDescription(description);
//
//
//        for (LegendEntry item : l.getEntries()) {
//            PrintUtil.printMsg("color:" + item.formColor + "   lable:" + item.label);
//        }
//
//    }


//    private void setPieData(PieChart chart, List<ChartKVModel> dataList) {
//
//
//        if (dataList == null) return;
//
//        ArrayList<PieEntry> entries = new ArrayList<>();
//
//        for (int i = 0; i < dataList.size(); i++) {
////            entries.add(new PieEntry((float) (Math.random() * range) + range / 5, parties[i % parties.length]));
//            entries.add(new PieEntry(dataList.get(i).getValueInteger(), dataList.get(i).getKey()));
//        }
//
//        PieDataSet dataSet = new PieDataSet(entries, "");
//        dataSet.setSliceSpace(5f);//设置选中的Tab离两边的距离
//        dataSet.setSelectionShift(10f);//设置选中的tab的多出来的
//        // add a lot of colors
//        ArrayList<Integer> colors = new ArrayList<>();
//        colors.add(pieColor1);
//        colors.add(pieColor2);
//        colors.add(pieColor3);
//        colors.add(pieColor4);
//        colors.add(pieColor5);
//        colors.add(pieColor6);
//        colors.add(pieColor7);
//        colors.add(pieColor8);
//        colors.add(pieColor9);
//        colors.add(pieColor10);
//
//        if (CommonUtil.matchList(dataList) && dataList.size() > 10) {
//            for (int c : ColorTemplate.VORDIPLOM_COLORS)
//                colors.add(c);
//
//            for (int c : ColorTemplate.JOYFUL_COLORS)
//                colors.add(c);
//
//            for (int c : ColorTemplate.COLORFUL_COLORS)
//                colors.add(c);
//
//            for (int c : ColorTemplate.LIBERTY_COLORS)
//                colors.add(c);
//
//            for (int c : ColorTemplate.PASTEL_COLORS)
//                colors.add(c);
//
//            colors.add(ColorTemplate.getHoloBlue());
//        }
//
//
//        dataSet.setColors(colors);
//        dataSet.setValueLineColor(blackColor);//指标线的颜色
//
//
//        dataSet.setValueLinePart1OffsetPercentage(80.f);//折线的角度
//        dataSet.setValueLinePart1Length(0.2f);
//        dataSet.setValueLinePart2Length(0.8f);
//
//        //dataSet.setUsingSliceColorAsValueLineColor(true);
//
////        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//
//        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new MyPercentFormater(chart));
//        data.setValueTextSize(13f);
//        data.setValueTextColor(blackColor);//折线上的字颜色
//        chart.setData(data);
//
//
//        // undo all highlights
//        chart.highlightValues(null);
//
//        chart.invalidate();
////        chart.animateXY(600, 600);
//        chart.animateXY(0,0);
//    }


}
