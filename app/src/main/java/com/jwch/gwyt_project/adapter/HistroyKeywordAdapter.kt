package com.jwch.gwyt_project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.ext.printMsg
import com.jwch.gwyt_project.ext.self
import org.jetbrains.anko.find

class HistroyKeywordAdapter(val context: Context, list: MutableList<String>?, maxMatch: Int = -1) :
    BaseAdapter(), Filterable {


    var original_list: MutableList<String>? = null // 原始数据
    var filter_list: MutableList<String>? = null// 过滤后的数据
    private var maxMatch = -1 // 最大显示的数据条数 默认不限制
    var mFilter: ArrayListFilter? = null
    private val mLock = Any()

    init {
        this.original_list = list
        this.maxMatch = maxMatch
    }

    override fun getCount(): Int {
        return filter_list?.size.self()
    }

    override fun getItem(position: Int): Any? {
        return filter_list?.get(position);
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var cView: View
        var holder: ViewHolder
        if (convertView == null) {
            cView = LayoutInflater.from(context).inflate(R.layout.item_history_keyword, null)

            holder = ViewHolder()
            holder.tvAccountName = cView.find(R.id.tvName)
            holder.llItem = cView.find(R.id.llItem)
            cView?.tag = holder
        } else {
            cView = convertView
            holder = convertView.tag as ViewHolder
        }

        val data = getItem(position) as String
        data.apply {
            holder.tvAccountName?.text = this
        }



        return cView
    }

    internal class ViewHolder {
        var tvAccountName: TextView? = null
        var llItem: LinearLayout? = null
    }

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayListFilter()
        }
        return mFilter as ArrayListFilter
    }

    fun getAllItems(): MutableList<String>? {
        return original_list
    }

    inner class ArrayListFilter : Filter() {

        override fun performFiltering(prefix: CharSequence?): FilterResults {

            val results = FilterResults()
            if (original_list == null) {
                "无数据可筛选".printMsg()
            }
//            //如果没有输入，那么显示所有
            if (prefix.isNullOrBlank()) {
                synchronized(mLock) {
                    val list = original_list
                    results.values = list
                    results.count = list?.size.self()
                }
            } else {
                val prefixString = prefix.toString().toLowerCase()
                val newValues = mutableListOf<String>()
                original_list?.forEach {
                    val account = it.self()
                    val accountText = account.toLowerCase()

                    if (accountText.contains(prefixString)) { //过滤规则
                        newValues.add(it)
                    }
                    if (maxMatch > 0) { //有数量限制
                        if (newValues.size > maxMatch - 1) { //不要太多
                            return@forEach
                        }
                    }
                }

                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(arg0: CharSequence?, results: FilterResults) {

            if (results.values != null) {
                filter_list = results.values as MutableList<String>

                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }


            }
        }
    }

}