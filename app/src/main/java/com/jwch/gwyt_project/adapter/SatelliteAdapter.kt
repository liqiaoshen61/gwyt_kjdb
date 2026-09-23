package com.jwch.gwyt_project.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jameni.jamenilistlib.adapter.BrvahAdapter
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.model.SatelliteModel
import com.jwch.gwyt_project.model.constellationColor
import com.jwch.gwyt_project.model.constellationName
import java.util.Locale

/**
 * 卫星列表 Adapter（BRVAH）
 * 每行：星座 / 编号 / 信号强度条 / 仰角 / 方位角 / 频率 / 状态
 */
class SatelliteAdapter : BrvahAdapter<SatelliteModel>(R.layout.item_satellite) {

    override fun convert(helper: BaseViewHolder?, item: SatelliteModel?) {
        if (helper == null || item == null) return
        val color = constellationColor(item.constellationType)

        val tvConstellation = helper.getView<TextView>(R.id.tvSatConstellation)
        tvConstellation.text = constellationName(item.constellationType)
        tvConstellation.setTextColor(color)

        helper.setText(R.id.tvSatSvid, item.svid.toString())
        helper.setText(R.id.tvSatElevation, "${item.elevation.toInt()}°")
        helper.setText(R.id.tvSatAzimuth, "${item.azimuth.toInt()}°")
        helper.setText(R.id.tvSatCn0, String.format(Locale.CHINA, "%.0f", item.cn0))

        val tvFreq = helper.getView<TextView>(R.id.tvSatFreq)
        tvFreq.text = if (item.carrierFreqHz > 0) {
            String.format(Locale.CHINA, "%.1f", item.carrierFreqHz / 1e6)
        } else {
            "—"
        }

        // 信号强度条：颜色跟随星座
        val bar = helper.getView<ProgressBar>(R.id.tvSatSignalBar)
        bar.max = 55
        bar.progress = item.cn0.toInt().coerceIn(0, 55)
        bar.progressTintList = ColorStateList.valueOf(color)
        bar.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#22364A"))

        // 是否参与定位
        val tvUsed = helper.getView<TextView>(R.id.tvSatUsed)
        tvUsed.text = if (item.usedInFix) "用" else ""
        tvUsed.setTextColor(if (item.usedInFix) color else Color.parseColor("#7E97AD"))
    }
}
