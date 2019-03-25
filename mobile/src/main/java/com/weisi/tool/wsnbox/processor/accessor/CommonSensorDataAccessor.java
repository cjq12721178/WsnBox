package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.wsn.lib.wsb.communicator.Communicator;
import com.wsn.lib.wsb.communicator.receiver.DataReceiver;
import com.wsn.lib.wsb.protocol.ControllableSensorProtocol;
import com.wsn.lib.wsb.protocol.OnFrameAnalyzedListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CJQ on 2017/12/27.
 */

public abstract class CommonSensorDataAccessor<C extends Communicator, P extends ControllableSensorProtocol>
        extends SensorDynamicDataAccessor<P>
        implements DataReceiver.Listener,
        OnFrameAnalyzedListener {

    public static final int ERR_PREPARE_START_DATA_ACCESS_FAILED = 1;
    public static final int ERR_LAUNCH_COMMUNICATOR_FAILED = 2;
    public static final int ERR_START_LISTEN_FAILED = 3;
    public static final int ERR_INIT_DATA_REQUEST_TASK_PARAMETER_FAILED = 4;
    public static final int ERR_IS_CONNECTING = 5;

    private static Timer dataRequestTimer;

    protected static Timer getDataRequestTimer() {
        if (dataRequestTimer == null) {
            dataRequestTimer = new Timer();
        }
        return dataRequestTimer;
    }

    protected final C mCommunicator;
    private final DataReceiver mDataReceiver;
    private DataRequestTask mDataRequestTask;
    private boolean mHasTimeSynchronized;

    public CommonSensorDataAccessor(C communicator, P protocol) {
        super(protocol);
        if (communicator == null) {
            throw new NullPointerException("communicator may not be null");
        }
        mCommunicator = communicator;
        //mCommunicator = onCreateCommunicator();
        mDataReceiver = new DataReceiver(mCommunicator);
    }

    @Override
    protected void onStartDataAccess(@NonNull Context context, @NonNull Settings settings, @NonNull OnStartResultListener listener) {
        if (onPreLaunchCommunicator(context)) {
            if (launchCommunicator(context, settings)) {
                onPostLaunchCommunicator(context, settings, listener);
            } else {
                notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED);
            }
        } else {
            notifyStartFailed(listener, ERR_PREPARE_START_DATA_ACCESS_FAILED);
        }
    }

    protected boolean onPreLaunchCommunicator(Context context) {
        return true;
    }

    protected abstract boolean launchCommunicator(Context context, Settings settings);

    protected void onPostLaunchCommunicator(Context context, Settings settings, OnStartResultListener listener) {
        if (mDataReceiver.startListen(this)) {
            //timeSynchronize(settings);
            if (onInitDataRequestTaskParameter(settings)) {
                startDataRequestTask(getDataRequestCycle(settings));
                notifyStartSuccess(listener);
            } else {
                notifyStartFailed(listener, ERR_INIT_DATA_REQUEST_TASK_PARAMETER_FAILED);
            }
        } else {
            notifyStartFailed(listener, ERR_START_LISTEN_FAILED);
        }
    }

    protected abstract boolean onInitDataRequestTaskParameter(Settings settings);

    public boolean timeSynchronize(@Nullable Settings settings) {
        try {
            onTimeSynchronize(settings);
            return true;
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
        return false;
    }

    protected abstract void onTimeSynchronize(@Nullable Settings settings) throws IOException;

    protected abstract long getDataRequestCycle(Settings settings);

    private void startDataRequestTask(long cycle) {
        stopDataRequestTask();
        if (mDataRequestTask == null) {
            mDataRequestTask = new DataRequestTask();
        }
        getDataRequestTimer().schedule(mDataRequestTask, 0, cycle);
    }

    @Override
    public void onStopDataAccess(Context context) {
        stopDataRequestTask();
        mDataReceiver.stopListen();
        shutdownCommunicator();
        onPostShutdownCommunicator();
    }

    private void stopDataRequestTask() {
        if (mDataRequestTask != null) {
            mDataRequestTask.cancel();
            mDataRequestTask = null;
            mHasTimeSynchronized = false;
        }
    }

    protected abstract void shutdownCommunicator();

    protected void onPostShutdownCommunicator() {
    }

    @Override
    public int onDataReceived(@NonNull byte[] data, int len) {
        //mProtocol.analyze(data, 0, len, this);
        //return len;
        return mProtocol.analyzeMultiplePackages(data, 0, len, this);
    }

    @Override
    public boolean onErrorOccurred(@NonNull Exception e) {
        ExceptionLog.record(e);
        return false;
    }

    @Override
    public void onSensorInfoAnalyzed(int sensorAddress, byte dataTypeValue, int dataTypeIndex, long timestamp, float batteryVoltage, double rawValue) {
        dispatchSensorData(sensorAddress, dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue);
    }

    @Override
    public void onTimeSynchronizationAnalyzed(long timestamp) {
        mHasTimeSynchronized = true;
    }

    public void restartDataRequestTask(long cycle) {
        stopDataRequestTask();
        startDataRequestTask(cycle);
    }

    public static void release() {
        if (dataRequestTimer != null) {
            dataRequestTimer.cancel();
            dataRequestTimer = null;
        }
    }

    protected abstract void sendDataRequestFrame() throws IOException;

    protected byte[] getDataRequestFrame() {
        return mProtocol.makeDataRequestFrame();
    }

    protected byte[] getTimeSynchronizationFrame() {
        return mProtocol.makeTimeSynchronizationFrame();
    }

//    protected void executeOneTimeTimerTask(TimerTask task) {
//        getDataRequestTimer().schedule(task, 0);
//    }

    public void setHasTimeSynchronized(boolean hasTimeSynchronized) {
        mHasTimeSynchronized = hasTimeSynchronized;
    }

    private class DataRequestTask extends TimerTask {

        @Override
        public void run() {
            try {
                if (mHasTimeSynchronized) {
                    sendDataRequestFrame();
                } else {
                    timeSynchronize(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
