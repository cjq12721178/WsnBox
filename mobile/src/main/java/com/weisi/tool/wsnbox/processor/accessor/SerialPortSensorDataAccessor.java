package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;

import com.cjq.lib.weisi.communicator.SerialPortKit;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.wsn.lib.wsb.protocol.UdpSensorProtocol;

import java.io.IOException;

/**
 * Created by CJQ on 2017/12/27.
 */

public class SerialPortSensorDataAccessor extends CommonSensorDataAccessor<SerialPortKit, UdpSensorProtocol> {

    private final byte[] DATA_REQUEST_FRAME;

    public SerialPortSensorDataAccessor() {
        this(new UdpSensorProtocol());
    }

    protected SerialPortSensorDataAccessor(UdpSensorProtocol protocol) {
        super(new SerialPortKit(), protocol);
        DATA_REQUEST_FRAME = getDataRequestFrame();
    }

    @Override
    protected boolean launchCommunicator(Context context, Settings settings) {
        return mCommunicator.launch(
                settings.getSerialPortName(),
                settings.getSerialPortBaudRate(),
                0);
    }

    @Override
    protected boolean onInitDataRequestTaskParameter(Settings settings) {
        return true;
    }

    @Override
    protected void onTimeSynchronize(Settings settings) {
    }

    @Override
    protected long getDataRequestCycle(Settings settings) {
        return settings.getSerialPortDataRequestCycle();
    }

    @Override
    protected void shutdownCommunicator() {
        mCommunicator.shutdown();
    }

    @Override
    protected void sendDataRequestFrame() throws IOException {
        mCommunicator.send(DATA_REQUEST_FRAME);
    }

//    public boolean restartDataAccess(Context context, Settings settings, String portName) {
//        stopDataAccess();
//        mTmpSerialPortName = portName;
//        boolean result = startDataAccess(context, settings);
//        mTmpSerialPortName = null;
//        return result;
//    }
//
//    public boolean restartDataAccess(Context context, Settings settings, int baudRate) {
//        stopDataAccess();
//        mTmpSerialPortBaudRate = baudRate;
//        boolean result = startDataAccess(context, settings);
//        mTmpSerialPortBaudRate = 0;
//        return result;
//    }
}
