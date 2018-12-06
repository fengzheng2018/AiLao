package com.android.ailao.data;

import org.litepal.crud.LitePalSupport;

/**
 * 数据库中用来记录每条位置信息的经纬度、记录的时间
 */
public class MyLocation extends LitePalSupport{
    private long recordId;
    private long recordTime;
    private double lng;
    private double lat;

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
