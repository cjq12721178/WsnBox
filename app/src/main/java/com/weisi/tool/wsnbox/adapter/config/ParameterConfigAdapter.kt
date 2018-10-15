package com.weisi.tool.wsnbox.adapter.config

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.tool.qbox.ui.adapter.RecyclerViewCursorAdapter
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.io.Constant
import kotlinx.android.synthetic.main.li_para_config.view.*

/**
 * Created by CJQ on 2018/3/5.
 */
class ParameterConfigAdapter : RecyclerViewCursorAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_para_config,
                        parent,
                        false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, item: Cursor?, position: Int) {
        var holder = viewHolder as ViewHolder
        holder.tvName?.text = getProviderName(item!!)
    }

    fun getProviderName(position: Int) : String {
        return getProviderName(getItemByPosition(position))
    }

    private fun getProviderName(item: Cursor) : String {
        return item.getString(item.getColumnIndex(Constant.COLUMN_CONFIGURATION_PROVIDER_NAME))
    }

    fun findProviderNameById(id: Long) : String {
        val c = cursor
        if (id != 0L && c !== null && c.moveToFirst()) {
            val idIndex = c.getColumnIndex(Constant.COLUMN_COMMON_ID)
            do {
                if (id == c.getLong(idIndex)) {
                    return getProviderName(c)
                }
            } while (c.moveToNext())
        }
        return ""
    }

    private class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var tvName = itemView?.tv_config_provider_name
    }
}