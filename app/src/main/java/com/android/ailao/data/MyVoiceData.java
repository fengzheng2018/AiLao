package com.android.ailao.data;

import org.litepal.crud.LitePalSupport;

public class MyVoiceData extends LitePalSupport{
    /**
     * 属于哪次记录
     */
    private long recordId;
    /**
     * 文件名
     */
    private String voiceName;
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

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
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
