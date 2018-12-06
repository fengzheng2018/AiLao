package com.android.ailao.tools;

import android.os.Handler;
import android.os.Message;

import com.android.ailao.MainActivity;
import com.android.ailao.map.MapConfig;

import java.lang.ref.WeakReference;

public class HandlerMain extends Handler {
    private final WeakReference<MapConfig> weakMapConfig;
    private final WeakReference<MainActivity> weakMainActivity;

    public HandlerMain(MapConfig mapConfig, MainActivity mainActivity) {
        this.weakMapConfig = new WeakReference<>(mapConfig);
        this.weakMainActivity = new WeakReference<>(mainActivity);
    }

    @Override
    public void handleMessage(Message msg){
        super.handleMessage(msg);

        MapConfig mapConfig = this.weakMapConfig.get();
        MainActivity mainActivity = this.weakMainActivity.get();

        if(mapConfig != null){
            switch (msg.what){
                case 201:{
                    /**
                     * 显示定位按钮、缩放控件
                     */
                    mainActivity.showViews();
                    /**
                     * 显示设备位置
                     */
                    mapConfig.showDeviceLocation();
                    /**
                     * 注册广播接收器
                     */
                    mapConfig.registLocationChangeReceiver();
                    /**
                     * 查找数据库
                     */
                    mapConfig.selectPointFromDB();
                    break;
                }
            }
        }
    }
}
