package com.android.ailao.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Message;
import android.widget.Toast;

import com.android.ailao.MainActivity;
import com.android.ailao.data.MyLocation;
import com.android.ailao.data.MyRecord;
import com.android.ailao.permissions.CheckNetwork;
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.services.SaveLocations;
import com.android.ailao.tools.HandlerMain;

import com.android.ailao.tools.ServiceIsRun;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.LineSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.List;

public class MapConfig {

    private MainActivity mActivity;
    private Context mContext;

    private MapView mMapView;
    private ArcGISMap arcGISMap;

    private LocationChangeReceiver locationChangeReceiver;

    private PointCollection pointCollection;
    private GraphicsOverlay graphicsOverlay;
    private LineSymbol lineSymbol;

    public MapConfig(MainActivity mActivity, Context mContext) {
        this.mActivity = mActivity;
        this.mContext = mContext;

        //初始化点收集器
        this.pointCollection = new PointCollection(SpatialReferences.getWgs84());
        //初始化线条样式
        lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 3.0f);
    }

    /**
     * 显示地图初始位置，联网权限检查
     */
    public void initMap(MapView mMapView){
        this.mMapView = mMapView;

        /**
         * 网络可用，加载地图
         */
        if(CheckNetwork.isNetworkAvailable(mContext)){
            arcGISMap = new ArcGISMap(Basemap.Type.IMAGERY,
                    25.063463,102.758171,16);
            /**
             * 底图
             */
            final String layerURL = "http://202.203.134.147:6080/arcgis/rest/services/AilaoFeature/AilaoFeatureService/MapServer";
            final ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer(layerURL);
            arcGISMap.getOperationalLayers().add(mapImageLayer);
            /**
             * 显示地图
             * 显示轨迹初始化
             */
            if(mMapView != null){
                mMapView.setMap(arcGISMap);

                //初始化路径图层
                graphicsOverlay = new GraphicsOverlay();
                mMapView.getGraphicsOverlays().add(graphicsOverlay);
            }
        }else {
            /**
             * 网络不可用，跳转设置界面设置网络
             */
            CheckNetwork.settingNetwork(mActivity);
        }
    }

    /**
     * 判断ArcGISMap是否加载完成
     */
    public void arcGISMapListener(){
        /**
         * 判断ArcGIS是否加载完成，加载完成后显示定位按钮、缩放按钮
         */
        if(arcGISMap != null){
            final HandlerMain handlerMain = new HandlerMain(MapConfig.this,mActivity);
            arcGISMap.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if(arcGISMap.getLoadStatus().equals(LoadStatus.LOADED)){
                        /**
                         * 从Message缓存池中取出Message对象
                         */
                        Message msg = handlerMain.obtainMessage();
                        msg.what = 201;
                        handlerMain.sendMessage(msg);
                    }
                }
            });
        }
    }

    /**
     * 显示设备位置，检查获取位置权限
     */
    public void showDeviceLocation(){
        if(this.mMapView != null){
            LocationDisplay locationDisplay = mMapView.getLocationDisplay();
            /**
             * LocationDisplay状态监听
             */
            locationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
                @Override
                public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                    if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                        return;
                    }
                    /**
                     * 检查权限
                     */
                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
                    CheckPermissions checkPermissions = new CheckPermissions(mActivity,mContext);

                    if(!(checkPermissions.checkPermission(permissions))){
                        //请求码
                        int requestLocateCode = 101;
                        String rational = "无法获取到定位权限软件将无法使用，现在去设置界面打开定位权限？";
                        checkPermissions.requestPermissions(rational,requestLocateCode,permissions);
                    }else{
                        String message = String.format("出现错误: %s",
                                dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    }
                }
            });
            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
            locationDisplay.startAsync();

            //开启服务
            startSaveLocationService();
        }
    }

    /**
     * 判断服务是否开启，否则开启服务
     * 注册广播接收器
     */
    private void startSaveLocationService(){
        //服务是否开启
        boolean isSaveLocationServiceRun = ServiceIsRun.isServiceExisted(SaveLocations.class.getName(),mContext);
        if(!isSaveLocationServiceRun){
            Intent startSaveLocationService = new Intent(mContext,SaveLocations.class);
            mContext.startService(startSaveLocationService);
        }
    }

    /**
     * 绘制路径
     */
    private void drawLocationLine(PointCollection pointCollection){
        try{
            Graphic graphic = new Graphic(new Polyline(pointCollection),lineSymbol);
            graphicsOverlay.getGraphics().add(graphic);
        }catch (Exception e){
            // 第一次从数据库中加载数据是Point为空
            e.printStackTrace();
        }
    }

    /**
     * 查询数据库中存在的点
     */
    public void selectPointFromDB(){
        //结束标志位为1，记录还没有结束
        List<MyRecord> myRecords = LitePal.where("isOver=?","1").find(MyRecord.class);
        if(myRecords.size()>0){
            //根据RecordID查找记录
            long recordId = myRecords.get(0).getRecordId();
            LitePal.where("recordId=?",""+recordId)
                    .order("id")
                    .findAsync(MyLocation.class)
                    .listen(new FindMultiCallback<MyLocation>() {
                        @Override
                        public void onFinish(List<MyLocation> list) {
                            //是否存在记录
                            if(list.size()>0){
                                //迭代记录
                                for(int i=0; i<list.size(); i++){
                                    pointCollection.add(list.get(i).getLng(),list.get(i).getLat());
                                }
                            }
                            //开始绘制路径
                            drawLocationLine(pointCollection);
                        }
                    });
        }
    }

    /**
     * 广播接收器
     */
    private class LocationChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            double lat = intent.getDoubleExtra("lat",0.0);
            double lng = intent.getDoubleExtra("lng",0.0);

            pointCollection.add(lng,lat);

            drawLocationLine(pointCollection);

            //阻止广播继续传递
            abortBroadcast();
        }
    }

    /**
     * 注册广播接收器
     */
    public void registLocationChangeReceiver(){
        locationChangeReceiver = new LocationChangeReceiver();
        IntentFilter locationChangeFilter = new IntentFilter("android.ailao.locationChangeReceiver");
        mContext.registerReceiver(locationChangeReceiver,locationChangeFilter);
    }

    /**
     * 注销广播
     */
    public void unRegistLocationChangeReceiver(){
            mContext.unregisterReceiver(locationChangeReceiver);
    }
}
