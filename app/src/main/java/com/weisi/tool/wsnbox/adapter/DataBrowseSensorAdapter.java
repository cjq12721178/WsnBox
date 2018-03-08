package com.weisi.tool.wsnbox.adapter;

import com.cjq.lib.weisi.node.Sensor;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegateManager;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.weisi.tool.wsnbox.bean.storage.BaseSensorStorage;

import static com.weisi.tool.wsnbox.adapter.BaseSensorAdapterDelegate.*;

/**
 * Created by CJQ on 2017/9/18.
 */

public class DataBrowseSensorAdapter extends RecyclerViewBaseAdapter<Sensor> {

    private final BaseSensorStorage mSensorStorage;

    public DataBrowseSensorAdapter(AdapterDelegateManager<Sensor> manager,
                                   BaseSensorStorage storage) {
        super(manager);
        mSensorStorage = storage;
    }

    @Override
    public Sensor getItemByPosition(int position) {
        return mSensorStorage.getSensor(position);
    }

    @Override
    public int getItemCount() {
        return mSensorStorage.getSensorSize();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemByPosition(position)
                .getMeasurementCollections()
                .size() - 1;
    }

    public void notifySensorNetIn(int position) {
        notifyItemInserted(position);
    }

    public void notifySensorValueUpdate(int position) {
        notifyItemChanged(position, UPDATE_TYPE_VALUE_CHANGED);
    }

    public void notifyWarnProcessorLoaded() {
        notifyItemRangeChanged(0, getItemCount(), UPDATE_TYPE_VALUE_CHANGED);
    }

    public void notifySensorOrderChanged() {
        notifyItemRangeChanged(0, getItemCount());
    }

    public void notifySensorLabelChanged() {
        notifyItemRangeChanged(0, getItemCount(),
                UPDATE_TYPE_SENSOR_LABEL_CHANGED);
    }

    public void notifyMeasurementLabelChanged() {
        notifyItemRangeChanged(0, getItemCount(),
                UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED);
    }

    public void notifySensorFilterChanged(int previousSize, int currentSize) {
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
