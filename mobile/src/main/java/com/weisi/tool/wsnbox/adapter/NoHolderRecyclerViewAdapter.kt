package com.weisi.tool.wsnbox.adapter

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter

abstract class NoHolderRecyclerViewAdapter<E> : RecyclerViewBaseAdapter<E>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(onCreateView(parent, viewType))
    }

    protected abstract fun onCreateView(parent: ViewGroup?, viewType: Int): View

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder as ViewHolder, getItemByPosition(position), position)
    }

    protected abstract fun onBindViewHolder(holder: ViewHolder, item: E, position: Int)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: E, position: Int, payloads: MutableList<Any?>?) {
        onBindViewHolder(holder as ViewHolder, position, item, payloads!!)
    }

    protected open fun onBindViewHolder(holder: ViewHolder, position: Int, item: E, payloads: MutableList<Any?>) {
        onBindViewHolder(holder, item, position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun <T : View> findView(viewId: Int): T {
            return itemView.findViewOften(viewId)
        }

        fun <T : View> View.findViewOften(viewId: Int): T {
            val viewHolder: SparseArray<View> = tag as? SparseArray<View> ?: SparseArray()
            tag = viewHolder
            var childView: View? = viewHolder.get(viewId)
            if (null == childView) {
                childView = findViewById(viewId)
                viewHolder.put(viewId, childView)
            }
            return childView as T
        }
    }
}