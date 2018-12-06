package com.android.ailao.data;

import org.litepal.crud.LitePalSupport;

/**
 * 数据库中用来记录数据属于哪次记录，记录是否结束
 */
public class MyRecord extends LitePalSupport{
    private long recordId;
    private int isOver;

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public int getIsOver() {
        return isOver;
    }

    public void setIsOver(int isOver) {
        this.isOver = isOver;
    }
}
