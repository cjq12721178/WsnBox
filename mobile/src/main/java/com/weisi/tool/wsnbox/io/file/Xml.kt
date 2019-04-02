package com.weisi.tool.wsnbox.io.file

import com.cjq.lib.weisi.iot.Configuration
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.tool.qbox.util.ExceptionLog
import com.cjq.tool.qbox.util.FileUtil
import com.weisi.tool.wsnbox.bean.configuration.RatchetWheelMeasurementConfiguration
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.bean.corrector.LinearFittingCorrector
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.decorator.CommonMeasurementDecorator
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner
import com.weisi.tool.wsnbox.io.Constant.*
import org.xml.sax.helpers.AttributesImpl
import java.io.FileOutputStream
import javax.xml.transform.OutputKeys
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult

object Xml {

    @JvmStatic
    fun exportParameterConfiguration(type: Int, provider: SensorManager.MeasurementConfigurationProvider, devices: List<Device>, filePath: String): Boolean {
        try {
            val file = FileUtil.openOrCreate(filePath) ?: return false
            // 生成xml
            // 1.创建一个TransformerFactory类的对象
            val factory = SAXTransformerFactory.newInstance() as SAXTransformerFactory
            // 2.通过SAXTransformerFactory对象创建一个TransformerHandler对象
            val handler = factory.newTransformerHandler()
            // 3.通过handler对象创建一个Transformer对象
            val transformer = handler.transformer
            // 4.通过Transformer对象对生成的xml文件进行设置
            // 设置xml的编码
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            // 设置xml的“是否换行”
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            // 5.创建一个Result对象，并且使其与handler关联
            val result = StreamResult(FileOutputStream(file))
            handler.setResult(result)
            // 6.利用handler对象进行xml文件内容的编写
            // 打开document
            val attr = AttributesImpl()
            handler.startDocument()
            insertNewLine(handler)
            handler.startElement("", "", TAG_PROVIDERS, attr)
            insertTab(handler)
            attr.addAttribute("", "", TAG_NAME, "", filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.')))
            attr.addAttribute("", "", TAG_TYPE, "", type.toString())
            handler.startElement("", "", TAG_PROVIDER, attr)
            attr.clear()
            //编辑“sensors”
            val ids = provider.configurationIds
            ids.sort()
            insertTabs(handler, 2)
            handler.startElement("", "", TAG_SENSORS, attr)
            if (ids.isNotEmpty()) {
                var address = 0
                ids.forEach { id ->
                    //Log.d(Tag.LOG_TAG_D_TEST, "export $id")
                    if (id.isSensorInfo) {
                        if (address != 0) {
                            insertTabs(handler, 3)
                            handler.endElement("", "", TAG_SENSOR)
                        }
                        address = id.address
                        //编辑“sensor”
                        insertTabs(handler, 3)
                        attr.addAttribute("", "", TAG_ADDRESS, "", id.formatAddress)
                        handler.startElement("", "", TAG_SENSOR, attr)
                        attr.clear()
                        insertNameElement(handler, provider.getConfiguration<Configuration>(id).decorator?.decorateName(""), attr, 4)
                    } else {
                        //编辑measurement
                        if (address == id.address) {
                            provider.getConfiguration<DisplayMeasurement.Configuration>(id)?.let { configuration ->
                                if (id.dataTypeValueIndex != 0) {
                                    attr.addAttribute("", "", TAG_INDEX, "", id.dataTypeValueIndex.toString())
                                }
                                if (id.isVirtualMeasurement) {
                                    attr.addAttribute("", "", TAG_PATTERN, "", when (configuration) {
                                        is RatchetWheelMeasurementConfiguration -> SensorConfiguration.Measure.CT_RATCHET_WHEEL
                                        else -> SensorConfiguration.Measure.CT_NORMAL
                                    }.toString())
                                } else {
                                    attr.addAttribute("", "", TAG_TYPE, "", id.formattedDataTypeValue)
                                }
                                insertTabs(handler, 4)
                                handler.startElement("", "", TAG_MEASUREMENT, attr)
                                attr.clear()
                                //insertNameElement(handler, provider.getConfiguration<DisplayMeasurement.Configuration>(id).decorator?.decorateName(""), attr, 5)
                                val decorator = configuration.decorator
                                if (decorator is CommonMeasurementDecorator) {
                                    //编辑自定义名称
                                    insertNameElement(handler, decorator.customName, attr, 5)
                                    //编辑自定义单位
                                    insertElement(handler, TAG_UNIT, decorator.customUnit, attr, 5)
                                    //编辑保留小数位数
                                    insertElement(handler, COLUMN_DECIMALS, decorator.getRefinedDecimals().toString(), attr, 5)
                                }

                                //编辑corrector
                                val corrector = configuration.corrector
                                when (corrector) {
                                    is LinearFittingCorrector -> {
                                        attr.addAttribute("", "", TAG_TYPE, "", SensorConfiguration.Measure.CTT_LINEAR_FITTING.toString())
                                        insertTabs(handler, 5)
                                        handler.startElement("", "", COLUMN_CORRECTOR, attr)
                                        attr.clear()
                                        repeat(corrector.groupCount()) { i ->
                                            insertTabs(handler, 6)
                                            handler.startElement("", "", TAG_ITEM, attr)
                                            insertElement(handler, TAG_CORRECTED_VALUE, corrector.getCorrectedValue(i), attr, 7)
                                            insertElement(handler, TAG_SAMPLING_VALUE, corrector.getSamplingValue(i), attr, 7)
                                            insertTabs(handler, 6)
                                            handler.endElement("", "", TAG_ITEM)
                                        }
                                        insertTabs(handler, 5)
                                        handler.endElement("", "", COLUMN_CORRECTOR)
                                    }
                                }

                                //编辑warner
                                val warner = configuration.warner
                                when (warner) {
                                    is CommonSingleRangeWarner -> {
                                        attr.addAttribute("", "", TAG_TYPE, "", TAG_SINGLE_RANGE_WARNER)
                                        insertTabs(handler, 5)
                                        handler.startElement("", "", TAG_WARNER, attr)
                                        attr.clear()
                                        insertElement(handler, TAG_LOW_LIMIT, warner.lowLimit, attr, 6)
                                        insertElement(handler, TAG_HIGH_LIMIT, warner.highLimit, attr, 6)
                                        insertTabs(handler, 5)
                                        handler.endElement("", "", TAG_WARNER)
                                    }
                                    is CommonSwitchWarner -> {
                                        attr.addAttribute("", "", TAG_TYPE, "", TAG_SWITCH_WARNER)
                                        insertTabs(handler, 5)
                                        handler.startElement("", "", TAG_WARNER, attr)
                                        attr.clear()
                                        insertElement(handler, TAG_ABNORMAL, warner.abnormalValue, attr, 6)
                                        insertTabs(handler, 5)
                                        handler.endElement("", "", TAG_WARNER)
                                    }
                                }

                                //编辑额外配置
                                if (configuration is RatchetWheelMeasurementConfiguration) {
                                    insertElement(handler, TAG_INIT_VALUE, configuration.initialValue, attr, 5)
                                    insertElement(handler, TAG_INIT_INSTANCE, configuration.initialDistance, attr, 5)
                                }
                                insertTabs(handler, 4)
                                handler.endElement("", "", TAG_MEASUREMENT)
                            }
                        }
                    }
                }
                insertTabs(handler, 3)
                handler.endElement("", "", TAG_SENSOR)
            }
            insertTabs(handler, 2)
            handler.endElement("", "", TAG_SENSORS)
            //编辑devices
            insertTabs(handler, 2)
            handler.startElement("", "", TAG_DEVICES, attr)
            devices.forEach { device ->
                if (device.name.isNotEmpty()) {
                    attr.addAttribute("", "", TAG_NAME, "", device.name)
                }
                insertTabs(handler, 3)
                handler.startElement("", "", TAG_DEVICE, attr)
                attr.clear()
                device.nodes.forEach { node ->
                    if (node.name?.isNotEmpty() == true) {
                        attr.addAttribute("", "", TAG_NAME, "", node.name)
                    }
                    val id = node.measurement.id
                    attr.addAttribute("", "", TAG_ADDRESS, "", id.formatAddress)
                    if (id.dataTypeAbsValue != 0) {
                        attr.addAttribute("", "", TAG_TYPE, "", id.formattedDataTypeValue)
                    }
                    if (id.dataTypeValueIndex != 0) {
                        attr.addAttribute("", "", TAG_INDEX, "", id.dataTypeValueIndex.toString())
                    }
                    insertTabs(handler, 4)
                    handler.startElement("", "", TAG_NODE, attr)
                    attr.clear()
                    handler.endElement("", "", TAG_NODE)
                }
                insertTabs(handler, 3)
                handler.endElement("", "", TAG_DEVICE)
            }
            insertTabs(handler, 2)
            handler.endElement("", "", TAG_DEVICES)
            insertTabs(handler, 1)
            handler.endElement("", "", TAG_PROVIDER)
            handler.endElement("", "", TAG_PROVIDERS)
            handler.endDocument()
            return true
        } catch (e: Exception) {
            ExceptionLog.record(e)
        }
        return false
    }

    @JvmStatic
    private fun insertNameElement(handler: TransformerHandler, name: String?, attr: AttributesImpl, tabCount: Int) {
        insertElement(handler, TAG_NAME, name, attr, tabCount)
//        if (!name.isNullOrEmpty()) {
//            insertTabs(handler, tabCount)
//            handler.startElement("", "", TAG_NAME, attr)
//            handler.characters(name.toCharArray(), 0, name.length)
//            handler.endElement("", "", TAG_NAME)
//        }
    }

    @JvmStatic
    private fun insertElement(handler: TransformerHandler, tag: String, value: String?, attr: AttributesImpl, tabCount: Int) {
        if (!value.isNullOrEmpty()) {
            insertTabs(handler, tabCount)
            handler.startElement("", "", tag, attr)
            handler.characters(value.toCharArray(), 0, value.length)
            handler.endElement("", "", tag)
        }
    }

    @JvmStatic
    private fun <T> insertElement(handler: TransformerHandler, tag: String, value: T, attr: AttributesImpl, tabCount: Int) {
        Xml.insertElement(handler, tag, value.toString(), attr, tabCount)
    }

    @JvmStatic
    private fun insertNewLine(handler: TransformerHandler) {
        insertChars(handler, "\n")
    }

    @JvmStatic
    private fun insertTabs(handler: TransformerHandler, count: Int, fromStart: Boolean = true) {
        if (count == 1) {
            insertTab(handler, fromStart)
        } else if (count > 1) {
            val c = "\t"
            val builder = StringBuilder(c.length * count + 4)
            if (fromStart) {
                builder.append("\n")
            }
            repeat(count) {
                builder.append(c)
            }
            insertChars(handler, builder.toString())
        }
    }

    @JvmStatic
    private fun insertTab(handler: TransformerHandler, fromStart: Boolean = true) {
        insertChars(handler, if (fromStart) {
            "\n\t"
        } else {
            "\t"
        })
    }

    @JvmStatic
    private fun insertChars(handler: TransformerHandler, content: String) {
        handler.characters(content.toCharArray(), 0, content.length)
    }
}