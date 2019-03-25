package com.weisi.tool.wsnbox.adapter.config

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.io.Constant
import java.util.*

class DevicesConfigAdapter : BaseConfigAdapter() {

    private val names = NameList()

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_devices_config,
                        parent,
                        false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Cursor?, position: Int) {
        super.onBindViewHolder(holder, item, position)
        val viewHolder = holder as ViewHolder
        viewHolder.tvName.text = getDeviceName(item!!)
    }

    private fun getDeviceName(item: Cursor) =
            item.getString(item.getColumnIndex(Constant.COLUMN_DEVICE_NAME))

    fun getDeviceName(position: Int) =
            getDeviceName(getItemByPosition(position))

    fun getDevicesName(): Array<String> {
        return Array<String>(itemCount) {
            getDeviceName(it)
        }
    }

    fun findDeviceConfigByName(name: String) =
            Collections.binarySearch(names, name)

    class ViewHolder(itemView: View) : BaseConfigAdapter.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tv_device_name)
    }

    private inner class NameList : List<String>, RandomAccess {
        override val size: Int
            get() = itemCount

        override fun contains(element: String): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun containsAll(elements: Collection<String>): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun get(index: Int): String {
            return getDeviceName(index)
        }

        override fun indexOf(element: String): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isEmpty(): Boolean {
            return itemCount == 0
        }

        override fun iterator(): Iterator<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun lastIndexOf(element: String): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(): ListIterator<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(index: Int): ListIterator<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}