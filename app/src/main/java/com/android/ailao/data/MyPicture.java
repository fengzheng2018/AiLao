package com.android.ailao.data;

import org.litepal.crud.LitePalSupport;

/**
 * 记录图片属于哪次记录
 */
public class MyPicture extends LitePalSupport {
    /**
     * 属于哪次记录
     */
    private long recordId;
    /**
     * 文件名
     */
    private String picName;

    /**
     * 经纬度
     */
    private double lat;
    private double lng;

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
