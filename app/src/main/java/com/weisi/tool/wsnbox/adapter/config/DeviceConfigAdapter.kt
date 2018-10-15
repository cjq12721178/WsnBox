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

class DeviceConfigAdapter : BaseConfigAdapter() {

    private val ids = IdList()

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_node_config,
                        parent,
                        false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Cursor?, position: Int) {
        super.onBindViewHolder(holder, item, position)
        val viewHolder = holder as ViewHolder
        //val measurement = getMeasurement(item!!)
        //viewHolder.tvMeasureId.text = measurement?.getId()?.toString()
        viewHolder.tvMeasureId.text = ID.getFormatId(getMeasurementId(item!!))
        viewHolder.tvMeasureName.text = getMeasurement(item)?.getDefaultName()
        viewHolder.tvNodeName.text = getNodeName(item)
    }

    private fun getNodeName(item: Cursor) =
            item.getString(item.getColumnIndex(Constant.COLUMN_NODE_NAME))

    fun getNodeName(position: Int) =
            getNodeName(getItemByPosition(position))

    private fun getMeasurement(cursor: Cursor) =
            SensorManager.getMeasurement(getMeasurementId(cursor))

    private fun getMeasurement(position: Int) =
            getMeasurement(getItemByPosition(position))

    private fun getMeasurementId(cursor: Cursor) =
            cursor.getLong(cursor.getColumnIndex(Constant.COLUMN_MEASUREMENT_VALUE_ID))

    fun getMeasurementId(position: Int) =
            getMeasurementId(getItemByPosition(position))

    fun findNodeConfigById(id: Long) =
            Collections.binarySearch(ids, id)

    private class ViewHolder(itemView: View?) : BaseConfigAdapter.ViewHolder(itemView) {
        val tvMeasureId = itemView!!.findViewById<TextView>(R.id.tv_measurement_id_value)
        val tvMeasureName = itemView!!.findViewById<TextView>(R.id.tv_measurement_name_value)
        val tvNodeName = itemView!!.findViewById<TextView>(R.id.tv_node_name_value)
    }

    private inner class IdList : List<Long>, RandomAccess {
        override val size: Int
            get() = itemCount

        override fun contains(element: Long): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun containsAll(elements: Collection<Long>): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun get(index: Int): Long {
            return getMeasurementId(index)
        }

        override fun indexOf(element: Long): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isEmpty(): Boolean {
            return itemCount == 0
        }

        override fun iterator(): Iterator<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun lastIndexOf(element: Long): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(): ListIterator<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun listIterator(index: Int): ListIterator<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<Long> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}