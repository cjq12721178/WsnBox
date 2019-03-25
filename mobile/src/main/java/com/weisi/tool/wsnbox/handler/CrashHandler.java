package com.weisi.tool.wsnbox.handler;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;

import com.cjq.tool.qbox.util.ExceptionLog;

/**
 * Created by CJQ on 2017/11/1.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context mContext;
    private final Thread.UncaughtExceptionHandler mSystemDefaultHandler;

    public CrashHandler(@NonNull Context context) {
        if (context == null) {
            throw new NullPointerException("context may not be null");
        }
        mContext = context;
        mSystemDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        customHandleException(t, e);
        if (mSystemDefaultHandler != null) {
            mSystemDefaultHandler.uncaughtException(t, e);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    private void customHandleException(Thread t, Throwable e) {
        ExceptionLog.process(ExceptionLog.LOG_TYPE_DEBUG
                | ExceptionLog.LOG_TYPE_RECORD,
                e);
    }
}
