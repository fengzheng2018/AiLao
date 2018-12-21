package com.android.ailao;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ailao.gps.GpsHandler;
import com.android.ailao.gps.GpsProxy;
import com.android.ailao.map.MapConfig;
import com.android.ailao.map.MapScaleListener;
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.picture.MyPicture;
import com.android.ailao.tools.MyBaseApplication;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private MapView mMapView;
    private Context mContext;
    private DrawerLayout mDrawLayout;

    private FloatingActionButton floatingActionButton;
    private FloatingActionButton openCamera;
    private FloatingActionButton openMicroPhone;

    private ImageButton zoomIn;
    private ImageButton zoomOut;

    private TextView gpsStatusTxt;

    private MyBaseApplication baseApplication;
    private MapConfig mapConfig;
    private long pressTime;
    private MyPicture myPicture;
    private GpsProxy gpsProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseApplication = MyBaseApplication.getInstance();

        /**
         * 顶部Toolbar
         */
        Toolbar mToolbar = findViewById(R.id.toolBar_top_container);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        /**
         * 侧边滑动栏
         */
        mDrawLayout = findViewById(R.id.drawLayout_container);
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(MainActivity.this,mDrawLayout,mToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        /**
         * GPS状态显示
         */
        gpsStatusTxt = findViewById(R.id.gps_status_txt);
    }

    @Override
    protected void onStart(){
        super.onStart();

        mContext = MainActivity.this;
        baseApplication.addActivity(MainActivity.this);

        /**
         * 设置侧滑栏菜单项监听
         */
        NavigationView navigationView = findViewById(R.id.navigation_left_content_item);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        /**
         * 设置定位按钮监听
         */
        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.hide();//隐藏
        floatingActionButton.setOnClickListener(actionButtonListener);

        /**
         * 设置缩放按钮监听
         */
        zoomIn = findViewById(R.id.mapview_zoom_in);
        zoomOut = findViewById(R.id.mapview_zoom_out);
        zoomIn.setVisibility(View.INVISIBLE);
        zoomOut.setVisibility(View.INVISIBLE);
        zoomIn.setOnClickListener(actionButtonListener);
        zoomOut.setOnClickListener(actionButtonListener);

        /**
         * 设置拍照按钮监听
         */
        openCamera = findViewById(R.id.FloatingToTakePhoto);
        openCamera.hide();
        openCamera.setOnClickListener(actionButtonListener);

        /**
         * 设置麦克风按钮监听
         */
        openMicroPhone = findViewById(R.id.takeVoice);
        openMicroPhone.hide();
        openMicroPhone.setOnClickListener(actionButtonListener);

        /**
         * 地图的初始化
         */
        mMapView = findViewById(R.id.MapView_center_container);
        baseApplication.setmMapView(mMapView);
        mapConfig = new MapConfig(MainActivity.this,mContext);

        /**
         * 地图比例监听
         */
        mMapView.addMapScaleChangedListener(new MapScaleListener(zoomIn,zoomOut));

        /**
         * 初始加载状态
         */
        mapConfig.initMap(mMapView);
        /**
         * 加载过程监听
         */
        mapConfig.arcGISMapListener();

        /**
         * GPS状态显示
         */
        GpsHandler gpsHandler = new GpsHandler(gpsStatusTxt);
        gpsProxy = GpsProxy.getInstance(mContext,MainActivity.this,gpsHandler);
        gpsProxy.initEnvironment();
    }


    /**
     * 显示顶部toolBar菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_top_menu1, menu);
        return true;
    }

    /**
     * 显示定位按钮、缩放按钮控件
     * 显示拍照按钮、麦克风按钮
     */
    public void showViews(){
        floatingActionButton.show();
        zoomIn.setVisibility(View.VISIBLE);
        zoomOut.setVisibility(View.VISIBLE);
        openCamera.show();
        openMicroPhone.show();
    }

    /**
     * 监听顶部toolBar菜单栏点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tooBar_top_menu_item1) {
            Toast.makeText(mContext,"点击了顶部菜单栏第一项",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 侧滑栏底部登陆注册按钮监听
     */
    public void btnLoginAndSign(View v){
        switch (v.getId()){
            case R.id.navigation_left_button1:{
                Toast.makeText(mContext,"点击了登陆",Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.navigation_left_button2:{
                Toast.makeText(mContext,"点击了注册",Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    /**
     * 侧滑栏菜单项点击事件监听
     */
    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.navigation_left_item1) {
                Toast.makeText(mContext,"点击了菜单项第一项",Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navigation_left_item2) {

            } else if (id == R.id.navigation_left_item3) {

            } else if (id == R.id.navigation_left_item4) {

            } else if (id == R.id.navigation_left_item5) {

            }else if (id == R.id.navigation_left_item6){

            }

            mDrawLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    /**
     * 按返回键隐藏侧边滑动栏
     */
    @Override
    public void onBackPressed() {
        if (mDrawLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 定位按钮监听
     * 拍照按钮监听
     * 地图缩放按钮监听
     */
    private View.OnClickListener actionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.floatingActionButton:{
                    mapConfig.showDeviceLocation();
                    break;
                }
                case R.id.FloatingToTakePhoto:{
                    myPicture = new MyPicture(MainActivity.this,mContext);
                    myPicture.takePhotoPermission();
                    break;
                }
                case R.id.takeVoice:{
                    //先检查权限
                    CheckPermissions checkStorePermission = new CheckPermissions(MainActivity.this,mContext);
                    String[] storeAndReadPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
                    if(checkStorePermission.checkPermission(storeAndReadPermissions)){
                        Intent intent = new Intent(MainActivity.this,RecordingActivity.class);
                        startActivityForResult(intent,704);
                    }else{
                        int storeRequestCode = 720;
                        String storeRational = "没有存储权限不能保存音频文件，现在去授予存储权限？";
                        checkStorePermission.requestPermissions(storeRational,storeRequestCode,storeAndReadPermissions);
                    }
                }
                case R.id.mapview_zoom_in:{
                    double mapScale = mMapView.getMapScale();
                    if(mapScale*0.5 <= 2000.0){
                        mMapView.setViewpointScaleAsync(2000.0);
                    }else{
                        mMapView.setViewpointScaleAsync(mapScale*0.5);
                    }
                    break;
                }
                case R.id.mapview_zoom_out:{
                    double mapScale = mMapView.getMapScale();
                    if(mapScale*2 >= 10000000.0){
                        mMapView.setViewpointScaleAsync(10000000.0);
                    }else{
                        mMapView.setViewpointScaleAsync(mapScale*2);
                    }
                    break;
                }
            }
        }
    };

    /**
     * 重写onKeyDown方法
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            exit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * 退出应用方法
     */
    private void exit(){
        if((System.currentTimeMillis() - pressTime) > 2000){
            Toast.makeText(this,"再按一次退出应用",Toast.LENGTH_SHORT).show();
            this.pressTime = System.currentTimeMillis();
        }else{
            baseApplication.removeAllActivity();
        }
    }


    /**
     * 结果返回主页面
     */
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);

        switch (requestCode){
            case 153:{
                if (resultCode==RESULT_OK) {
                    if(myPicture != null){
                        myPicture.compressionPicture();
                    }
                }
                /**
                 * 创建了一个空的图片文件，删除文件
                 */
                else{
                    myPicture.deleteImgFile();
                }
                break;
            }
            case 704:{
                if(resultCode == RESULT_OK){
                    Intent collectionInfoActivity = new Intent(MainActivity.this,CollectionInfoActivity.class);
                    startActivity(collectionInfoActivity);
                }
                break;
            }
        }
    }

    /**
     * 重写onRequestPermissionsResult方法，用于接收请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    /**
     * 请求权限成功
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case 101:{
                mapConfig.showDeviceLocation();
                break;
            }
            case 720:{
                Intent intent = new Intent(MainActivity.this,RecordingActivity.class);
                startActivityForResult(intent,704);
                break;
            }
            case 807:{
                gpsProxy.initEnvironment();
                break;
            }
        }
    }

    /**
     * 请求权限失败
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        /**
         * 跳转到设置界面，让用户手动开启
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mMapView.dispose();

        //取消GPS监听
        gpsProxy.removeListener();
    }

    @Override
    protected void onStop(){
        super.onStop();

        //取消广播监听
        mapConfig.unRegistLocationChangeReceiver();
    }
}
