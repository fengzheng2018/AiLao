package com.android.ailao.picture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.ailao.CollectionInfoActivity;
import com.android.ailao.MainActivity;
import com.android.ailao.data.MyRecord;
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.tools.MyBaseApplication;
import com.esri.arcgisruntime.mapping.view.MapView;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MyPicture {

    private MainActivity mainActivity;
    private Context mContext;
    private MyBaseApplication baseApplication;
    private MapView mMapView;

    private File imageFile;

    public MyPicture(MainActivity mainActivity, Context mContext) {
        this.mainActivity = mainActivity;
        this.mContext = mContext;
        baseApplication = MyBaseApplication.getInstance();
        mMapView = baseApplication.getmMapView();
    }

    /**
     * 检查存储权限
     * 检查拍照权限
     */
    public void takePhotoPermission(){
        String[] storeAndReadPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        CheckPermissions checkPermissions = new CheckPermissions(mainActivity,mContext);

        /**
         * 如果不存在读写权限，请求权限
         */
        if(!checkPermissions.checkPermission(storeAndReadPermissions)){
            int requestReadAndWriteConde = 151;
            String rational = "没有存储权限将不能拍照，现在去打开存储权限？";
            checkPermissions.requestPermissions(rational,requestReadAndWriteConde,storeAndReadPermissions);
        }
        /**
         * 已经获得存储权限，检查拍照权限
         */
        else{
            String[] cameraPermission = {Manifest.permission.CAMERA};
            /**
             * 如果不存在拍照权限，检查拍照权限
             */
            if(!checkPermissions.checkPermission(cameraPermission)){
                int requestTakePhotoPermission = 152;
                String rational = "没有拍照权限将不能打开摄像机，现在去打开权限？";
                checkPermissions.requestPermissions(rational,requestTakePhotoPermission,cameraPermission);
            }
            /**
             * 存在拍照权限，打开摄像机
             */
            else {
                openCamera();
            }
        }
    }

    /**
     * 打开摄像机拍照
     */
    private void openCamera(){
        //是否有摄像机
        if(isExistCamera()){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageFile = createImageFile();

            /**
             * 成功创建了图片文件
             */
            Uri tempPath;

            if(imageFile != null){
                //手机版本在安卓7及以上，使用FileProvider获得Uri
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    tempPath = FileProvider.getUriForFile(mContext,mContext.getPackageName()+".provider",imageFile);
                }else{
                    tempPath = Uri.fromFile(imageFile);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,tempPath);

                /**
                 * 跳转到拍照页面
                 */
                mainActivity.startActivityForResult(intent,153);
            }
            /**
             * 创建文件失败
             */
            else{
                Toast.makeText(mContext,"创建文件失败，请检查是否给予存储权限！",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(mContext,"抱歉，设备找不到摄像机，不能拍照！！",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 压缩图片并存到AiLaoShan文件夹
     */
    public void compressionPicture(){

        /**
         * 创建AiLaoShan文件夹
         */
        boolean mkDirSuccess = true;
        String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Picture" +File.separator;
        File folder = new File(picPath);
        if(!folder.exists()){
            mkDirSuccess = folder.mkdirs();
        }

        if(mkDirSuccess){
            Luban.with(mContext)
                    .load(imageFile)
                    .ignoreBy(1024)
                    .setTargetDir(picPath)
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) {
                            /**
                             * 删除原图
                             */
                            deleteImgFile();
                            /**
                             * 压缩后的图片路径存数据库
                             */
                            String myPicName = file.getName();
                            storePicturePath(myPicName);
                            /**
                             * 打开信息收集界面
                             */
                            Intent collectionIntent = new Intent(mainActivity, CollectionInfoActivity.class);
                            mContext.startActivity(collectionIntent);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    }).launch();
        }
    }

    /**
     * 创建存储图片的文件
     */
    private File createImageFile(){
        String imageFileName = "JPEG_"+ System.currentTimeMillis();
        /**
         * 获取Picture公用文件夹路径
         */
        File storeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try{
            imageFile = File.createTempFile(imageFileName,".jpg",storeDir);
        }catch (IOException e){
            e.printStackTrace();
        }
        return imageFile;
    }

    /**
     * 检查是否有摄像机
     */
    private boolean isExistCamera(){
        PackageManager manager = mContext.getPackageManager();
        if(manager.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 删除空的图片文件
     */
    public void deleteImgFile(){
        if(imageFile.exists()){
            imageFile.delete();
        }
    }

    /**
     * 压缩后的图片存数据库
     */
    public void storePicturePath(String picName){
        /**
         * 查找未完成的记录
         */
        long recordId = 0;
        List<MyRecord> myRecords = LitePal.where("isOver=?","1").find(MyRecord.class);
        if(myRecords.size()>0){
            MyRecord myRecord = myRecords.get(0);
            recordId = myRecord.getRecordId();
        }
        /**
         * 没有未完成的记录，新建一次记录
         */
        else{
            recordId = System.currentTimeMillis();

            //向MyRecord表中添加一条记录
            MyRecord myRecord = new MyRecord();
            myRecord.setIsOver(1);
            myRecord.setRecordId(recordId);

            myRecord.save();
        }

        /**
         * 向MyPicture表中添加数据
         */
        if(recordId != 0){
            com.android.ailao.data.MyPicture myPictureData = new com.android.ailao.data.MyPicture();
            myPictureData.setPicName(picName);
            myPictureData.setRecordId(recordId);

            myPictureData.save();
        }
    }
}
