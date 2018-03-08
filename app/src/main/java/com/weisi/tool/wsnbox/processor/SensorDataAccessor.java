package com.weisi.tool.wsnbox.processor;

import android.content.Context;

import com.cjq.lib.weisi.protocol.BaseSensorProtocol;
import com.cjq.lib.weisi.node.Sensor;
import com.cjq.lib.weisi.node.SensorManager;
import com.cjq.tool.qbox.util.ClosableLog;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.util.Tag;

/**
 * Created by CJQ on 2017/12/26.
 */

public abstract class SensorDataAccessor<P extends BaseSensorProtocol> {

    private static long lastNetInTimestamp;
    private static final Object LAST_NET_IN_TIME_LOCKER = new Object();
    private static OnSensorNetInListener onSensorNetInListener;
    private static OnSensorValueUpdateListener onSensorValueUpdateListener;

    private boolean mRunning;
    protected final P mProtocol;

    protected SensorDataAccessor(P protocol) {
        mProtocol = protocol;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void startDataAccess(final Context context,
                                final Settings settings,
                                PermissionsRequester.Builder builder,
                                final OnStartResultListener listener) {
        if (context == null
                || settings == null
                || listener == null) {
            throw new NullPointerException("context or settings or listener may not be null");
        }
        final PermissionsRequester requester;
        if (builder != null) {
            requester = builder.build(getPermissionsRequestType());
        } else {
            requester = null;
        }
        if (requester != null) {
            requester.requestPermissions(new PermissionsRequester.OnRequestResultListener() {
                @Override
                public void onPermissionsGranted() {
                    onStartDataAccess(context, settings, listener);
                }

                @Override
                public void onPermissionsDenied() {
                }
            });
        } else {
            onStartDataAccess(context, settings, listener);
        }
    }

    protected int getPermissionsRequestType() {
        return PermissionsRequester.TYPE_NONE;
    }

    protected abstract void onStartDataAccess(Context context,
                                              Settings settings,
                                              OnStartResultListener listener);

    public void stopDataAccess(Context context) {
        onStopDataAccess(context);
        mRunning = false;
    }

    public abstract void onStopDataAccess(Context context);

    public void restartDataAccess(final Context context,
                                  final Settings settings,
                                  PermissionsRequester.Builder builder,
                                  final OnStartResultListener listener) {
        stopDataAccess(context);
        startDataAccess(context, settings, builder, listener);
    }

    public static void setOnSensorNetInListener(OnSensorNetInListener onSensorNetInListener) {
        SensorDataAccessor.onSensorNetInListener = onSensorNetInListener;
    }

    public static void setOnSensorValueUpdateListener(OnSensorValueUpdateListener onSensorValueUpdateListener) {
        SensorDataAccessor.onSensorValueUpdateListener = onSensorValueUpdateListener;
    }

    protected static void dispatchSensorData(int sensorAddress,
                                             byte dataTypeValue,
                                             int dataTypeIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue) {
        //printCommunicationData(sensorAddress, dataTypeValue, dataTypeIndex);
        Sensor sensor = SensorManager.getSensor(sensorAddress, true);
        int position = sensor.addDynamicValue(dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue);
        if (!recordSensorNetIn(sensor)) {
            if (onSensorValueUpdateListener != null) {
                onSensorValueUpdateListener.onSensorValueUpdate(sensor, position);
            }
        }
    }

    private void printCommunicationData(int sensorAddress, byte dataTypeValue, int dataTypeIndex) {
        ClosableLog.d(Tag.LOG_TAG_D_COMMUNICATION_DATA,
                String.format("sensor address = %06X, data type value = %02X, index = %d",
                        sensorAddress, dataTypeValue, dataTypeIndex));
    }

    private static boolean recordSensorNetIn(Sensor sensor) {
        if (sensor.getNetInTimestamp() == 0) {
            synchronized (LAST_NET_IN_TIME_LOCKER) {
                long currentNetInTimestamp = System.currentTimeMillis();
                if (lastNetInTimestamp >= currentNetInTimestamp) {
                    currentNetInTimestamp = lastNetInTimestamp + 1;
                }
                sensor.setNetInTimestamp(currentNetInTimestamp);
                lastNetInTimestamp = currentNetInTimestamp;
                if (onSensorNetInListener != null) {
                    onSensorNetInListener.onSensorNetIn(sensor);
                }
            }
            return true;
        }
        return false;
    }

    protected void notifyStartSuccess(OnStartResultListener listener) {
        listener.onStartSuccess(this);
        mRunning = true;
    }

    protected void notifyStartFailed(OnStartResultListener listener, int cause) {
        listener.onStartFailed(this, cause);
        mRunning = false;
    }

    public interface OnSensorNetInListener {
        void onSensorNetIn(Sensor sensor);
    }

    public interface OnSensorValueUpdateListener {
        void onSensorValueUpdate(Sensor sensor, int valuePosition);
    }

    public interface OnStartResultListener {
        void onStartSuccess(SensorDataAccessor accessor);
        void onStartFailed(SensorDataAccessor accessor, int cause);
    }
}
