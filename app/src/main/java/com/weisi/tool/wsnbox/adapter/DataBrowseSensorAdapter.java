package com.weisi.tool.wsnbox.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegateManager;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.CodeRunTimeCatcher;
import com.weisi.tool.wsnbox.util.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/9/18.
 */

public class DataBrowseSensorAdapter extends RecyclerViewBaseAdapter<Sensor> {

    private final List<Sensor> mSensors;
    //private final SingleMeasurementSensorAdapterDelegate mSingleDelegate = new SingleMeasurementSensorAdapterDelegate();
    //private final List<MultipleMeasurementSensorAdapterDelegate> mMultipleDelegates = new ArrayList<>();
    //private final List<BaseSensorAdapterDelegate> mSensorAdapterDelegates = new ArrayList<>();
    private boolean mIsDescend;

    public DataBrowseSensorAdapter(AdapterDelegateManager<Sensor> manager,
                                   List<Sensor> sensors) {
        super(manager);
        mSensors = sensors;
    }

    public boolean setOrder(boolean isDescend) {
        if (mIsDescend != isDescend) {
            mIsDescend = isDescend;
            return true;
        }
        return false;
    }

//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        CodeRunTimeCatcher.start();
//        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
//        ClosableLog.d(Tag.LOG_TAG_D_CREATE_BIND_VIEW_HOLDER_RUN_TIME,
//                "on create view holder, type = "
//                        + viewType
//                        + ", position = "
//                        + holder.getLayoutPosition()
//                        + ", time = "
//                        + CodeRunTimeCatcher.end() / 100000);
//        return holder;
//    }

//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        CodeRunTimeCatcher.start();
//        super.onBindViewHolder(holder, position);
//        ClosableLog.d(Tag.LOG_TAG_D_CREATE_BIND_VIEW_HOLDER_RUN_TIME,
//                "on bind view holder, type = "
//                        + holder.getItemViewType()
//                        + ", position = "
//                        + position
//                        + ", time = "
//                        + CodeRunTimeCatcher.end() / 100000);
//    }

//    @Override
//    public void onAddAdapterDelegate() {
//        mSensorAdapterDelegates.add(new SingleMeasurementSensorAdapterDelegate());
//        mSensorAdapterDelegates.add(new TwoMeasurementsSensorAdapterDelegate());
//        mSensorAdapterDelegates.add(new ThreeMeasurementsSensorAdapterDelegate());
//        mSensorAdapterDelegates.add(new FourMeasurementsSensorAdapterDelegate());
//    }

//    @Override
//    protected AdapterDelegate<Sensor> getAdapterDelegate(int viewType) {
////        if (viewType == mSingleDelegate.getItemViewType()) {
////            return mSingleDelegate;
////        }
//        while (viewType > mSensorAdapterDelegates.size()) {
//            mSensorAdapterDelegates.add(null);
//        }
//        BaseSensorAdapterDelegate delegate = mSensorAdapterDelegates.get(viewType - 1);
//        if (delegate == null) {
//            delegate = new MultipleMeasurementSensorAdapterDelegate(viewType);
//            mSensorAdapterDelegates.set(viewType - 1, delegate);
//        }
//        return delegate;
//    }

    @Override
    public Sensor getItemByPosition(int position) {
        return mSensors.get(getItemDisplayPosition(position));
    }

    @Override
    public int getItemCount() {
        return mSensors.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemByPosition(position)
                .getMeasurementCollections()
                .size() - 1;
    }

    private int getItemDisplayPosition(int structurePosition) {
        return mIsDescend
                ? mSensors.size() - 1 - structurePosition
                : structurePosition;
    }

    public void notifySensorNetIn(int position) {
        notifyItemInserted(getItemDisplayPosition(position));
    }

    public void notifySensorValueUpdate(int position) {
        notifyItemChanged(getItemDisplayPosition(position),
                BaseSensorAdapterDelegate.UPDATE_TYPE_VALUE_CHANGED);
    }

    public void notifySensorOrderChanged() {
        notifyItemRangeChanged(0, getItemCount());
    }

    public void notifySensorLabelChanged() {
        notifyItemRangeChanged(0, getItemCount(),
                BaseSensorAdapterDelegate.UPDATE_TYPE_SENSOR_LABEL_CHANGED);
    }

    public void notifyMeasurementLabelChanged() {
        notifyItemRangeChanged(0, getItemCount(),
                BaseSensorAdapterDelegate.UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED);
    }

    public void notifySensorFilterChanged(int previousSize) {
        int currentSize = getItemCount();
        if (previousSize < currentSize) {
            notifyItemRangeChanged(0, previousSize);
            notifyItemRangeInserted(previousSize, currentSize - previousSize);
        } else if (previousSize > currentSize) {
            notifyItemRangeChanged(0, currentSize);
            notifyItemRangeRemoved(currentSize, previousSize - currentSize);
        } else {
            notifyItemRangeChanged(0, currentSize);
        }
    }
}
