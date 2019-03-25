package com.weisi.tool.wsnbox.processor.accessor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.communicator.BleKit;
import com.wsn.lib.wsb.protocol.BleSensorProtocol;
import com.wsn.lib.wsb.protocol.OnSensorInfoAnalyzeListener;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by CJQ on 2017/12/26.
 */

public class BleSensorDataAccessor
        extends SensorDynamicDataAccessor<BleSensorProtocol>
        implements BluetoothAdapter.LeScanCallback,
        OnSensorInfoAnalyzeListener {

    public static final int ERR_BLE_LAUNCH_FAILED = 1;
    public static final int ERR_BLE_START_SCAN_FAILED = 2;

    private final BleKit mBleKit = new BleKit();
    //private final BleSensorProtocol mBleSensorProtocol = new BleSensorProtocol();

    public BleSensorDataAccessor() {
        super(new BleSensorProtocol());
    }

    @Override
    protected int getPermissionsRequestType() {
        return PermissionsRequesterBuilder.TYPE_BLE;
    }

    @Override
    protected void onStartDataAccess(@NonNull Context context, @NonNull Settings settings, @NonNull OnStartResultListener listener) {
        if (mBleKit.launch(context)) {
            if (startBleScan(settings.getBleScanCycle(), settings.getBleScanDuration())) {
                //listener.onStartSuccess(this);
                notifyStartSuccess(listener);
            } else {
                //listener.onStartFailed(this, ERR_BLE_START_SCAN_FAILED);
                notifyStartFailed(listener, ERR_BLE_START_SCAN_FAILED);
            }
        } else {
            //listener.onStartFailed(this, ERR_BLE_LAUNCH_FAILED);
            notifyStartFailed(listener, ERR_BLE_LAUNCH_FAILED);
        }
    }

    @Override
    public void onStopDataAccess(Context context) {
        mBleKit.stopScan();
    }

//    @Override
//    public boolean startDataAccess(Context context, Settings settings) {
//        if (!mBleKit.launch(context)) {
//            return false;
//        }
//        return startBleScan(settings.getBleScanCycle(), settings.getBleScanDuration());
//    }

    private boolean startBleScan(long cycle, long duration) {
        return mBleKit.startScan(this,
                TimeUnit.SECONDS.toMillis(cycle),
                TimeUnit.SECONDS.toMillis(duration));
    }

    public boolean restartBleScan(long cycle, long duration) {
        if (!mBleKit.isLaunch()) {
            return false;
        }
        mBleKit.stopScan();
        return startBleScan(cycle, duration);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mProtocol.analyze(device.getAddress(), scanRecord, this);
    }

    @Override
    public void onSensorInfoAnalyzed(int sensorAddress, byte dataTypeValue, int dataTypeIndex, long timestamp, float batteryVoltage, double rawValue) {
        dispatchSensorData(sensorAddress, dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue);
    }
}
