package com.weisi.tool.wsnbox.fragment;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjq.lib.weisi.sensor.Measurement;
import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.adapter.SensorInfoAdapter;
import com.weisi.tool.wsnbox.io.SensorDatabase;

import java.util.Calendar;
import java.util.List;


/**
 * Created by CJQ on 2017/11/3.
 */

public class SensorInformationFragment
        extends DialogFragment
        implements SensorInfoAdapter.OnDisplayStateChangeListener,
        View.OnTouchListener, View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private static final String ARGUMENT_KEY_START_MEASUREMENT_INDEX = "smi";
    public static final String TAG = "sensor_info";
    private boolean mRealTime;
    private Sensor mSensor;
    private SensorInfoAdapter mSensorInfoAdapter;
    private ImageView mIvInfoOrientation;
    private TextView[] mTvValueLabels = new TextView[SensorInfoAdapter.MAX_DISPLAY_COUNT];
    private float mLastTouchX;
    private float mLastTouchY;
    private TextView mTvDate;
    private Calendar mDateOperator = Calendar.getInstance();
    private DatePickerDialog mDatePickerDialog;

    public void setSensor(Sensor sensor) {
        mSensor = sensor;
    }

    public Sensor getSensor() {
        return mSensor;
    }

    public void setRealTime(boolean realTime) {
        mRealTime = realTime;
    }

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
        //tvTitle.setText(mCurrentSensor.getName() + "详细信息");
        tvTitle.setText(getString(R.string.sensor_info_title, mSensor.getName()));
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_sensor_address);
        //tvAddress.setText("地址：" + mCurrentSensor.getFormatAddress());
        tvAddress.setText(getString(R.string.sensor_info_address, mSensor.getFormatAddress()));
        TextView tvState = (TextView) view.findViewById(R.id.tv_sensor_state);
        tvState.setText(mSensor.getState() == Sensor.State.ON_LINE
                        ? R.string.sensor_info_state_on
                        : R.string.sensor_info_state_off);
        mTvDate = (TextView) view.findViewById(R.id.tv_date);
//        if (mSensor.getIntraday() == 0) {
//            mSensor.setIntraday(System.currentTimeMillis());
//        }
//        if ()
//        chooseDate(mSensor.getIntraday());
//        tvDate.setText("来源："
//                + (ConfigurationManager.isBleSensor(mCurrentSensor.getFormatAddress())
//                ? "BLE"
//                : "WIFI"));

        //设置日期标签及选择面板（历史）
        if (mRealTime) {
            setDateLabel(System.currentTimeMillis());
        } else {
            chooseDate(0);
            ViewStub vsDateChooser = (ViewStub) view.findViewById(R.id.vs_date_chooser);
            View vDateChooser = vsDateChooser.inflate();
            vDateChooser.findViewById(R.id.btn_today).setOnClickListener(this);
            vDateChooser.findViewById(R.id.btn_previous_day).setOnClickListener(this);
            vDateChooser.findViewById(R.id.btn_next_day).setOnClickListener(this);
            vDateChooser.findViewById(R.id.btn_custom_day).setOnClickListener(this);
        }

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

    private void chooseDate(long date) {
        boolean isDateChanged = true;
        if (date == 0) {
            if (mSensor.getIntraday() == 0) {
                mSensor.setIntraday(System.currentTimeMillis());
            } else {
                isDateChanged = false;
            }
        } else if (date > 0) {
            if (mSensor.isIntraday(date)) {
                isDateChanged = false;
            } else {
                mSensor.setIntraday(date);
                mSensorInfoAdapter.notifyDataSetChanged();
            }
        } else {
            throw new IllegalArgumentException("intraday start time may not be less than 0");
        }
        if (isDateChanged) {
            long dateTime = mSensor.getIntraday();
            setDateLabel(dateTime);
            ImportSensorHistoryDataTask task = new ImportSensorHistoryDataTask();
            task.execute(mSensor, dateTime);
        }
    }

    private void setDateLabel(long date) {
        mTvDate.setText(getString(R.string.sensor_info_date, date));
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
                && mSensor == sensor
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_today:
                chooseDate(System.currentTimeMillis());
                break;
            case R.id.btn_previous_day:
                chooseDate(getPreviousDayTime(mSensor.getIntraday()));
                break;
            case R.id.btn_next_day:
                chooseDate(getNextDayTime(mSensor.getIntraday()));
                break;
            case R.id.btn_custom_day:
                if (mDatePickerDialog == null) {
                    mDateOperator.setTimeInMillis(mSensor.getIntraday());
                    mDatePickerDialog = new DatePickerDialog(getContext(),
                            this,
                            mDateOperator.get(Calendar.YEAR),
                            mDateOperator.get(Calendar.MONTH),
                            mDateOperator.get(Calendar.DAY_OF_MONTH));
                }
                mDatePickerDialog.show();
                break;
        }
    }

    private long getPreviousDayTime(long sourceDate) {
        mDateOperator.setTimeInMillis(sourceDate);
        mDateOperator.add(Calendar.DAY_OF_MONTH, -1);
        return mDateOperator.getTimeInMillis();
    }

    private long getNextDayTime(long sourceDate) {
        mDateOperator.setTimeInMillis(sourceDate);
        mDateOperator.add(Calendar.DAY_OF_MONTH, 1);
        return mDateOperator.getTimeInMillis();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDateOperator.set(Calendar.YEAR, year);
        mDateOperator.set(Calendar.MONTH, month);
        mDateOperator.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        chooseDate(mDateOperator.getTimeInMillis());
    }

    private class ImportSensorHistoryDataTask
            extends AsyncTask<Object, Object, Boolean>
            implements SensorDatabase.SensorHistoryInfoReceiver {

        private static final int TASK_SENSOR_DATA_RECEIVED = 1;
        private static final int TASK_MEASUREMENT_DATA_RECEIVED = 2;

        private Sensor mCurrentSensor;

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null) {
                return false;
            }
            if (params.length < 2) {
                return false;
            }
            if (!(params[0] instanceof Sensor)) {
                return false;
            }
            if (!(params[1] instanceof Long)) {
                return false;
            }
            mCurrentSensor = (Sensor) params[0];
            if (mCurrentSensor.hasHistoryValue()) {
                return true;
            }
            //Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis((long) params[1]);
            long today = (long) params[1];
            //calendar.add(Calendar.DAY_OF_MONTH, 1);
            long tomorrow = getNextDayTime(today);
            return SensorDatabase.importSensorHistoryValues(
                    mCurrentSensor.getRawAddress(),
                    today,
                    tomorrow,
                    0,
                    this
            );
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            switch ((int) values[0]) {
                case TASK_SENSOR_DATA_RECEIVED:
                    notifySensorDataChanged(
                            mCurrentSensor,
                            mCurrentSensor.addHistoryValue((long) values[1],
                                    (float) values[2]));
                    break;
                case TASK_MEASUREMENT_DATA_RECEIVED:
                    notifyMeasurementDataChanged(
                            mCurrentSensor,
                            mCurrentSensor.addHistoryValue((long) values[1],
                                    (long) values[2],
                                    (double) values[3]),
                            (long) values[2]
                    );
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getChildFragmentManager(),
                        "import_sensor_history_value_failed",
                        getString(R.string.import_sensor_history_value_failed),
                        false);
            }
        }

        @Override
        public void onSensorDataReceived(int address, long timestamp, float batteryVoltage) {
            publishProgress(TASK_SENSOR_DATA_RECEIVED, timestamp, batteryVoltage);
        }

        @Override
        public void onMeasurementDataReceived(long measurementValueId, long timestamp, double rawValue) {
            publishProgress(TASK_MEASUREMENT_DATA_RECEIVED, measurementValueId, timestamp, rawValue);
        }
    }
}
