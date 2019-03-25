package com.weisi.tool.wsnbox.activity;

import android.support.annotation.NonNull;
import android.view.View;

import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.service.DataPrepareService;
import com.weisi.tool.wsnbox.service.OnServiceConnectionListener;
import com.weisi.tool.wsnbox.service.ServiceInfoObserver;
import com.weisi.tool.wsnbox.util.SafeAsyncTask;

public interface BaseActivityFunction
        extends OnServiceConnectionListener,
        PermissionsRequesterBuilder,
        SafeAsyncTask.AchieverChecker,
        ServiceInfoObserver {

    DataPrepareService getDataPrepareService();
    @NonNull BaseApplication getBaseApplication();
    default @NonNull Settings getSettings() {
        return getBaseApplication().getSettings();
    }
    void onInitActionBar(@NonNull View customView);
    void notifyRegisteredFragmentsServiceConnectionCreate();
    void notifyRegisteredFragmentsServiceConnectionStart();
}
