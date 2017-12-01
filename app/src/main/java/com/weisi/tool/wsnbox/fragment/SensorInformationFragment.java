package com.weisi.tool.wsnbox.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjq.lib.weisi.sensor.ConfigurationManager;
import com.cjq.lib.weisi.sensor.Measurement;
import com.cjq.lib.weisi.sensor.Sensor;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.adapter.SensorInfoAdapter;

import java.util.List;


/**
 * Created by CJQ on 2017/11/3.
 */

public class SensorInformationFragment
        extends DialogFragment
        implements SensorInfoAdapter.OnDisplayStateChangeListener,
        View.OnTouchListener {

    private static final String ARGUMENT_KEY_START_MEASUREMENT_INDEX = "smi";
    public static final String TAG = "sensor_info";
    private boolean mRealTime;
    private Sensor mSensor;
    private SensorInfoAdapter mSensorInfoAdapter;
    private ImageView mIvInfoOrientation;
    private TextView[] mTvValueLabels = new TextView[SensorInfoAdapter.MAX_DISPLAY_COUNT];
    private float mLastTouchX;
    private float mLastTouchY;

    public void setSensor(Sensor sensor) {
        mSensor = sensor;
    }

    public Sensor getSensor() {
        return mSensor;
    }

    public void setRealTime(boolean realTime) {
        mRealTime = realTime;
    }

//    private RecyclerView.OnFlingListener mSensorInfoOnFlingListener = new RecyclerView.OnFlingListener() {
//        @Override
//        public boolean onFling(int velocityX, int velocityY) {
//            if (Math.abs(velocityX) > Math.abs(velocityY)) {
//                if (velocityX == 0) {
//                    return false;
//                } else if (velocityX > 0) {
//                    mSensorInfoAdapter.showNextItem(false);
//                } else {
//                    mSensorInfoAdapter.showNextItem(true);
//                }
//                return true;
//            }
//            return false;
//        }
//    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mSensorInfoAdapter = new SensorInfoAdapter(getContext(), mSensor, mRealTime, 0);
        } else {
            mSensorInfoAdapter = new SensorInfoAdapter(getContext(), mSensor, mRealTime, savedInstanceState.getInt(ARGUMENT_KEY_START_MEASUREMENT_INDEX));
        }
        View view = inflater.inflate(R.layout.fragment_sensor_info, container, false);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_sensor_info_label);
        tvTitle.setText(mSensor.getName() + "详细信息");
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_sensor_address);
        tvAddress.setText("地址：" + mSensor.getFormatAddress());
        TextView tvState = (TextView) view.findViewById(R.id.tv_sensor_state);
        tvState.setText("状态：" +
                (mSensor.getState() == Sensor.State.ON_LINE
                        ? "在线"
                        : "离线"));
        TextView tvSource = (TextView) view.findViewById(R.id.tv_sensor_source);
        tvSource.setText("来源："
                + (ConfigurationManager.isBleSensor(mSensor.getFormatAddress())
                ? "BLE"
                : "WIFI"));

        //设置RecyclerView header
        List<Measurement> measurements = mSensor.getMeasurementCollections();
        mTvValueLabels[0] = (TextView) view.findViewById(R.id.tv_measurement1);
        mTvValueLabels[1] = (TextView) view.findViewById(R.id.tv_measurement2);
        if (mSensorInfoAdapter.getScheduledDisplayCount() >= SensorInfoAdapter.MAX_DISPLAY_COUNT) {
            ViewStub vsMeasurement = (ViewStub) view.findViewById(R.id.vs_measurement);
            mTvValueLabels[2] = (TextView) vsMeasurement.inflate();
        }
        setValueLabels(measurements);

        RecyclerView rvSensorInfo = (RecyclerView) view.findViewById(R.id.rv_sensor_info);
        //设置info orientation
        int displayState = mSensorInfoAdapter.getInfoDisplayState();
        if (displayState != SensorInfoAdapter.HAS_NO_EXCESS_DISPLAY_ITEM) {
            ViewStub vsInfoOrientation = (ViewStub) view.findViewById(R.id.vs_info_orientation);
            mIvInfoOrientation = (ImageView) vsInfoOrientation.inflate();
            setInfoOrientation(displayState);
            //rvSensorInfo.setOnFlingListener(mSensorInfoOnFlingListener);
            rvSensorInfo.setOnTouchListener(this);
            mSensorInfoAdapter.setOnDisplayStateChangeListener(this);
        }
        rvSensorInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSensorInfo.setAdapter(mSensorInfoAdapter);
        return view;
    }

    private void setValueLabels(List<Measurement> measurements) {
        for (int i = 0,
             measurementSize = mSensorInfoAdapter.getScheduledDisplayCount() - 1,
             displayCount = mSensorInfoAdapter.getActualDisplayCount(),
             offset = mSensorInfoAdapter.getDisplayStartIndex();
             i < displayCount;
             ++i) {
            Measurement measurement = offset + i < measurementSize
                    ? measurements.get(offset + i)
                    : null;
            mTvValueLabels[i].setText(measurement != null
                    ? measurement.getName()
                    : "电量");
        }
    }

    private void setInfoOrientation(int displayState) {
        switch (displayState) {
            case SensorInfoAdapter.ONLY_HAS_RIGHT_DISPLAY_ITEM:
                mIvInfoOrientation.setImageResource(R.drawable.ic_info_right);
                break;
            case SensorInfoAdapter.ONLY_HAS_LEFT_DISPLAY_ITEM:
                mIvInfoOrientation.setImageResource(R.drawable.ic_info_left);
                break;
            case SensorInfoAdapter.HAS_BOTH_DISPLAY_ITEM:
                mIvInfoOrientation.setImageResource(R.drawable.ic_info_both);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARGUMENT_KEY_START_MEASUREMENT_INDEX, mSensorInfoAdapter.getDisplayStartIndex());
    }

    public int show(FragmentTransaction transaction, Sensor sensor, boolean realTime) {
        mSensor = sensor;
        mRealTime = realTime;
        return super.show(transaction, TAG);
    }

    public void show(FragmentManager manager, Sensor sensor, boolean realTime) {
        mSensor = sensor;
        mRealTime = realTime;
        super.show(manager, TAG);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        throw new UnsupportedOperationException("use show(FragmentTransaction transaction, Sensor sensor) for instead");
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        throw new UnsupportedOperationException("use show(FragmentManager manager, Sensor sensor) for instead");
    }

    private boolean canNotifyValueChanged(Sensor sensor) {
        return mSensorInfoAdapter != null
                && mSensor != sensor
                && getDialog() != null
                && getDialog().isShowing();
    }

    public void notifySensorDataChanged(Sensor sensor, int position) {
        if (!canNotifyValueChanged(sensor)) {
            return;
        }
        switch (mSensor.interpretAddResult(position, mRealTime)) {
            case Sensor.NEW_VALUE_ADDED:
                mSensorInfoAdapter.notifyItemInserted(position);
                break;
            case Sensor.LOOP_VALUE_ADDED:
                mSensorInfoAdapter.notifyItemRangeChanged(0, mSensorInfoAdapter.getItemCount());
                break;
            case Sensor.VALUE_UPDATED:
                mSensorInfoAdapter.notifyItemChanged(position);
                break;
            default:
                break;
        }
    }

    public void notifyMeasurementDataChanged(Sensor sensor, int position, long timestamp) {
        if (!canNotifyValueChanged(sensor)
                || mSensor.interpretAddResult(position, mRealTime) == Measurement.ADD_VALUE_FAILED) {
            return;
        }
        int sensorValuePosition = mSensor.findHistoryValuePosition(position, timestamp);
        if (sensorValuePosition >= 0) {
            mSensorInfoAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onInfoOrientationChanged(int newDisplayState) {
        setInfoOrientation(newDisplayState);
    }

    @Override
    public void onDisplayStartIndexChanged(int newDisplayStartIndex) {
        setValueLabels(mSensor.getMeasurementCollections());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - mLastTouchX;
                float absX = Math.abs(deltaX);
                if (absX > 100 && absX > Math.abs(event.getY() - mLastTouchY)) {
                    if (deltaX > 0) {
                        mSensorInfoAdapter.showNextItem(true);
                        return true;
                    } else if (deltaX < 0) {
                        mSensorInfoAdapter.showNextItem(false);
                        return true;
                    }
                }
        }
        return false;
    }
}
