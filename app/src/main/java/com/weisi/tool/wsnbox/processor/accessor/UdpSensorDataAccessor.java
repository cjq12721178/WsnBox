package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.wsn.lib.wsb.communicator.UdpKit;
import com.wsn.lib.wsb.protocol.UdpSensorProtocol;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.Settings;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by CJQ on 2017/12/27.
 */

public class UdpSensorDataAccessor extends CommonSensorDataAccessor<UdpKit, UdpSensorProtocol> {

    public UdpSensorDataAccessor() {
        super(new UdpKit(), new UdpSensorProtocol());
    }

    @Override
    protected boolean launchCommunicator(Context context, Settings settings) {
        return mCommunicator.launch();
    }

    @Override
    protected boolean onInitDataRequestTaskParameter(Settings settings) {
        try {
            mCommunicator.setSendIp(settings.getBaseStationIp());
            mCommunicator.setSendPort(settings.getBaseStationPort());
//            mCommunicator.setSendParameter(settings.getBaseStationIp(),
//                    settings.getBaseStationPort(),
//                    getDataRequestFrame());
            return true;
        } catch (UnknownHostException e) {
            ExceptionLog.record(e);
        }
        return false;
    }

    @Override
    protected void onTimeSynchronize(final Settings settings) throws IOException {
        mCommunicator.setSendData(getTimeSynchronizationFrame());
        mCommunicator.send();
//        executeOneTimeTimerTask(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    mCommunicator.send(settings.getBaseStationIp(),
//                            settings.getBaseStationPort(),
//                            getTimeSynchronizationFrame());
//                    onInitDataRequestTaskParameter(settings);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    @Override
    public void onTimeSynchronizationAnalyzed(long timestamp) {
        super.onTimeSynchronizationAnalyzed(timestamp);
        mCommunicator.setSendData(getDataRequestFrame());
    }

    @Override
    protected long getDataRequestCycle(Settings settings) {
        return settings.getUdpDataRequestCycle();
    }

    @Override
    protected void shutdownCommunicator() {
        mCommunicator.close();
    }

    @Override
    protected void sendDataRequestFrame() throws IOException {
        mCommunicator.send();
    }

    @Override
    public boolean onErrorOccurred(@NonNull Exception e) {
        if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
            return true;
        }
        return super.onErrorOccurred(e);
    }

    public void setDataRequestTaskTargetIp(String ip) throws UnknownHostException {
        mCommunicator.setSendIp(ip);
    }

    public void setDataRequestTaskTargetPort(int port) {
        mCommunicator.setSendPort(port);
    }
}
