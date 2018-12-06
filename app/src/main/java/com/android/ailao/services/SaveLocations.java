package com.android.ailao.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.ailao.R;
import com.android.ailao.data.MyLocation;
import com.android.ailao.data.MyRecord;
import com.android.ailao.tools.MyBaseApplication;
import com.android.ailao.tools.MyNotification;

import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

import org.litepal.LitePal;

import java.util.List;

public class SaveLocations extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        /**
         * 显示前台通知
         */
        final int NOTIFICATION_ID = 11;
        MyNotification myNotification = new MyNotification(this);
        myNotification.createNotificationChanel();
        Notification notification = myNotification.showNotification(R.string.notification_status2,R.string.notification_ticker2,true);

        startForeground(NOTIFICATION_ID,notification);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){
        /**
         * 获取MapView对象
         */
        MyBaseApplication myBaseApplication = MyBaseApplication.getInstance();
        final MapView mMapView = myBaseApplication.getmMapView();

        if(mMapView != null){
            /**
             * 开启线程
             */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LocationDisplay locationDisplay = mMapView.getLocationDisplay();

                    /**
                     * 检查上一次记录是否完成
                     */
                    long recordId = checkIsOver();

                    /**
                     * 上一次记录已经完成，使用系统当前时间戳作为recordId
                     */
                    if(recordId == 0){
                        recordId = System.currentTimeMillis();
                        //向MyRecord表中写入数据
                        storeToRecord(recordId);
                        //向MyLocation表中写入数据
                        storeToLocation(locationDisplay,recordId);
                    }
                    /**
                     * 上一次记录未完成
                     */
                    else{
                        //向MyLocation表中写入数据
                        storeToLocation(locationDisplay,recordId);
                    }
                }
            }).start();
        }else{
            MyNotification myNotification = new MyNotification(this);
            myNotification.createNotificationChanel();
            myNotification.showNotification(R.string.notification_error_status,R.string.notification_error_ticker,true);
        }

        /**
         * 如果服务被杀死，知道接收到新的开启命令，才会打开服务
         */
        return START_NOT_STICKY;
    }

    /**
     * 服务被销毁，本次记录结束，设置记录结束标志位为2
     */
    @Override
    public void onDestroy(){
        MyRecord myOverRecord = new MyRecord();
        myOverRecord.setIsOver(2);
        myOverRecord.updateAllAsync("isOver = 1","2");
    }

    /**
     * 查找未完成的记录
     */
    private long checkIsOver(){
        long recordId = 0;

        List<MyRecord> myRecords = LitePal.where("isOver=?","1").find(MyRecord.class);
        if(myRecords.size()>0){
            MyRecord myRecord = myRecords.get(0);
            recordId = myRecord.getRecordId();
        }
        return recordId;
    }

    /**
     * 向MyRecord表中添加数据
     */
    private void storeToRecord(long recordId){
        MyRecord myRecord = new MyRecord();
        myRecord.setIsOver(1);
        myRecord.setRecordId(recordId);

        myRecord.save();
    }

    /**
     * 监听位置变化，向MyLocation表中添加数据
     */
    private void storeToLocation(LocationDisplay locationDisplay, final long recordId){
        locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                LocationDataSource.Location location = locationChangedEvent.getLocation();

                /**
                 * 获取经纬度
                 */
                double lat = location.getPosition().getY();
                double lng = location.getPosition().getX();

                /**
                 * 存储位置
                 */
                MyLocation myLocation = new MyLocation();
                myLocation.setLat(lat);
                myLocation.setLng(lng);
                myLocation.setRecordId(recordId);
                myLocation.setRecordTime(System.currentTimeMillis());

                myLocation.save();

                /**
                 * 发送广播
                 */
                Intent intent = new Intent("android.ailao.locationChangeReceiver");
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                sendOrderedBroadcast(intent,null);
            }
        });
    }
}
