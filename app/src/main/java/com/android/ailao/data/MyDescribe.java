package com.android.ailao.data;

import org.litepal.crud.LitePalSupport;

public class MyDescribe extends LitePalSupport {
    /**
     * 属于哪次记录
     */
    private long recordId;
    /**
     * 文件名
     */
    private String txtName;
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

    public String getTxtName() {
        return txtName;
    }

    public void setTxtName(String txtName) {
        this.txtName = txtName;
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
