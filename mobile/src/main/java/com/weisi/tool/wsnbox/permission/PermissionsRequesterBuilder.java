package com.weisi.tool.wsnbox.permission;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CJQ on 2018/4/4.
 */
public interface PermissionsRequesterBuilder {

    @IntDef({TYPE_NONE, TYPE_BLE, TYPE_UDP, TYPE_SERIAL_PORT, TYPE_USB})
    @Retention(RetentionPolicy.SOURCE)
    @interface PermissionsRequesterType {
    }

    int TYPE_NONE = 0;
    int TYPE_BLE = 1;
    int TYPE_UDP = 2;
    int TYPE_SERIAL_PORT = 3;
    int TYPE_USB = 4;

    PermissionsRequester build(@PermissionsRequesterType int type);
}
