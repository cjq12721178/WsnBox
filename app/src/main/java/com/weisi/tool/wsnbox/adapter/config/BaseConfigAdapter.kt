package com.weisi.tool.wsnbox.adapter.config

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import com.cjq.tool.qbox.ui.adapter.RecyclerViewCursorAdapter
import com.weisi.tool.wsnbox.R

open class BaseConfigAdapter : RecyclerViewCursorAdapter() {

    var inDeleteMode = false
    private val deleteSelections = mutableListOf<Int>()

    fun selectDeletingItem(position: Int) {
        if (position in 0..(itemCount - 1) && inDeleteMode) {
            val index = deleteSelections.binarySearch(position)
            if (index >= 0) {
                deleteSelections.removeAt(index)
            } else {
                deleteSelections.add(-index - 1, position)
            }
        }
    }

    fun changeDeleteModeWithNotification() {
        inDeleteMode = !inDeleteMode
        notifyItemRangeChanged(0, itemCount)
    }

    fun getDeletingItemsPosition() : List<Int> {
        return deleteSelections.toList()
    }

    fun hasDeletingItems() : Boolean {
        return !deleteSelections.isEmpty()
    }

    override fun scheduleItemRemove(position: Int): Int {
        deleteSelections.remove(position)
        return super.scheduleItemRemove(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Cursor?, position: Int) {
        val viewHolder = holder as ViewHolder
        if (inDeleteMode) {
            viewHolder.chkDelete.visibility = View.VISIBLE
            viewHolder.chkDelete.isChecked = deleteSelections.binarySearch(position) >= 0
        } else {
            viewHolder.chkDelete.visibility = View.GONE
        }
    }

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val chkDelete = itemView!!.findViewById<CheckBox>(R.id.chk_delete_selection)!!
    }
}