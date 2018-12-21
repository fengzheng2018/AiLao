package com.android.ailao.gps;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

import com.android.ailao.MainActivity;
import com.android.ailao.permissions.CheckPermissions;

import java.util.Iterator;

public class GpsProxy {

    private static volatile GpsProxy gpsProxy;
    private Handler mHandler;
    private MainActivity mainActivity;
    private Context mContext;

    private LocationManager mLocationManager;

    private GpsProxy(Context mContext,Handler handler,MainActivity mainActivity) {
        this.mContext = mContext;
        this.mHandler = handler;
        this.mainActivity = mainActivity;
    }

    /**
     * 获取GpsProxy实例
     */
    public static GpsProxy getInstance(Context context,MainActivity activity,Handler handler){
        if(gpsProxy == null){
            //同步锁
            synchronized (GpsProxy.class){
                gpsProxy = new GpsProxy(context,handler,activity);
            }
        }
        return gpsProxy;
    }

    /**
     * 初始化环境
     */
    public void initEnvironment(){
        /**
         * 检查权限
         */
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        CheckPermissions checkPermissions = new CheckPermissions(mainActivity,mContext);

        if(!(checkPermissions.checkPermission(permissions))){
            //请求码
            int requestLocateCode = 807;
            String rational = "无法获取到定位权限软件将无法使用，现在去设置界面打开定位权限？";
            checkPermissions.requestPermissions(rational,requestLocateCode,permissions);

            return;
        }

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(mLocationManager != null){
            registerListener();
        }
    }

    /**
     * 注册监听器
     */
    private void registerListener(){
        if(mHandler != null){
            mLocationManager.addGpsStatusListener(gpsListener);
        }
    }

    /**
     * 移除监听器
     */
    public void removeListener(){
        if(mLocationManager != null){
            mLocationManager.removeGpsStatusListener(gpsListener);
        }
    }


    /**
     * 获取最后一个已知位置
     */
    public Location mGetLastKnownLocation(LocationManager locationManager){
        if(locationManager != null){
            //获取最佳位置提供器
            String bestProvider = mLocationManager.getBestProvider(getCriteria(),true);
            //获取最后一个已知位置
            return mLocationManager.getLastKnownLocation(bestProvider);
        }else{
            return null;
        }
    }


    /**
     * GPS状态监听器
     */
    private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event){
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:{
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    //获取卫星颗数默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> satelliteIterator = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (satelliteIterator.hasNext() && count <= maxSatellites){
                        GpsSatellite satellite = satelliteIterator.next();
                        //已定位卫星颗数
                        if(satellite.usedInFix()){
                            count ++;
                        }
                    }

                    Message msg = mHandler.obtainMessage();

                    msg.what = 806;
                    msg.arg1 = count;

                    mHandler.sendMessage(msg);
                    break;
                }
            }
        }
    };


    /**
     * 获取最佳位置提供器
     */
    private Criteria getCriteria(){
        Criteria criteria = new Criteria();

        //要求高精度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //是否要求速度
        criteria.setSpeedRequired(false);
        //是否允许运营商收费
        criteria.setCostAllowed(true);
        //是否需要方位信息
        criteria.setBearingRequired(false);
        //是否需要海拔信息
        criteria.setAltitudeRequired(false);
        //对电源需求
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        return criteria;
    }
}
