package com.weisi.tool.wsnbox.permission;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CJQ on 2018/1/2.
 */

public class BlePermissionsRequester extends PermissionsRequester {

    public BlePermissionsRequester(Activity activity) {
        super(activity, 2, new String[] {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    @Override
    protected int onPreNotifyRequestResultListener() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            SimpleCustomizeToast.show(R.string.ble_not_supported);
            return PREPARE_FAILED;
        }
        if (!bluetoothAdapter.isEnabled()) {
            // 请求打开 Bluetooth
            Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
            requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 设置 Bluetooth 设备可见时间
            requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            // 请求开启 Bluetooth
            getActivity().startActivityForResult(requestBluetoothOn, REQUEST_CODE);
            return PREPARE_PENDING;
        }
        return PREPARE_SUCCESS;
    }

    @Override
    protected int getRequestRationaleRes() {
        return R.string.request_enable_ble;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE) {
            if (EasyPermissions.somePermissionPermanentlyDenied(getActivity(), perms)) {
                new AppSettingsDialog.Builder(getActivity()).build().show();
            } else {
                notifyPermissionsDenied();
                SimpleCustomizeToast.show(R.string.lack_ble_permissions);
            }
        }
    }

    @Override
    public void onActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            notifyPermissionsDenied();
            SimpleCustomizeToast.show(R.string.need_ble_permission);
        } else {
            notifyPermissionsGranted();
        }
    }
}
