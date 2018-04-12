package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.cjq.lib.weisi.communicator.Communicator;
import com.cjq.lib.weisi.communicator.usb.UsbKit;
import com.cjq.lib.weisi.communicator.usb.UsbSerialPort;
import com.cjq.lib.weisi.protocol.BleOnUsbSensorProtocol;
import com.cjq.lib.weisi.protocol.EsbOnUsbSensorProtocol;
import com.cjq.lib.weisi.protocol.OnFrameAnalyzedListener;
import com.cjq.lib.weisi.protocol.UsbSensorProtocol;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.Settings;

import java.io.IOException;

/**
 * Created by CJQ on 2018/1/5.
 */

public class UsbSensorDataAccessor
        extends CommonSensorDataAccessor<UsbSensorDataAccessor.CommunicatorDelegate, UsbSensorDataAccessor.ProtocolDelegate>
        implements UsbKit.OnUsbSerialPortStateChangeListener {

    private final int TIMEOUT_MILLIS = 5000;
    private byte[] mDataRequestFrame;
    private Context mTmpContext;
    private Settings mTmpSettings;
    private OnStartResultListener mTmpListener;

    public UsbSensorDataAccessor() {
        super(new CommunicatorDelegate(), new ProtocolDelegate());
    }

    @Override
    protected void onStartDataAccess(Context context, Settings settings, OnStartResultListener listener) {
        mTmpContext = context;
        mTmpSettings = settings;
        mTmpListener = listener;
        setProtocol(settings.getUsbProtocol());
        onPreLaunchCommunicator(context);
        if (!launchCommunicator(context, settings)) {
            mTmpContext = null;
            mTmpSettings = null;
            mTmpListener = null;
            notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED);
        }
    }

    public void setProtocol(String protocolName) {
        mProtocol.setProtocol(protocolName);
        mDataRequestFrame = getDataRequestFrame();
    }

    @Override
    protected boolean onPreLaunchCommunicator(Context context) {
        UsbKit.register(context, this);
        return true;
    }

    @Override
    protected boolean launchCommunicator(Context context, Settings settings) {
        return UsbKit.launch(context, settings.getUsbVendorId(), settings.getUsbProductId());
    }

    @Override
    protected boolean onInitDataRequestTaskParameter(Settings settings) {
        return setCommunicationParameter(settings.getUsbBaudRate(),
                settings.getUsbDataBits(),
                settings.getUsbStopBits(),
                settings.getUsbParity());
    }

    @Override
    protected void onTimeSynchronize(Settings settings) throws IOException {
        mCommunicator.write(getTimeSynchronizationFrame(), TIMEOUT_MILLIS);
    }

    public boolean setCommunicationParameter(int baudRate, int dataBits, int stopBits, int parity) {
        try {
            mCommunicator.setParameters(baudRate, dataBits, stopBits, parity);
            return true;
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
        return false;
    }

    @Override
    protected long getDataRequestCycle(Settings settings) {
        return settings.getUsbUsbDataRequestCycle();
    }

    @Override
    protected void shutdownCommunicator() {
        try {
            mCommunicator.close();
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
    }

    @Override
    protected void sendDataRequestFrame() throws IOException {
        mCommunicator.write(mDataRequestFrame, TIMEOUT_MILLIS);
    }

    @Override
    public void stopDataAccess(Context context) {
        stopDataAccessImpl();
        UsbKit.unregister(context);
    }

    private void stopDataAccessImpl() {
        super.stopDataAccess(null);
    }

    @Override
    public void onUsbSerialPortOpen(UsbSerialPort usbSerialPort) {
        mCommunicator.setUsbSerialPort(usbSerialPort);
        onPostLaunchCommunicator(mTmpContext, mTmpSettings, mTmpListener);
    }

    @Override
    public void onUsbSerialPortClose(UsbDevice usbDevice) {
        stopDataAccessImpl();
    }

    @Override
    public int onDataReceived(byte[] data, int len) {
        return mProtocol.analyzeMultiplePackages(data, 0, len, this);
    }

    static class CommunicatorDelegate implements Communicator {

        private UsbSerialPort mUsbSerialPort;

        //注意，暂时只支持连接一个USB设备
        public void setUsbSerialPort(UsbSerialPort usbSerialPort) {
            mUsbSerialPort = usbSerialPort;
        }

        @Override
        public int read(byte[] dst, int offset, int length) throws IOException {
            return mUsbSerialPort != null
                    ? mUsbSerialPort.read(dst, offset, length)
                    : 0;
        }

        @Override
        public boolean canRead() {
            return mUsbSerialPort != null && mUsbSerialPort.canRead();
        }

        @Override
        public void stopRead() throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.stopRead();
            }
        }

        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.setParameters(baudRate, dataBits, stopBits, parity);
            }
        }

        public void close() throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.close();
                mUsbSerialPort = null;
            }
        }

        public void write(byte[] src, int timeoutMillis) throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.write(src, timeoutMillis);
            }
        }
    }

    static class ProtocolDelegate extends UsbSensorProtocol {

        private UsbSensorProtocol mProtocol;

        public void setProtocol(String protocolName) {
            switch (protocolName) {
                case "BLE":
                    mProtocol = new BleOnUsbSensorProtocol();
                    break;
                case "ESB":
                default:
                    mProtocol = new EsbOnUsbSensorProtocol();
                    break;
            }
        }

        protected ProtocolDelegate() {
            super(null);
        }

        @Override
        public void analyze(byte[] udpData, int offset, int length, OnFrameAnalyzedListener listener) {
            mProtocol.analyze(udpData, offset, length, listener);
        }

        @Override
        public int analyzeMultiplePackages(byte[] udpData, int offset, int length, OnFrameAnalyzedListener listener) {
            return mProtocol.analyzeMultiplePackages(udpData, offset, length, listener);
        }

        @Override
        public byte getDataRequestCommandCode() {
            return mProtocol.getDataRequestCommandCode();
        }

        @Override
        protected void onDataAnalyzed(byte[] data, int realDataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {

        }
    }
}
