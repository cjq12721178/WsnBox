package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.wsn.lib.wsb.protocol.BaseSensorProtocol;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;

/**
 * Created by CJQ on 2017/12/26.
 */

public abstract class SensorDynamicDataAccessor<P extends BaseSensorProtocol> {

    //private static long lastNetInTimestamp;
    //private static final Object LAST_NET_IN_TIME_LOCKER = new Object();
    //private static OnSensorNetInListener onSensorNetInListener;
    //private static OnSensorValueUpdateListener onSensorValueUpdateListener;
    private static OnSensorDynamicDataAccessListener onSensorDynamicDataAccessListener;

    private boolean mRunning;
    protected final P mProtocol;

    protected SensorDynamicDataAccessor(P protocol) {
        mProtocol = protocol;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void startDataAccess(final Context context,
                                final Settings settings,
                                PermissionsRequesterBuilder builder,
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
        return PermissionsRequesterBuilder.TYPE_NONE;
    }

    protected abstract void onStartDataAccess(@NonNull Context context,
                                              @NonNull Settings settings,
                                              @NonNull OnStartResultListener listener);

    public void stopDataAccess(Context context) {
        onStopDataAccess(context);
        mRunning = false;
    }

    public abstract void onStopDataAccess(Context context);

    public void restartDataAccess(final Context context,
                                  final Settings settings,
                                  PermissionsRequesterBuilder builder,
                                  final OnStartResultListener listener) {
        stopDataAccess(context);
        startDataAccess(context, settings, builder, listener);
    }

//    public static void setOnSensorNetInListener(OnSensorNetInListener onSensorNetInListener) {
//        SensorDynamicDataAccessor.onSensorNetInListener = onSensorNetInListener;
//    }
//
//    public static void setOnSensorValueUpdateListener(OnSensorValueUpdateListener onSensorValueUpdateListener) {
//        SensorDynamicDataAccessor.onSensorValueUpdateListener = onSensorValueUpdateListener;
//    }


    public static void setOnSensorDynamicDataAccessListener(OnSensorDynamicDataAccessListener listener) {
        onSensorDynamicDataAccessListener = listener;
    }

    protected static void dispatchSensorData(int sensorAddress,
                                             byte dataTypeValue,
                                             int dataTypeIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue) {
        //printCommunicationData(sensorAddress, dataTypeValue, dataTypeIndex);
//        PhysicalSensor sensor = SensorManager.getPhysicalSensor(sensorAddress, true);
//        int position = sensor.addDynamicValue(dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue);
//        if (position != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//            if (!recordSensorNetIn(sensor)) {
//                if (onSensorValueUpdateListener != null) {
//                    onSensorValueUpdateListener.onSensorValueUpdate(sensor, position);
//                }
//            }
//        }
        if (onSensorDynamicDataAccessListener != null) {
            onSensorDynamicDataAccessListener.onSensorDynamicDataAccess(sensorAddress,
                    dataTypeValue, dataTypeIndex, timestamp,
                    batteryVoltage, rawValue);
        }
    }

    //    private void printCommunicationData(int sensorAddress, byte dataTypeValue, int dataTypeIndex) {
//        ClosableLog.d(Tag.LOG_TAG_D_COMMUNICATION_DATA,
//                String.format("sensor address = %06X, data type value = %02X, index = %d",
//                        sensorAddress, dataTypeValue, dataTypeIndex));
//    }

//    private static boolean recordSensorNetIn(PhysicalSensor sensor) {
//        if (sensor.getNetInTimestamp() == 0) {
//            synchronized (LAST_NET_IN_TIME_LOCKER) {
//                long currentNetInTimestamp = System.currentTimeMillis();
//                if (lastNetInTimestamp >= currentNetInTimestamp) {
//                    currentNetInTimestamp = lastNetInTimestamp + 1;
//                }
//                sensor.setNetInTimestamp(currentNetInTimestamp);
//                lastNetInTimestamp = currentNetInTimestamp;
//                if (onSensorNetInListener != null) {
//                    onSensorNetInListener.onSensorNetIn(sensor);
//                }
//            }
//            return true;
//        }
//        return false;
//    }

    protected void notifyStartSuccess(OnStartResultListener listener) {
        listener.onStartSuccess(this);
        mRunning = true;
    }

    protected void notifyStartFailed(OnStartResultListener listener, int cause) {
        listener.onStartFailed(this, cause);
        mRunning = false;
    }

//    public interface OnSensorNetInListener {
//        void onSensorNetIn(PhysicalSensor sensor);
//    }
//
//    public interface OnSensorValueUpdateListener {
//        void onSensorValueUpdate(PhysicalSensor sensor, int valuePosition);
//    }

    public interface OnStartResultListener {
        void onStartSuccess(SensorDynamicDataAccessor accessor);
        void onStartFailed(SensorDynamicDataAccessor accessor, int cause);
    }
}
