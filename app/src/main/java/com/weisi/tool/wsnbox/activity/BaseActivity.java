package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.fragment.BaseFragmentFunction;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.ReadPermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;
import com.weisi.tool.wsnbox.version.update.UpdateInfo;
import com.weisi.tool.wsnbox.version.update.Updater;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Created by CJQ on 2017/9/7.
 */

public abstract class BaseActivity
        extends AppCompatActivity
        implements BaseActivityFunction {

    protected static final String DIALOG_TAG_UPDATE_APP = "tag_update_app";
    private static final String ARGUMENT_KEY_UPDATE_INFO = "ak_update_info";
    private static final int RC_READ_PERMISSION = 1;

    private ActivityFunctionDelegate mFunctionDelegate;

    @Override
    public DataPrepareService getDataPrepareService() {
        return getFunctionDelegate().getDataPrepareService();
    }

    @Override
    @NonNull
    public BaseApplication getBaseApplication() {
        return getFunctionDelegate().getBaseApplication();
    }

    @Override
    public void notifyRegisteredFragmentsServiceConnectionCreate() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment :
                fragments) {
            if (fragment instanceof BaseFragmentFunction) {
                ((BaseFragmentFunction) fragment).performServiceConnectionCreate();
            }
        }
//        ArrayList<String> fragmentTags = getIntent().getStringArrayListExtra(BaseFragmentFunction.ARGUMENT_KEY_REGISTER_TAG);
//        if (fragmentTags != null) {
//            for (String tag :
//                    fragmentTags) {
//                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
//                if (fragment instanceof BaseFragmentFunction) {
//                    ((BaseFragmentFunction) fragment).performServiceConnectionCreate();
//                }
//            }
//        }
    }

    @Override
    public void notifyRegisteredFragmentsServiceConnectionStart() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment :
                fragments) {
            if (fragment instanceof BaseFragmentFunction) {
                ((BaseFragmentFunction) fragment).performServiceConnectionStart();
            }
        }
//        ArrayList<String> fragmentTags = getIntent().getStringArrayListExtra(BaseFragmentFunction.ARGUMENT_KEY_REGISTER_TAG);
//        if (fragmentTags != null) {
//            for (String tag :
//                    fragmentTags) {
//                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
//                if (fragment instanceof BaseFragmentFunction) {
//                    ((BaseFragmentFunction) fragment).performServiceConnectionStart();
//                }
//            }
//        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFunctionDelegate().onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getFunctionDelegate().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        getFunctionDelegate().onMenuOpened(featureId, menu);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFunctionDelegate().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFunctionDelegate().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getFunctionDelegate().onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        getFunctionDelegate().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getFunctionDelegate().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onServiceConnectionCreate(@NonNull DataPrepareService service) {
        notifyRegisteredFragmentsServiceConnectionCreate();
    }

    @Override
    public void onServiceConnectionStart(@NonNull DataPrepareService service) {
        notifyRegisteredFragmentsServiceConnectionStart();
    }

    @Override
    public void onServiceConnectionStop(@NonNull DataPrepareService service) {

    }

    @Override
    public void onServiceConnectionDestroy(@NonNull DataPrepareService service) {

    }

    @Override
    public void onInitActionBar(@NonNull View customView) {
    }

    @Override
    public PermissionsRequester build(int type) {
        return getFunctionDelegate().build(type);
    }

    private ActivityFunctionDelegate getFunctionDelegate() {
        if (mFunctionDelegate == null) {
            mFunctionDelegate = new ActivityFunctionDelegate(this);
        }
        return mFunctionDelegate;
    }

    protected void checkVersionAndDecideIfUpdate(boolean activeQuery) {
        Updater.checkLatestVersion(this, new Updater.CheckVersionCallBack() {
            @Override
            public void onVersionChecked(@NotNull UpdateInfo updateInfo) {
                if (Updater.hasNewVersion(getApplicationContext(), updateInfo)) {
                    getSettings().setLatestVersionName(updateInfo.getVersionName());
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setTitle(getString(R.string.check_for_new_version, updateInfo.getVersionName()));
                    dialog.setContent(updateInfo.getVersionDescription());
                    Objects.requireNonNull(dialog.getArguments()).putParcelable(ARGUMENT_KEY_UPDATE_INFO, updateInfo);
                    ConfirmDialog.Decorator decorator = dialog.getCustomDecorator();
                    decorator.setDrawSeparationLine(true);
                    if (updateInfo.getForceUpdate()) {
                        dialog.setDrawCancelButton(false);
                        dialog.setCancelable(false);
                        decorator.setOkLabel(R.string.immediate_update);
                    } else {
                        decorator.setOkLabel(R.string.immediate_update);
                        decorator.setCancelLabel(R.string.wait_a_minute);
                    }
                    dialog.show(getSupportFragmentManager(), DIALOG_TAG_UPDATE_APP);
                } else if (activeQuery) {
                    informCurrentVersionLatest();
                }
            }

            @Override
            public void onErrorOccurred(@NotNull Throwable t) {
                if (activeQuery) {
                    informCurrentVersionLatest();
                }
            }
        });
    }

    private void informCurrentVersionLatest() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setDrawCancelButton(false);
        dialog.setTitle(R.string.current_version_latest);
        dialog.show(getSupportFragmentManager(), "tag_cur_vsn_latest");
    }

    protected void updateVersion(BaseDialog dialog) {
        UpdateInfo info = Objects.requireNonNull(dialog.getArguments()).getParcelable(ARGUMENT_KEY_UPDATE_INFO);
        new ReadPermissionsRequester(this, RC_READ_PERMISSION)
                .requestPermissions(new PermissionsRequester.OnRequestResultListener() {
                    @Override
                    public void onPermissionsGranted() {
                        Updater.update(getApplicationContext(), Objects.requireNonNull(info));
                    }

                    @Override
                    public void onPermissionsDenied() {
                        SimpleCustomizeToast.show(R.string.lack_read_permissions);
                    }
                });
    }

    public void showExpectDialog() {
        getFunctionDelegate().showExpectDialog(getSupportFragmentManager());
    }

    protected View getGroupLabelView(@NonNull LayoutInflater inflater, @NonNull RecyclerView rv, @StringRes int labelResId) {
        View view = inflater.inflate(R.layout.li_group_label, rv, false);
        if (view instanceof TextView) {
            ((TextView) view).setText(labelResId);
        }
        return view;
    }

    @Override
    public boolean invalid() {
        return getFunctionDelegate().isActivityInvalid();
    }
}
