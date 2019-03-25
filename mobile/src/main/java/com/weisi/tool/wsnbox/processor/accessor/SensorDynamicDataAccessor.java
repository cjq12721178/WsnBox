package com.weisi.tool.wsnbox.processor.accessor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.wsn.lib.wsb.protocol.BaseSensorProtocol;

/**
 * Created by CJQ on 2017/12/26.
 */

public abstract class SensorDynamicDataAccessor<P extends BaseSensorProtocol> {

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

    public static void setOnSensorDynamicDataAccessListener(OnSensorDynamicDataAccessListener listener) {
        onSensorDynamicDataAccessListener = listener;
    }

    protected static void dispatchSensorData(int sensorAddress,
                                             byte dataTypeValue,
                                             int dataTypeIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue) {
        //Log.d(Tag.LOG_TAG_D_TEST, String.format("address: %04X, type: %02X, value: %.3f", sensorAddress, dataTypeValue, rawValue));
        if (onSensorDynamicDataAccessListener != null) {
            onSensorDynamicDataAccessListener.onSensorDynamicDataAccess(sensorAddress,
                    dataTypeValue, dataTypeIndex, timestamp,
                    batteryVoltage, rawValue);
        }
    }

    protected void notifyStartSuccess(OnStartResultListener listener) {
        listener.onStartSuccess(this);
        mRunning = true;
    }

    protected void notifyStartFailed(OnStartResultListener listener, int cause) {
        listener.onStartFailed(this, cause);
        mRunning = false;
    }

    public interface OnStartResultListener {
        void onStartSuccess(SensorDynamicDataAccessor accessor);
        void onStartFailed(SensorDynamicDataAccessor accessor, int cause);
    }
}
