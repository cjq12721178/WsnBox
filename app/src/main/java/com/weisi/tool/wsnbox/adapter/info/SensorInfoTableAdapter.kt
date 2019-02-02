package com.weisi.tool.wsnbox.adapter.info

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.Measurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.Warner
import com.cjq.lib.weisi.iot.container.Value
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.evrencoskun.tableview.handler.SelectionHandler
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.tbv_sensor_cell.view.*
import kotlinx.android.synthetic.main.tbv_sensor_column_header.view.*
import kotlinx.android.synthetic.main.tbv_sensor_row_header.view.*
import java.text.SimpleDateFormat
import java.util.*

class SensorInfoTableAdapter(context: Context) : AbstractTableAdapter<Measurement<*, *>, SensorInfoTableAdapter.RowHeader, SensorInfoTableAdapter.Cell>(context) {

    private var columnBaseWidth: Int = 0
    private var selectedRow = SelectionHandler.UNSELECTED_POSITION
    private var selectedColumn = SelectionHandler.UNSELECTED_POSITION
    private val unselectedHeaderBackgroundColor = ContextCompat.getColor(context, R.color.bg_unselected_header)
    private val unselectedCellBackgroundColor = ContextCompat.getColor(context, R.color.bg_unselected_cell)
    private val selectedBackgroundColor = ContextCompat.getColor(context, R.color.bg_selected_unit)
    private val shadowHeaderBackgroundColor = ContextCompat.getColor(context, R.color.bg_shadow_header)
    private val unselectedTextColor = ContextCompat.getColor(context, R.color.tc_unselected_cell)
    private val selectedTextColor = ContextCompat.getColor(context, R.color.tc_selected_cell)
    private val lowLimitCellBackgroundColor = ContextCompat.getColor(context, R.color.warner_low_limit)
    private val highLimitCellBackgroundColor = ContextCompat.getColor(context, R.color.warner_high_limit)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")
    private val date = Date()

    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return ColumnHeaderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tbv_sensor_column_header, parent, false))
    }

    override fun onBindColumnHeaderViewHolder(holder: AbstractViewHolder, columnHeaderItemModel: Any, columnPosition: Int) {
        val measurement = columnHeaderItemModel as Measurement<*, *>
        holder.itemView.tv_column_label.text = measurement.getValueLabel()
        if (selectedRow == SelectionHandler.UNSELECTED_POSITION) {
            if (selectedColumn == columnPosition) {
                holder.itemView.tv_column_label.setBackgroundColor(selectedBackgroundColor)
                holder.itemView.tv_column_label.setTextColor(selectedTextColor)
            } else {
                holder.itemView.tv_column_label.setBackgroundColor(unselectedHeaderBackgroundColor)
                holder.itemView.tv_column_label.setTextColor(unselectedTextColor)
            }
        } else {
            if (selectedColumn == SelectionHandler.UNSELECTED_POSITION || selectedColumn == columnPosition) {
                holder.itemView.tv_column_label.setBackgroundColor(shadowHeaderBackgroundColor)
                holder.itemView.tv_column_label.setTextColor(unselectedTextColor)
            } else {
                holder.itemView.tv_column_label.setBackgroundColor(unselectedHeaderBackgroundColor)
                holder.itemView.tv_column_label.setTextColor(unselectedTextColor)
            }
        }
        //holder.itemView.rl_container.layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
        //holder.itemView.tv_column_label.requestLayout()
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder, rowHeaderItemModel: Any, rowPosition: Int) {
        holder.itemView.tv_row_label.text = rowHeaderItemModel.toString()
        if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
            if (selectedRow == rowPosition) {
                holder.itemView.tv_row_label.setBackgroundColor(selectedBackgroundColor)
                holder.itemView.tv_row_label.setTextColor(selectedTextColor)
            } else {
                holder.itemView.tv_row_label.setBackgroundColor(unselectedHeaderBackgroundColor)
                holder.itemView.tv_row_label.setTextColor(unselectedTextColor)
            }
        } else {
            if (selectedRow == SelectionHandler.UNSELECTED_POSITION || selectedRow == rowPosition) {
                holder.itemView.tv_row_label.setBackgroundColor(shadowHeaderBackgroundColor)
                holder.itemView.tv_row_label.setTextColor(unselectedTextColor)
            } else {
                holder.itemView.tv_row_label.setBackgroundColor(unselectedHeaderBackgroundColor)
                holder.itemView.tv_row_label.setTextColor(unselectedTextColor)
            }
        }
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return RowHeaderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tbv_sensor_row_header, parent, false))
    }

    override fun getCellItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return CellViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tbv_sensor_cell, parent, false))
    }

    override fun onCreateCornerView(): View {
        return LayoutInflater.from(mContext).inflate(R.layout.tbv_sensor_corner, null)
    }

    override fun onBindCellViewHolder(holder: AbstractViewHolder, cellItemModel: Any, columnPosition: Int, rowPosition: Int) {
        val content = cellItemModel.toString()
        holder.itemView.tv_cell_content.text = if (content.isEmpty()) {
            " "
        } else {
            content
        }
        //Log.d(Tag.LOG_TAG_D_TEST, "row: $rowPosition, column: $columnPosition")
        val cell = cellItemModel as Cell
        val cellSelected = (selectedRow == SelectionHandler.UNSELECTED_POSITION && selectedColumn == columnPosition)
                || (selectedColumn == SelectionHandler.UNSELECTED_POSITION && selectedRow == rowPosition)
                || (selectedRow == rowPosition && selectedColumn == columnPosition)
        holder.itemView.tv_cell_content.setBackgroundColor(when {
            cellSelected -> selectedBackgroundColor
            cell.warnerResult == DisplayMeasurement.SingleRangeWarner.RESULT_ABOVE_HIGH_LIMIT -> highLimitCellBackgroundColor
            cell.warnerResult == DisplayMeasurement.SingleRangeWarner.RESULT_BELOW_LOW_LIMIT -> lowLimitCellBackgroundColor
            else -> unselectedCellBackgroundColor
        })
        holder.itemView.tv_cell_content.setTextColor(if (cellSelected) {
            selectedTextColor
        } else {
            unselectedTextColor
        })
    }

    override fun getColumnHeaderItemViewType(position: Int): Int {
        return 0
    }

    override fun getRowHeaderItemViewType(position: Int): Int {
        return 0
    }

    fun selectCell(rowPosition: Int, columnPosition: Int) {
        if (rowPosition < SelectionHandler.UNSELECTED_POSITION
                || rowPosition >= rowHeaderRecyclerViewAdapter.itemCount
                || columnPosition < SelectionHandler.UNSELECTED_POSITION
                || columnPosition >= columnHeaderRecyclerViewAdapter.itemCount) {
            return
        }
        if (rowPosition == selectedRow) {
            if (columnPosition == selectedColumn) {
                //啥也不干
            } else {
                if (columnPosition == SelectionHandler.UNSELECTED_POSITION) {
                    if (rowPosition == SelectionHandler.UNSELECTED_POSITION) {
                        rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                        columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                        cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                        selectedColumn = columnPosition
                    } else {
                        rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                        columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                        cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                        selectedColumn = columnPosition
                    }
                } else {
                    if (rowPosition == SelectionHandler.UNSELECTED_POSITION) {
                        if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                            selectedColumn = columnPosition
                        } else {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                            selectedColumn = columnPosition
                        }
                    } else {
                        if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                            cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            selectedColumn = columnPosition
                        } else {
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                            cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            selectedColumn = columnPosition
                        }
                    }
                }
            }
        } else {
            if (columnPosition == selectedColumn) {
                if (rowPosition == SelectionHandler.UNSELECTED_POSITION) {
                    if (columnPosition == SelectionHandler.UNSELECTED_POSITION) {
                        rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                        columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                        cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                        selectedRow = rowPosition
                    } else {
                        rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                        columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                        cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                        selectedRow = rowPosition
                    }
                } else {
                    if (columnPosition == SelectionHandler.UNSELECTED_POSITION) {
                        if (selectedRow == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                            cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            selectedRow = rowPosition
                        } else {
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            selectedRow = rowPosition
                        }
                    } else {
                        if (selectedRow == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                            selectedRow = rowPosition
                        } else {
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            selectedRow = rowPosition
                        }
                    }
                }
            } else {
                if (rowPosition == SelectionHandler.UNSELECTED_POSITION) {
                    if (columnPosition == SelectionHandler.UNSELECTED_POSITION) {
                        rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                        columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                        cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                    } else {
                        if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                        } else {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                            columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                        }
                    }
                } else {
                    if (columnPosition == SelectionHandler.UNSELECTED_POSITION) {
                        if (selectedRow == SelectionHandler.UNSELECTED_POSITION) {
                            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                            columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                            cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                        } else {
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                            cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                            cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                        }
                    } else {
                        if (selectedRow == SelectionHandler.UNSELECTED_POSITION) {
                            if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                                rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                                columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                                cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            } else {
                                rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, rowHeaderRecyclerViewAdapter.itemCount)
                                columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                                columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                                cellRecyclerViewAdapter.notifyItemRangeChanged(0, cellRecyclerViewAdapter.itemCount)
                            }
                        } else {
                            if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                                rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                                rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                                columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                                cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                                cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            } else {
                                rowHeaderRecyclerViewAdapter.notifyItemChanged(selectedRow)
                                rowHeaderRecyclerViewAdapter.notifyItemChanged(rowPosition)
                                columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                                columnHeaderRecyclerViewAdapter.notifyItemChanged(columnPosition)
                                cellRecyclerViewAdapter.notifyItemChanged(selectedRow)
                                cellRecyclerViewAdapter.notifyItemChanged(rowPosition)
                            }
                        }
                    }
                }
                selectedRow = rowPosition
                selectedColumn = columnPosition
            }
        }
    }

    fun selectRow(rowPosition: Int) {
        selectCell(rowPosition, SelectionHandler.UNSELECTED_POSITION)
    }

    fun selectColumn(columnPosition: Int) {
        selectCell(SelectionHandler.UNSELECTED_POSITION, columnPosition)
    }

    fun findActualColumnPosition(measurement: Measurement<*, *>): Int {
        return mColumnHeaderItems.indexOf(measurement)
    }

//    fun findVisibleColumnPosition(measurement: Measurement<*, *>): Int {
//        for (i in 0 until columnHeaderRecyclerViewAdapter.itemCount) {
//            if (measurement === columnHeaderRecyclerViewAdapter.getItem(i)) {
//                return i
//            }
//        }
//        return -1
//    }

    fun initData(sensor: Sensor, measurements: List<Measurement<*, *>>) {
        val columnHeaderItems = ArrayList<Measurement<*, *>>(measurements.size)
        val rowHeaderItems = mutableListOf<RowHeader>()
        val cellItems = mutableListOf<MutableList<Cell>>()
        repeat(measurements.size) {
            columnHeaderItems.add(measurements[it])
        }

        if (measurements.isNotEmpty()) {
            val mainContainer = sensor.mainMeasurement.getUniteValueContainer()
            //var measurement: Measurement<*, *>
            var mainValue: Value
            var rowCellItems: MutableList<Cell>
            val start: Int
            val step: Int
            val end: Int
            if (mainContainer === sensor.mainMeasurement.getDynamicValueContainer()) {
                start = mainContainer.size() - 1
                step = -1
                end = -1
            } else {
                start = 0
                step = 1
                end = mainContainer.size()
            }
            var i = start
            while (i != end) {
                mainValue = mainContainer.getValue(i)
                rowHeaderItems.add(createRowHeader(mainValue))
                rowCellItems = MutableList(columnHeaderItems.size) {
                    createCell(measurements[it], i, mainValue.timestamp)
//                    measurement = measurements[it]
//                    val measurementValue = measurement.getUniteValueContainer().findValue(i, mainValue.timestamp)
//                    when (measurementValue) {
//                        is DisplayMeasurement.Value -> createMeasurementCell(measurement as DisplayMeasurement<*>, measurementValue)
//                        is Sensor.Info.Value -> createInfoCell(measurementValue)
//                        else -> createEmptyCell()
//                    }
                }
                cellItems.add(rowCellItems)
                i += step
            }

            //设置列宽
            columnBaseWidth = (mContext.resources.displayMetrics.widthPixels - mContext.resources.getDimensionPixelOffset(R.dimen.size_sensor_info_row_header_width)) / Math.min(3, measurements.size)
        }
        setAllItems(columnHeaderItems, rowHeaderItems, cellItems)

        //设置选择项
        selectCell(SelectionHandler.UNSELECTED_POSITION, SelectionHandler.UNSELECTED_POSITION)
    }

    private fun createRowHeader(value: Value): RowHeader {
        date.time = value.timestamp + 500L
        val label = dateFormat.format(date)
        return RowHeader(label)
    }

    private fun createInfoCell(value: Sensor.Info.Value): Cell {
        return Cell(value.formattedBatteryVoltage, Warner.RESULT_NORMAL)
    }

    private fun createMeasurementCell(displayMeasurement: DisplayMeasurement<*>, measurementValue: DisplayMeasurement.Value) =
            Cell(displayMeasurement.formatValue(measurementValue), displayMeasurement.testValue(measurementValue))

    private fun createEmptyCell(): Cell {
        return Cell("", Warner.RESULT_NORMAL)
    }

    private fun createCell(measurement: Measurement<*, *>, physicalValuePosition: Int, timestamp: Long): Cell {
        val measurementValue = measurement.getUniteValueContainer().findValue(physicalValuePosition, timestamp)
        return when (measurementValue) {
            is DisplayMeasurement.Value -> createMeasurementCell(measurement as DisplayMeasurement<*>, measurementValue)
            is Sensor.Info.Value -> createInfoCell(measurementValue)
            else -> createEmptyCell()
        }
    }

    //protected abstract fun createMainMeasurementCell(mainMeasurement: M, v: V): Cell;

    private fun setInfoCell(cell: Cell, value: Sensor.Info.Value): Cell {
        cell.data = value.formattedBatteryVoltage
        return cell
    }

    private fun setMeasurementCell(cell: Cell, displayMeasurement: DisplayMeasurement<*>, measurementValue: DisplayMeasurement.Value): Cell {
        cell.data = displayMeasurement.formatValue(measurementValue)
        cell.warnerResult = displayMeasurement.testValue(measurementValue)
        return cell
    }

    //protected abstract fun setMainMeasurementCell(cell: Cell, mainMeasurement: M, v: V): Cell

    fun showMeasurement(sensor: Sensor, targetMeasurement: Measurement<*, *>, isShow: Boolean) {
        if (isShow) {
            if (findActualColumnPosition(targetMeasurement) >= 0) {
                return
            }
            initData(sensor, MutableList(mColumnHeaderItems.size + 1) {
                if (it == mColumnHeaderItems.size) {
                    targetMeasurement
                } else {
                    mColumnHeaderItems[it]
                }
            })
        } else {
            if (findActualColumnPosition(targetMeasurement) < 0) {
                return
            }
            mColumnHeaderItems.remove(targetMeasurement)
            initData(sensor, mColumnHeaderItems)
        }
    }

    fun updateMainValue(sensor: Sensor, mainMeasurement: Measurement<*, *>, valuePosition: Int) {
        if (columnHeaderRecyclerViewAdapter.itemCount == 0
                || mainMeasurement != sensor.mainMeasurement) {
            return
        }
//        val container = mainMeasurement.uniteValueContainer
//        val physicalPosition = container.getPhysicalPositionByLogicalPosition(valuePosition)
//        val realTime = container === mainMeasurement.dynamicValueContainer
        //val mainMeasurementColumnPosition = mColumnHeaderItems.lastIndexOf(mainMeasurement)
        when (mainMeasurement.getUniteValueContainer().interpretAddResult(valuePosition)) {
            ValueContainer.NEW_VALUE_ADDED -> {
                addMainValue(mainMeasurement, valuePosition, false)
                correctItemLocation()
            }
            ValueContainer.LOOP_VALUE_ADDED -> {
                addMainValue(mainMeasurement, valuePosition, true)
                removeRow(rowHeaderRecyclerViewAdapter.itemCount - 1)
                if (rowHeaderRecyclerViewAdapter.itemCount - 1 == selectedRow) {
                    selectedRow = SelectionHandler.UNSELECTED_POSITION
                    if (selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
                        columnHeaderRecyclerViewAdapter.notifyItemRangeChanged(0, columnHeaderRecyclerViewAdapter.itemCount)
                    } else {
                        columnHeaderRecyclerViewAdapter.notifyItemChanged(selectedColumn)
                        selectedColumn = SelectionHandler.UNSELECTED_POSITION
                    }
                }
            }
            ValueContainer.VALUE_UPDATED -> {
                setMainValue(mainMeasurement, valuePosition)
            }
        }
    }

    private fun addMainValue(mainMeasurement: Measurement<*, *>, logicalValuePosition: Int, loop: Boolean) {
        val container = mainMeasurement.getUniteValueContainer()
        val physicalValuePosition = container.getPhysicalPositionByLogicalPosition(logicalValuePosition)
        val realTime = container === mainMeasurement.getDynamicValueContainer()
        val mainValue = container.getValue(physicalValuePosition)
        val newRowPosition = getActualRowPosition(physicalValuePosition, realTime, !loop)
        if (newRowPosition < 0 || newRowPosition > rowHeaderRecyclerViewAdapter.itemCount) {
            return
        }
        val mainMeasurementColumnPosition = mColumnHeaderItems.lastIndexOf(mainMeasurement)
        val isSensorInfo = mainMeasurement.getId().isSensorInfo
        val rowHeader = createRowHeader(mainValue)
        val cellItems = MutableList(columnHeaderRecyclerViewAdapter.itemCount) {
            if (it == mainMeasurementColumnPosition) {
                //createMainMeasurementCell(mainMeasurement, mainValue)
                if (isSensorInfo) {
                    createInfoCell(mainValue as Sensor.Info.Value)
                } else {
                    createMeasurementCell(mainMeasurement as DisplayMeasurement<*>, mainValue as DisplayMeasurement.Value)
                }
//                if (mainValue is DisplayMeasurement.Value) {
//                    createMeasurementCell(mainMeasurement as DisplayMeasurement<*>, mainValue)
//                } else {
//                    createInfoCell(mainValue as Sensor.Info.Value)
//                }
            }
            else if (isSensorInfo) {
                createEmptyCell()
            } else {
                createCell(mColumnHeaderItems[it], physicalValuePosition, mainValue.timestamp)
//                val value = mColumnHeaderItems[it].getUniteValueContainer().findValue(physicalValuePosition, mainValue.timestamp)
//                if (value is Sensor.Info.Value) {
//                    createInfoCell(value)
//                } else {
//                    createMeasurementCell(mColumnHeaderItems[it] as DisplayMeasurement<*>, value as DisplayMeasurement.Value)
//                }
            }
        }
        addRow(newRowPosition, rowHeader, cellItems)
        //这破控件额也是无语。。
        if (newRowPosition < rowHeaderRecyclerViewAdapter.itemCount - 1) {
            val updatePos = newRowPosition + 1
            rowHeaderRecyclerViewAdapter.notifyItemRangeChanged(updatePos, rowHeaderRecyclerViewAdapter.itemCount - updatePos)
            cellRecyclerViewAdapter.notifyItemRangeChanged(updatePos, cellRecyclerViewAdapter.itemCount - updatePos)
        }
        if (selectedRow != SelectionHandler.UNSELECTED_POSITION && newRowPosition <= selectedRow) {
            ++selectedRow
        }
    }

    fun getActualRowPosition(physicalValuePosition: Int, isRealTime: Boolean, isAdd: Boolean): Int {
        return if (isRealTime) {
            rowHeaderRecyclerViewAdapter.itemCount - physicalValuePosition - if (isAdd) {
                0
            } else {
                1
            }
        } else {
            physicalValuePosition
        }
    }

    private fun correctItemLocation() {
        val rv = tableView.cellRecyclerView
        if (rv.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rv.canScrollVertically(-1)) {
            //rv.scrollToPosition(0)
            tableView.scrollToRowPosition(0)
        }
    }

    private fun setMainValue(mainMeasurement: Measurement<*, *>, logicalValuePosition: Int) {
        val mainMeasurementColumnPosition = mColumnHeaderItems.lastIndexOf(mainMeasurement)
        if (mainMeasurementColumnPosition < 0) {
            return
        }
        val container = mainMeasurement.getUniteValueContainer()
        val physicalValuePosition = container.getPhysicalPositionByLogicalPosition(logicalValuePosition)
        val realTime = container === mainMeasurement.getDynamicValueContainer()
        val rowPosition = getActualRowPosition(physicalValuePosition, realTime, false)
        val cell = getCellRowItems(rowPosition)[columnHeaderRecyclerViewAdapter.itemCount - 1]
        val mainValue = container.getValue(rowPosition)
        changeCellItem(mainMeasurementColumnPosition, rowPosition,
                //setMainMeasurementCell(cell, mainMeasurement, mainValue))
                if (mainValue is Sensor.Info.Value) {
                    setInfoCell(cell, mainValue)
                } else {
                    setMeasurementCell(cell,
                            mainMeasurement as DisplayMeasurement<*>,
                            mainValue as DisplayMeasurement.Value)
                })
    }

    fun updateMeasurementValue(sensor: Sensor, measurement: Measurement<*, *>, valueLogicalPosition: Int) {
        val columnPosition = findActualColumnPosition(measurement)
        if (columnPosition < 0) {
            return
        }
        val container = measurement.getUniteValueContainer()
        val addResult = container.interpretAddResult(valueLogicalPosition)
        if (addResult == ValueContainer.ADD_VALUE_FAILED) {
            return
        }
        val physicalPosition = container.getPhysicalPositionByLogicalPosition(valueLogicalPosition)
        val value = container.getValue(physicalPosition)
        val infoPhysicalPosition = sensor.mainMeasurement.getUniteValueContainer().findValuePosition(physicalPosition, value.timestamp)
        if (infoPhysicalPosition < 0) {
            return
        }
        val rowPosition = getActualRowPosition(infoPhysicalPosition, container === measurement.getDynamicValueContainer(), false)
        if (rowPosition < 0 || rowPosition >= rowHeaderRecyclerViewAdapter.itemCount) {
            return
        }
        val cell = getCellRowItems(rowPosition)[columnPosition]
        changeCellItem(columnPosition, rowPosition, if (value is Sensor.Info.Value) {
            setInfoCell(cell, value)
        } else {
            setMeasurementCell(cell, measurement as DisplayMeasurement<*>, value as DisplayMeasurement.Value)
        })
    }

    class RowHeader(var label: String) {

        override fun toString(): String {
            return label
        }
    }

    class Cell(var data: String, var warnerResult: Int) {

        override fun toString(): String {
            return data
        }
    }

    protected class RowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView)

    protected open inner class ExpandableWidthViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        init {
            itemView.minimumWidth = columnBaseWidth
        }
    }

    protected inner class ColumnHeaderViewHolder(itemView: View) : ExpandableWidthViewHolder(itemView)

    protected inner class CellViewHolder(itemView: View) : ExpandableWidthViewHolder(itemView)
}