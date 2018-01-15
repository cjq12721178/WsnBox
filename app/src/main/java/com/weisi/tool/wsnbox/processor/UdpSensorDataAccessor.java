package com.weisi.tool.wsnbox.processor;

import android.content.Context;

import com.cjq.lib.weisi.communicator.UdpKit;
import com.cjq.lib.weisi.protocol.UdpSensorProtocol;
import com.weisi.tool.wsnbox.bean.configuration.Settings;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.TimerTask;

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
            mCommunicator.setSendParameter(settings.getBaseStationIp(),
                    settings.getBaseStationPort(),
                    getDataRequestFrame());
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onTimeSynchronize(final Settings settings) throws IOException {
        executeOneTimeTimerTask(new TimerTask() {
            @Override
            public void run() {
                try {
                    mCommunicator.send(settings.getBaseStationIp(),
                            settings.getBaseStationPort(),
                            getTimeSynchronizationFrame());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public void setDataRequestTaskTargetIp(String ip) throws UnknownHostException {
        mCommunicator.setSendIp(ip);
    }

    public void setDataRequestTaskTargetPort(int port) {
        mCommunicator.setSendPort(port);
    }
}
