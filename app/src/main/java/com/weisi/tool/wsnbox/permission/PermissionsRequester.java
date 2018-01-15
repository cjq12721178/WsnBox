package com.weisi.tool.wsnbox.permission;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CJQ on 2018/1/2.
 */

public abstract class PermissionsRequester implements EasyPermissions.PermissionCallbacks {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_BLE = 1;
    public static final int TYPE_UDP = 2;
    public static final int TYPE_SERIAL_PORT = 3;
    public static final int TYPE_USB = 4;

    protected static final int PREPARE_SUCCESS = 0;
    protected static final int PREPARE_FAILED = 1;
    protected static final int PREPARE_PENDING = 2;

    private final Activity mActivity;
    public final int REQUEST_CODE;
    private final String[] PERMISSIONS;
    private OnRequestResultListener mOnRequestResultListener;

    public PermissionsRequester(@NonNull Activity activity,
                                int requestCode,
                                String[] permissions) {
        mActivity = activity;
        REQUEST_CODE = requestCode;
        PERMISSIONS = permissions;
    }

    protected Activity getActivity() {
        return mActivity;
    }

    public void requestPermissions(OnRequestResultListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener may not be null");
        }
        mOnRequestResultListener = listener;
        Manager.register(this);
        if (hasPermissions()) {
            switch (onPreNotifyRequestResultListener()) {
                case PREPARE_SUCCESS:
                    notifyPermissionsGranted();
                    break;
                case PREPARE_PENDING:
                    break;
                case PREPARE_FAILED:
                default:
                    notifyPermissionsDenied();
                    break;
            }
        } else {
            onRequestPermissions();
        }
    }

    //返回值表示是否准备好可以通知进行实际操作
    protected int onPreNotifyRequestResultListener() {
        return PREPARE_SUCCESS;
    }

    protected boolean hasPermissions() {
        return EasyPermissions.hasPermissions(mActivity, PERMISSIONS);
    }

    protected void onRequestPermissions() {
        EasyPermissions.requestPermissions(mActivity,
                mActivity.getString(getRequestRationaleRes()),
                REQUEST_CODE,
                PERMISSIONS);
    }

    protected abstract @StringRes int getRequestRationaleRes();

    protected void notifyPermissionsGranted() {
        Manager.unregister(this);
        mOnRequestResultListener.onPermissionsGranted();
    }

    protected void notifyPermissionsDenied() {
        Manager.unregister(this);
        mOnRequestResultListener.onPermissionsDenied();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (isAllPermissionsGranted(perms)) {
            notifyPermissionsGranted();
        } else {
            notifyPermissionsDenied();
        }
    }

    private boolean isAllPermissionsGranted(@NonNull List<String> grantedPermissions) {
        if (grantedPermissions.size() == PERMISSIONS.length) {
            for (int i = 0;i < PERMISSIONS.length;++i) {
                if (!grantedPermissions.get(i).equals(PERMISSIONS[i])) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public void onActivityResult(int resultCode, Intent data) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    public interface OnRequestResultListener {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    public interface Builder {
        PermissionsRequester build(int type);
    }

    public static class Manager {

        private static List<PermissionsRequester> permissionsRequesters = new ArrayList<>();

        public static void register(PermissionsRequester requester) {
            permissionsRequesters.add(requester);
        }

        public static PermissionsRequester find(int requestCode) {
            for (PermissionsRequester requester
                    : permissionsRequesters) {
                if (requester.REQUEST_CODE == requestCode) {
                    return requester;
                }
            }
            return null;
        }

        public static void unregister(PermissionsRequester requester) {
            permissionsRequesters.remove(requester);
        }
    }
}
