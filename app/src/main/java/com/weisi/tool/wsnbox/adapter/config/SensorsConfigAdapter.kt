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

    //var inDeleteMode = false
    //private val deleteSelections = mutableListOf<Int>()
    private val addresses = AddressList()

//    fun selectDeletingSensor(position: Int) {
//        if (position in 0..(itemCount - 1) && inDeleteMode) {
//            val index = deleteSelections.binarySearch(position)
//            if (index >= 0) {
//                deleteSelections.removeAt(index)
//            } else {
//                deleteSelections.add(-index - 1, position)
//            }
//        }
//    }
//
//    fun changeDeleteModeWithNotification() {
//        inDeleteMode = !inDeleteMode
//        notifyItemRangeChanged(0, itemCount)
//    }
//
//    fun getDeletingSensorsPosition() : List<Int> {
//        return deleteSelections.toList()
//    }
//
//    fun hasDeletingSensors() : Boolean {
//        return !deleteSelections.isEmpty()
//    }
//
//    override fun scheduleItemRemove(position: Int): Int {
//        deleteSelections.remove(position)
//        return super.scheduleItemRemove(position)
//    }

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

    private class ViewHolder(itemView: View?) : BaseConfigAdapter.ViewHolder(itemView) {
        //val chkDelete = itemView!!.findViewById<CheckBox>(R.id.chk_delete_selection)!!
        val tvName = itemView!!.findViewById<TextView>(R.id.tv_sensor_name)!!
        val tvAddress = itemView!!.findViewById<TextView>(R.id.tv_sensor_address)!!
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

//        inner class IteratorImpl : Iterator<Int> {
//
//            init {
//                cursor.moveToFirst()
//                cursor.moveToPrevious()
//            }
//
//            override fun hasNext(): Boolean {
//                return !cursor.isLast
//            }
//
//            override fun next(): Int {
//                cursor.moveToNext()
//                return getSensorAddress(cursor)
//            }
//        }
    }
}