package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.communicator.usb.UsbKit;
import com.cjq.lib.weisi.communicator.usb.UsbSerialPort;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.wsn.lib.wsb.communicator.Communicator;
import com.wsn.lib.wsb.protocol.Analyzable;
import com.wsn.lib.wsb.protocol.BleOnUsbSensorProtocol;
import com.wsn.lib.wsb.protocol.EsbOnUsbSensorProtocol;
import com.wsn.lib.wsb.protocol.OnFrameAnalyzedListener;
import com.wsn.lib.wsb.protocol.UsbSensorProtocol;

import org.jetbrains.annotations.NotNull;

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
    protected void onStartDataAccess(@NonNull Context context, @NonNull Settings settings, @NonNull OnStartResultListener listener) {
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
    public int onDataReceived(@NonNull byte[] data, int len) {
        return mProtocol.analyzeMultiplePackages(data, 0, len, this);
    }

    @Override
    public boolean onErrorOccurred(@NonNull Exception e) {

        return false;
    }

    static class CommunicatorDelegate implements Communicator {

        private UsbSerialPort mUsbSerialPort;

        //注意，暂时只支持连接一个USB设备
        void setUsbSerialPort(UsbSerialPort usbSerialPort) {
            mUsbSerialPort = usbSerialPort;
        }

        @Override
        public int read(@NonNull byte[] dst, int offset, int length) throws IOException {
            return mUsbSerialPort != null
                    ? mUsbSerialPort.read(dst, offset, length)
                    : 0;
        }

        @Override
        public boolean canRead() {
            return mUsbSerialPort != null && mUsbSerialPort.canRead();
        }

        @Override
        public void stopRead() {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.stopRead();
            }
        }

        void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.setParameters(baudRate, dataBits, stopBits, parity);
            }
        }

        void close() throws IOException {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.close();
                mUsbSerialPort = null;
            }
        }

        void write(byte[] src, int timeoutMillis) throws IOException {
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

        ProtocolDelegate() {
            super(new EmptyAnalyzer());
        }

        @Override
        public void analyze(@NonNull byte[] udpData, int offset, int length, @NonNull OnFrameAnalyzedListener listener) {
            mProtocol.analyze(udpData, offset, length, listener);
        }

        @Override
        public int analyzeMultiplePackages(@NonNull byte[] udpData, int offset, int length, @NonNull OnFrameAnalyzedListener listener) {
            //Log.d(Tag.LOG_TAG_D_TEST, NumericConverter.bytesToHexDataString(udpData, offset, length));
            return mProtocol.analyzeMultiplePackages(udpData, offset, length, listener);
            //Log.d(Tag.LOG_TAG_D_TEST, "analyzeMultiplePackages: offset: " + offset + ", length: " + length + ", handled: " + result);
            //return result;
        }

        @Override
        public byte getDataRequestCommandCode() {
            return mProtocol.getDataRequestCommandCode();
        }

        @Override
        protected void onDataAnalyzed(@NonNull byte[] data, int realDataZoneStart, int realDataZoneLength, @NonNull OnFrameAnalyzedListener listener) {

        }

        static class EmptyAnalyzer implements Analyzable {
            @Override
            public long analyzeTimestamp(@NotNull byte[] bytes, int i) {
                return 0;
            }
        }
    }
}
