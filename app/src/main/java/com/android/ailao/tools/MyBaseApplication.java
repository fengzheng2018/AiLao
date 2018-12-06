package com.android.ailao.tools;

import android.app.Activity;
import android.app.Application;

import com.esri.arcgisruntime.mapping.view.MapView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MyBaseApplication extends Application {
    private static MyBaseApplication baseApplication;
    private List<Activity> activityList;
    private MapView mMapView;

    @Override
    public void onCreate(){
        super.onCreate();

        baseApplication = MyBaseApplication.this;
        activityList = new ArrayList<>();

        LitePal.initialize(this);
    }

    /**
     * 静态方法，返回baseApplication实例
     */
    public static MyBaseApplication getInstance(){
        return  baseApplication;
    }

    /**
     * 添加activity
     */
    public void addActivity(Activity activity){
        if(!activityList.contains(activity)){
            activityList.add(activity);
        }
    }

    /**
     * 销毁单个activity
     */
    public void removeActivity(Activity activity){
        if(activityList.contains(activity)){
            activityList.remove(activity);
            activity.finish();
        }
    }

    /**
     * 销毁所有activity
     */
    public void removeAllActivity(){
        for(Activity activity : activityList){
            activity.finish();
        }
    }

    /**
     * MapView对象的Getter和Setter方法
     */
    public MapView getmMapView() {
        return mMapView;
    }
    public void setmMapView(MapView mMapView) {
        this.mMapView = mMapView;
    }
}
