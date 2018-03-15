package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;

import com.cjq.tool.qbox.util.ExceptionLog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by CJQ on 2017/12/5.
 */

public class SettingsImporter extends DefaultHandler {

    private final String FILE_NAME = "configuration.xml";
    private final String COMMUNICATOR_UDP = "UDP";
    private final String COMMUNICATOR_BLE = "BLE";
    private final String COMMUNICATOR_SERIAL_PORT = "SerialPort";
    private final String COMMUNICATOR_USB = "USB";
    private final String COMMUNICATOR_TCP = "TCP";
    private final String DATA_PROCESSOR = "DataProcessor";

    private StringBuilder mBuilder;
    private Settings mSettings;
    private String mSettingType;

    public boolean leadIn(Context context) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            mSettings = new Settings(context);
            parser.parse(context.getAssets().open(FILE_NAME), this);
            return true;
        } catch (Exception e) {
            mSettings = null;
            ExceptionLog.process(e);
        }
        return false;
    }

    public Settings getSettings() {
        return mSettings;
    }

    @Override
    public void startDocument() throws SAXException {
        mBuilder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("communicator")) {
            mSettingType = attributes.getValue("name");
        } else if (localName.equals(DATA_PROCESSOR)) {
            mSettingType = DATA_PROCESSOR;
        }
        mBuilder.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        mBuilder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "enable":
                switch (mSettingType) {
                    case COMMUNICATOR_UDP:
                        mSettings.mDefaultUdpEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                    case COMMUNICATOR_BLE:
                        mSettings.mDefaultBleEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                    case COMMUNICATOR_SERIAL_PORT:
                        mSettings.mDefaultSerialPortEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                    case COMMUNICATOR_USB:
                        mSettings.mDefaultUsbEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                    case DATA_PROCESSOR:
                        mSettings.mDefaultSensorDataGatherEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                    case COMMUNICATOR_TCP:
                        mSettings.mDefaultTcpEnable = Boolean.parseBoolean(mBuilder.toString());
                        break;
                }
                break;
            case "ip":
                switch (mSettingType) {
                    case COMMUNICATOR_UDP:
                        mSettings.setDefaultBaseStationIp(mBuilder.toString());
                        break;
                    case COMMUNICATOR_TCP:
                        mSettings.setDefaultRemoteServerIp(mBuilder.toString());
                        break;
                }
                break;
            case "port":
                switch (mSettingType) {
                    case COMMUNICATOR_UDP:
                        mSettings.setDefaultBaseStationPort(Integer.parseInt(mBuilder.toString()));
                        break;
                    case COMMUNICATOR_TCP:
                        mSettings.setDefaultRemoteServerPort(Integer.parseInt(mBuilder.toString()));
                        break;
                }
                break;
            case "DataRequestCycle":
                switch (mSettingType) {
                    case COMMUNICATOR_UDP:
                        mSettings.setDefaultUdpDataRequestCycle(Long.parseLong(mBuilder.toString()));
                        break;
                    case COMMUNICATOR_SERIAL_PORT:
                        mSettings.setDefaultSerialPortDataRequestCycle(Long.parseLong(mBuilder.toString()));
                        break;
                    case COMMUNICATOR_USB:
                        mSettings.setDefaultUsbDataRequestCycle(Long.parseLong(mBuilder.toString()));
                        break;
                    case COMMUNICATOR_TCP:
                        mSettings.setDefaultTcpDataRequestCycle(Long.parseLong(mBuilder.toString()));
                        break;
                }
                break;
            case "ScanCycle":
                mSettings.setDefaultBleScanCycle(Long.parseLong(mBuilder.toString()));
                break;
            case "ScanDuration":
                mSettings.setDefaultBleScanDuration(Long.parseLong(mBuilder.toString()));
                break;
            case "PortName":
                mSettings.mDefaultSerialPortName = mBuilder.toString();
                break;
            case "BaudRate":
                switch (mSettingType) {
                    case COMMUNICATOR_SERIAL_PORT:
                        mSettings.mDefaultSerialPortBaudRate = Integer.parseInt(mBuilder.toString());
                        break;
                    case COMMUNICATOR_USB:
                        mSettings.mDefaultUsbBaudRate = Integer.parseInt(mBuilder.toString());
                        break;
                }
                break;
            case "GatherCycle":
                mSettings.setDefaultSensorDataGatherCycle(Long.parseLong(mBuilder.toString()));
                break;
            case "vpid":
                mSettings.mDefaultUsbVendorProductId = Long.parseLong(mBuilder.toString(), 16);
                break;
            case "DataBits":
                mSettings.mDefaultUsbDataBits = Integer.parseInt(mBuilder.toString());
                break;
            case "StopBits":
                mSettings.mDefaultUsbStopBits = Integer.parseInt(mBuilder.toString());
                break;
            case "parity":
                mSettings.mDefaultUsbParity = Integer.parseInt(mBuilder.toString());
                break;
            case "protocol":
                mSettings.mDefaultUsbProtocol = mBuilder.toString();
                break;
        }
    }
}
