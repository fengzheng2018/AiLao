package com.android.ailao.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.android.ailao.data.MyDescribe;
import com.android.ailao.data.MyPicture;
import com.android.ailao.data.MyRecord;
import com.android.ailao.data.MyVoiceData;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

/**
 * 作为文件上传的守护服务，防止服务被销毁
 */
public class UploadService extends Service {

    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

        /**
         * 开启一个线程从数据库查找文件
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 查找存在的记录
                List<MyRecord> myRecordList = LitePal.where("isOver=?","1").find(MyRecord.class);
                // 依次从每条记录里取出文字描述、图片和声音
                for(int i=0; i<myRecordList.size(); i++){
                    uploadText(myRecordList.get(i).getRecordId());
                    uploadPicture(myRecordList.get(i).getRecordId());
                    uploadAudio(myRecordList.get(i).getRecordId());

                    Log.d("fengzheng","查找成功");
                }
            }
        }).start();

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){}


    /**
     * 从数据库中查找文字描述上传
     */
    private void uploadText(long recordId){

        // text文件路径
        String txtFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Text" +File.separator;

        // 根据recordId从数据库中查找记录
        List<MyDescribe> describeList = LitePal.where(
                "recordId=?",""+recordId).find(MyDescribe.class);

        for(int i=0; i<describeList.size(); i++){
            // 初始化Intent
            Intent intentService = new Intent(UploadService.this,UploadIntentService.class);

            // 文件名
            intentService.putExtra("fileName",describeList.get(i).getTxtName());
            // 文件路径
            intentService.putExtra("filePath",txtFilePath + describeList.get(i).getTxtName());
            // 文件类型
            intentService.putExtra("fileType","text");
            // recordId
            intentService.putExtra("recordId",describeList.get(i).getRecordId());
            // lat
            intentService.putExtra("lat",describeList.get(i).getLat());
            // lng
            intentService.putExtra("lng",describeList.get(i).getLng());

            // 开启Service
            startService(intentService);
        }
    }


    /**
     * 从数据库中查找图片上传
     */
    private void uploadPicture(long recordId){

        // Picture文件路径
        String pictureFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Picture" +File.separator;

        // 根据recordId从数据库中查找记录
        List<MyPicture> pictureList = LitePal.where(
                "recordId=?",""+recordId).find(MyPicture.class);

        for(int i=0; i<pictureList.size(); i++){
            // 初始化Intent
            Intent intentService = new Intent(UploadService.this,UploadIntentService.class);

            // 文件名
            intentService.putExtra("fileName",pictureList.get(i).getPicName());
            // 文件路径
            intentService.putExtra("filePath",pictureFilePath + pictureList.get(i).getPicName());
            // 文件类型
            intentService.putExtra("fileType","image");
            // recordId
            intentService.putExtra("recordId",pictureList.get(i).getRecordId());
            // lat
            intentService.putExtra("lat",pictureList.get(i).getLat());
            // lng
            intentService.putExtra("lng",pictureList.get(i).getLng());

            // 开启Service
            startService(intentService);
        }
    }

    /**
     * 从数据库中查找音频文件上传
     */
    private void uploadAudio(long recordId){

        // 音频文件路径
        String voiceFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Voice" +File.separator;

        // 根据recordId从数据库中查找文件
        List<MyVoiceData> voiceList = LitePal.where(
                "recordId=?",""+recordId).find(MyVoiceData.class);

        for(int i=0; i<voiceList.size(); i++){
            // 初始化Intent
            Intent intentService = new Intent(UploadService.this,UploadIntentService.class);

            // 文件名
            intentService.putExtra("fileName",voiceList.get(i).getVoiceName());
            // 文件路径
            intentService.putExtra("filePath",voiceFilePath + voiceList.get(i).getVoiceName());
            // 文件类型
            intentService.putExtra("fileType","audio");
            // recordId
            intentService.putExtra("recordId",voiceList.get(i).getRecordId());
            // lat
            intentService.putExtra("lat",voiceList.get(i).getLat());
            // lng
            intentService.putExtra("lng",voiceList.get(i).getLng());

            // 开启Service
            startService(intentService);
        }
    }
}
