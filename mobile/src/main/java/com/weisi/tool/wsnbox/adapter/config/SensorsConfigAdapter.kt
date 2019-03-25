package com.weisi.tool.wsnbox.adapter.config

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.io.Constant
import java.util.*

class SensorsConfigAdapter : BaseConfigAdapter() {

    private val addresses = AddressList()

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_sensors_config,
                        parent,
                        false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Cursor?, position: Int) {
        super.onBindViewHolder(holder, item, position)
        val viewHolder = holder as ViewHolder
        val address = getSensorAddress(item!!)
        viewHolder.tvAddress.text = ID.getFormatAddress(address)
        viewHolder.tvName.text = getSensorCustomName(item) ?: getSensorDefaultName(address)
    }

    private fun getSensorAddress(item: Cursor) : Int {
        return item.getInt(item.getColumnIndex(Constant.COLUMN_SENSOR_ADDRESS))
    }

    private fun getSensorCustomName(item: Cursor) : String? {
        return item.getString(item.getColumnIndex(Constant.COLUMN_CUSTOM_NAME))
    }

    private fun getSensorDefaultName(address: Int) : String? {
        return SensorManager.findSensorType(address)?.sensorGeneralName
    }

    fun getSensorName(position: Int): String? {
        val cursor = getItemByPosition(position) ?: return null
        return getSensorCustomName(cursor) ?: getSensorDefaultName(getSensorAddress(cursor))
    }

    fun getSensorAddress(position: Int): Int {
        return getSensorAddress(getItemByPosition(position))
    }

    fun getSensorAddresses(): Array<String> {
        return Array(itemCount) {
            ID.getFormatAddress(getSensorAddress(it))
        }
    }

    fun findSensorConfigByAddress(address: Int): Int {
        return Collections.binarySearch(addresses, address)
    }

    private class ViewHolder(itemView: View) : BaseConfigAdapter.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tv_sensor_name)!!
        val tvAddress = itemView.findViewById<TextView>(R.id.tv_sensor_address)!!
    }

    //主要用于二分法查找
    private inner class AddressList : List<Int>, RandomAccess {
        override val size: Int
            get() = itemCount

        override fun contains(element: Int): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun containsAll(elements: Collection<Int>): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun get(index: Int): Int {
            return getSensorAddress(index)
        }

        override fun indexOf(element: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isEmpty(): Boolean {
            return itemCount == 0
        }

        override fun iterator(): Iterator<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun lastIndexOf(element: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(): ListIterator<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(index: Int): ListIterator<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}