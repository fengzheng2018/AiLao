package com.android.ailao.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * https://my.oschina.net/zhangjie830621/blog/163770
 * https://blog.csdn.net/mahuicool/article/details/80724582
 */
public class UploadIntentService extends IntentService {

    public UploadIntentService() {
        super("UploadIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try{
            // 获取文件路径
            String filePath = intent.getStringExtra("filePath");

            // 判断文件是否存在，若存在构造请求体
            File uploadFile = new File(filePath);
            if(uploadFile.exists()) {
                RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"),uploadFile);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file",intent.getStringExtra("fileName"),fileBody)
                        .addFormDataPart("fileType", intent.getStringExtra("fileType"))
                        .addFormDataPart("recordId", String.valueOf(intent.getLongExtra("recordId", 0)) )
                        .addFormDataPart("lat", String.valueOf(intent.getDoubleExtra("lat", 0.0)) )
                        .addFormDataPart("lng", String.valueOf(intent.getDoubleExtra("lng", 0.0)) )
                        .build();

                final Request request = new Request.Builder()
                        .url("http://192.168.1.100:8080/server/uploadFile")
                        .post(requestBody)
                        .build();

                // 发送请求
                OkHttpClient okHttpClient = new OkHttpClient();

                Log.d("fengzheng","准备发送");

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("fengzheng",response.body().string());
                    }
                });
            }else{
                // 文件不存在
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){}
}
