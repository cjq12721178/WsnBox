package com.weisi.tool.wsnbox.bean.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.weisi.tool.wsnbox.bean.filter.PhysicalSensorInfoFilter;
import com.weisi.tool.wsnbox.bean.filter.PhysicalSensorTypeFilter;
import com.weisi.tool.wsnbox.bean.filter.SensorUseForRealTimeFilter;
import com.weisi.tool.wsnbox.bean.filter.SensorWithHistoryValueFilter;
import com.weisi.tool.wsnbox.bean.sorter.SensorSorter;

import java.util.List;

/**
 * Created by CJQ on 2018/1/15.
 * 注意，本类中凡是方法中带listener的都会自动commit，否则需手动commit
 */

public class DataBrowseSensorStorage extends BaseSensorStorage<PhysicalSensor> implements Parcelable {

    public static final int SORTED_BY_ADDRESS = 1;
    public static final int SORTED_BY_TIME = 2;
    public static final int SORTED_BY_NAME = 3;

    private Filter<PhysicalSensor> mDataSourceFilter;
    private Filter<PhysicalSensor> mSensorProtocolFilter;
    private PhysicalSensorTypeFilter mPhysicalSensorTypeFilter;
    private PhysicalSensorInfoFilter mPhysicalSensorInfoFilter;

    public DataBrowseSensorStorage(boolean isRealTime) {
        super();
        setDataSource(isRealTime);
    }

    protected DataBrowseSensorStorage(Parcel in) {
        super();
        setDataSource(in.readByte() != 0);
        setSorter(in.readInt(), in.readByte() != 0);
        setSensorProtocol(in.readInt());
        setSensorType(in.readArrayList(Integer.class.getClassLoader()));
        setSearchContent(in.readString());
    }

    public static final Creator<DataBrowseSensorStorage> CREATOR = new Creator<DataBrowseSensorStorage>() {
        @Override
        public DataBrowseSensorStorage createFromParcel(Parcel in) {
            return new DataBrowseSensorStorage(in);
        }

        @Override
        public DataBrowseSensorStorage[] newArray(int size) {
            return new DataBrowseSensorStorage[size];
        }
    };

    public void setDataSource(boolean isRealTime) {
        setDataSource(isRealTime, false, false, null);
    }

    public void setDataSource(boolean isRealTime, OnSensorDataSourceChangeListener listener) {
        setDataSource(isRealTime, true, true, listener);
    }

    private void setDataSource(boolean isRealTime, boolean changeSorter, boolean isCommit, OnSensorDataSourceChangeListener listener) {
        Filter<PhysicalSensor> oldDataSourceFilter = mDataSourceFilter;
        if (isRealTime) {
            if (!(mDataSourceFilter instanceof SensorUseForRealTimeFilter)) {
                mDataSourceFilter = new SensorUseForRealTimeFilter();
            }
        } else {
            if (!(mDataSourceFilter instanceof SensorWithHistoryValueFilter)) {
                mDataSourceFilter = new SensorWithHistoryValueFilter();
            }
        }
        boolean changed = oldDataSourceFilter != mDataSourceFilter;
        if (changed) {
            //removeFilter(oldDataSourceFilter);
            //addFilter(mDataSourceFilter);
            if (changeSorter) {
                setSorter(getSensorSortType(), getSensorOrder());
            }
            if (isCommit) {
                commitDataSourceChange(listener);
            }
        }
    }

    public void commitDataSourceChange(OnSensorDataSourceChangeListener listener) {
        commitFilter(listener);
        if (listener != null) {
            listener.onDataSourceChange(getDataSource());
        }
    }

    public boolean getDataSource() {
        if (mDataSourceFilter instanceof SensorUseForRealTimeFilter) {
            return true;
        } else if (mDataSourceFilter instanceof SensorWithHistoryValueFilter) {
            return false;
        }
        throw new IllegalArgumentException("data source filter may not be null");
    }

    public int getSensorSortType() {
        SensorSorter sorter = getSensorSorter();
//        if (sorter instanceof SensorNetInTimeSorter) {
//            return SORTED_BY_TIME;
//        } else if (sorter instanceof SensorEarliestValueTimeSorter) {
//            return SORTED_BY_TIME;
//        } else if (sorter instanceof SensorAddressSorter) {
//            return SORTED_BY_ADDRESS;
//        } else if (sorter instanceof SensorNameSorter) {
//            return SORTED_BY_NAME;
//        }
        return SORTED_BY_TIME;
    }

//    public void setFilter(boolean isRealTime,
//                          OnSensorFilterChangeListener listener) {
//        setDataSource(isRealTime);
//        commitFilter(listener);
//    }

    public void setSorter(int type,
                          boolean isDescend) {
        setSorter(type, isDescend, false, null);
    }

    public void setSorter(int type,
                          boolean isDescend,
                          OnSensorSorterChangeListener listener) {
        setSorter(type, isDescend, true, listener);
    }

    private void setSorter(int type,
                           boolean isDescend,
                           boolean isCommit,
                           OnSensorSorterChangeListener listener) {
//        switch (type) {
//            case SORTED_BY_ADDRESS:
//                setSorter(new SensorAddressSorter(), isDescend, isCommit, listener);
//                break;
//            case SORTED_BY_NAME:
//                setSorter(new SensorNameSorter(), isDescend, isCommit, listener);
//                break;
//            case SORTED_BY_TIME:
//            default:
//                if (getDataSource()) {
//                    setSorter(new SensorNetInTimeSorter(), isDescend, isCommit, listener);
//                } else {
//                    setSorter(new SensorEarliestValueTimeSorter(), isDescend, isCommit, listener);
//                }
//                break;
//        }
    }

//    public int getSensorProtocolType() {
//        if (mSensorProtocolFilter instanceof BleProtocolFilter) {
//            return SensorProtocolFilter.BLE_PROTOCOL;
//        } else if (mSensorProtocolFilter instanceof EsbProtocolFilter) {
//            return SensorProtocolFilter.ESB_PROTOCOL;
//        } else {
//            return SensorProtocolFilter.ALL_PROTOCOL;
//        }
//    }

    public void setSensorProtocol(int sensorProtocolType) {
        setSensorProtocol(sensorProtocolType, false, null);
    }

    public void setSensorProtocol(int sensorProtocolType, OnSensorProtocolChangeListener listener) {
        setSensorProtocol(sensorProtocolType, true, listener);
    }

    private void setSensorProtocol(int sensorProtocolType, boolean isCommit, OnSensorProtocolChangeListener listener) {
//        if (sensorProtocolType < SensorProtocolFilter.ALL_PROTOCOL || sensorProtocolType > SensorProtocolFilter.ESB_PROTOCOL) {
//            throw new IllegalArgumentException("invalid sensor protocol type");
//        }
//        if (sensorProtocolType != getSensorProtocolType()) {
//            if (mSensorProtocolFilter != null) {
//                removeFilter(mSensorProtocolFilter);
//            }
//            switch (sensorProtocolType) {
//                case SensorProtocolFilter.ALL_PROTOCOL:
//                    mSensorProtocolFilter = null;
//                    break;
//                case SensorProtocolFilter.BLE_PROTOCOL:
//                    mSensorProtocolFilter = new BleProtocolFilter();
//                    break;
//                case SensorProtocolFilter.ESB_PROTOCOL:
//                    mSensorProtocolFilter = new EsbProtocolFilter();
//                    break;
//            }
//            if (mSensorProtocolFilter != null) {
//                addFilter(mSensorProtocolFilter);
//            }
//            if (isCommit) {
//                commitFilter(listener);
//                if (listener != null) {
//                    listener.onSensorProtocolChange(sensorProtocolType);
//                }
//            }
//        }
    }

    public List<Integer> getSensorTypeNos() {
        return mPhysicalSensorTypeFilter != null
                ? mPhysicalSensorTypeFilter.getSelectedSensorTypeNos()
                : null;
    }

    public void setSensorType(List<Integer> selectedSensorTypeNos) {
        setSensorType(selectedSensorTypeNos, false, null);
    }

    public void setSensorType(List<Integer> selectedSensorTypeNos, OnSensorTypeChangeListener listener) {
        setSensorType(selectedSensorTypeNos, true, listener);
    }

    private void setSensorType(List<Integer> selectedSensorTypeNos, boolean isCommit, OnSensorTypeChangeListener listener) {
        boolean changed = false;
        if (selectedSensorTypeNos == null) {
            if (mPhysicalSensorTypeFilter != null) {
                if (!mPhysicalSensorTypeFilter.getSelectedSensorTypeNos().isEmpty()) {
                    changed = true;
                }
                //removeFilter(mPhysicalSensorTypeFilter);
                mPhysicalSensorTypeFilter = null;
            }
        } else {
            if (mPhysicalSensorTypeFilter != null) {
                if (!mPhysicalSensorTypeFilter.getSelectedSensorTypeNos().equals(selectedSensorTypeNos)) {
                    mPhysicalSensorTypeFilter.setSelectedSensorTypeNos(selectedSensorTypeNos);
                    changed = true;
                }
            } else {
                mPhysicalSensorTypeFilter = new PhysicalSensorTypeFilter(selectedSensorTypeNos);
                //addFilter(mPhysicalSensorTypeFilter);
                changed = true;
            }
        }
        if (changed && isCommit) {
            commitFilter(listener);
            if (listener != null) {
                listener.onSensorTypeChange(getSensorTypeNos());
            }
         }
    }

    public String getSearchContent() {
        return mPhysicalSensorInfoFilter != null
                ? mPhysicalSensorInfoFilter.getKeyWord()
                : "";
    }

    public void setSearchContent(String keyWord) {
        setSearchContent(keyWord, false, null);
    }

    public void setSearchContent(String keyWord, OnSearchKeyWordChangeListener listener) {
        setSearchContent(keyWord, true, listener);
    }

    private void setSearchContent(String keyWord, boolean isCommit, OnSearchKeyWordChangeListener listener) {
        boolean changed = false;
        if (TextUtils.isEmpty(keyWord)) {
            if (mPhysicalSensorInfoFilter != null) {
                if (!TextUtils.isEmpty(mPhysicalSensorInfoFilter.getKeyWord())) {
                    changed = true;
                }
                //removeFilter(mPhysicalSensorInfoFilter);
                mPhysicalSensorInfoFilter = null;
            }
        } else {
            if (mPhysicalSensorInfoFilter != null) {
                if (!mPhysicalSensorInfoFilter.getKeyWord().equals(keyWord)) {
                    mPhysicalSensorInfoFilter.setKeyWord(keyWord);
                    changed = true;
                }
            } else {
                mPhysicalSensorInfoFilter = new PhysicalSensorInfoFilter(keyWord);
                //addFilter(mPhysicalSensorInfoFilter);
                changed = true;
            }
        }
        if (changed && isCommit) {
            commitFilter(listener);
            if (listener != null) {
                listener.onSearchKeyWordChange(keyWord);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mDataSourceFilter != null ? (getDataSource() ? 1 : 0) : 1));
        dest.writeInt(getSensorSortType());
        dest.writeByte((byte) (getSensorOrder() ? 1 : 0));
        //dest.writeInt(getSensorProtocolType());
        dest.writeList(getSensorTypeNos());
        dest.writeString(getSearchContent());
    }

    public interface OnSensorDataSourceChangeListener
            extends OnSensorFilterChangeListener,
            OnSensorSorterChangeListener<PhysicalSensor> {
        void onDataSourceChange(boolean isRealTime);
    }

    public interface OnSensorProtocolChangeListener extends OnSensorFilterChangeListener {
        void onSensorProtocolChange(int protocolType);
    }

    public interface OnSensorTypeChangeListener extends OnSensorFilterChangeListener {
        void onSensorTypeChange(List<Integer> sensorTypeNos);
    }

    public interface OnSearchKeyWordChangeListener extends OnSensorFilterChangeListener {
        void onSearchKeyWordChange(String keyWord);
    }
}
