package com.android.ailao;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ailao.data.MyRecord;
import com.android.ailao.data.MyVoiceData;
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.voice.Timer;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class RecordingActivity extends AppCompatActivity {

    private Context mContext;

    private Recorder recorder;

    private ImageView rejectBut;
    private ImageView acceptBut;
    private ImageView startAndStopBut;
    private ImageView reStartBut;

    private Chronometer timerShow;
    private TextView status;
    private TextView reStartInstructions;

    private boolean isStart;
    private boolean isPause;

    private Timer timer;

    private String voiceFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        mContext = RecordingActivity.this;

        rejectBut = findViewById(R.id.voice_reject);
        acceptBut = findViewById(R.id.voice_accept);
        startAndStopBut = findViewById(R.id.voice_microPhone);
        reStartBut = findViewById(R.id.voice_reStart);

        timerShow = findViewById(R.id.voice_time);
        status = findViewById(R.id.voice_status);
        reStartInstructions = findViewById(R.id.voice_reStart_instructions);

        //是否开始录音
        isStart = false;

        //是否暂停
        isPause = false;

        status.setText("");

        setupRecorder();
    }

    @Override
    protected void onStart(){
        super.onStart();

        timer = new Timer(timerShow);

        if(recorder != null){
            ButClickListener clickListener = new ButClickListener();

            startAndStopBut.setOnClickListener(clickListener);
            acceptBut.setOnClickListener(clickListener);
            rejectBut.setOnClickListener(clickListener);
            reStartBut.setOnClickListener(clickListener);
        }else{
            Toast.makeText(mContext,"不能创建音频文件，请检查存储权限",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击事件监听
     */
    private class ButClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            /**
             * 先检查权限
             */
            String[] recordPermission = {Manifest.permission.RECORD_AUDIO};
            CheckPermissions checkPermissions = new CheckPermissions(RecordingActivity.this,mContext);
            /**
             * 如果有录音权限
             */
            if(checkPermissions.checkPermission(recordPermission)){
                /**
                 * 检查存储权限
                 */
                if(isStoreAble()){
                    switch (v.getId()){
                        /**
                         * 麦克风图标点击事件
                         */
                        case R.id.voice_microPhone:{
                            start$pause$Record();
                            //显示重新开始的视图控件
                            reStartBut.setVisibility(View.VISIBLE);
                            reStartInstructions.setVisibility(View.VISIBLE);
                            break;
                        }
                        /**
                         * 接受图标点击事件
                         */
                        case R.id.voice_accept:{
                            stop$Record();
                            break;
                        }
                        /**
                         * 放弃图标点击事件
                         */
                        case R.id.voice_reject:{
                            giveUp$Exit();
                            break;
                        }
                        /**
                         * 重新录音按钮点击监听
                         */
                        case R.id.voice_reStart:{
                            again$Record();
                            break;
                        }
                    }
                }
            }else{
                int recordRequestCode = 702;
                String recordRational = "没有录音权限将不能打开麦克风录音，现在去打开麦克风权限？";
                checkPermissions.requestPermissions(recordRational,recordRequestCode,recordPermission);
            }
        }
    }

    /**
     * 开始、暂停录音
     */
    private void start$pause$Record(){
        //已经开始录音
        if(isStart){
            //处于暂停状态，则恢复录音
            if(isPause){
                recorder.resumeRecording();
                isPause = false;
                status.setText("正在录音...");

                if(timer != null){
                    timer.timerResume();
                }
            }
            //没有处于暂停状态，则暂停录音
            else{
                recorder.pauseRecording();
                isPause = true;

                if(timer != null){
                    timer.timerPause();
                }

                //异步更新UI
                startAndStopBut.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("已暂停");
                        animateVoice(0);
                    }
                },100);
            }
        }
        //尚未开始录音，则打开录音
        else{
            recorder.startRecording();
            isStart = true;
            isPause = false;

            status.setText("正在录音...");

            if(timer != null){
                timer.timerStart();
            }
        }
    }

    /**
     * 停止录音
     */
    private void stop$Record(){
        try{
            recorder.stopRecording();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(timer != null){
            timer.timerStop();
        }
        //异步更新UI
        acceptBut.post(new Runnable() {
            @Override
            public void run() {
                animateVoice(0);
                status.setText("");
            }
        });
        /**
         * 文件名存数据库，Activity返回
         */
        Intent intent = new Intent();
        if(voiceFileName != null){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    fileStoreDB(voiceFileName);
                }
            }.start();
            RecordingActivity.this.setResult(RESULT_OK,intent);
        }else{
            RecordingActivity.this.setResult(RESULT_CANCELED,intent);
        }
        RecordingActivity.this.finish();
    }

    /**
     * 文件名存数据库
     */
    private void fileStoreDB(String name){
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
         * 向MyVoice表中增加一条记录
         */
        if(recordId != 0){
            MyVoiceData voiceData = new MyVoiceData();
            voiceData.setRecordId(recordId);
            voiceData.setVoiceName(name);

            voiceData.save();
        }
    }

    /**
     * 重新开始录音
     */
    private void again$Record(){
        if(isStart){
            try {
                recorder.stopRecording();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if(voiceFileName != null){
            delete$voiceFile(voiceFileName);
        }

        //重新开始
        start$pause$Record();
    }

    /**
     * 放弃录音并退出
     */
    private void giveUp$Exit(){
        //如果已经开始录音，则停止录音
        if(isStart){
            try{
                recorder.stopRecording();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //删除录音文件
        if(voiceFileName != null){
            delete$voiceFile(voiceFileName);
        }

        Intent intent = new Intent();
        RecordingActivity.this.setResult(RESULT_CANCELED,intent);
        RecordingActivity.this.finish();
    }

    /**
     * 删除录音文件
     */
    private void delete$voiceFile(String name){
        String voicePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Voice" +File.separator;
        File file = new File(voicePath,name);
        if(file.exists()){
            file.delete();
        }
    }


    /**
     * 录音前初始化
     */
    private void setupRecorder() {
        File voiceFile = file();
        if(voiceFile != null){
            Log.e("fz","voiceFile不为空");
            recorder = OmRecorder.wav(
                    new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                        @Override
                        public void onAudioChunkPulled(AudioChunk audioChunk) {
                            animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                        }
                    }), voiceFile);
        }else{
            Toast.makeText(mContext,"不能创建存储音频的文件夹，请检查是否授予存储权限",Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * 配置音频格式
     */
    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    /**
     * 配置文件存储路径
     */
    private File file() {
        File voiceFile = null;

        /**
         * 创建保存音频的文件夹
         */
        boolean mkDirSuccess = true;
        String voicePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Voice" +File.separator;
        File folder = new File(voicePath);
        //文件夹不存在，新建文件夹
        if(!folder.exists()){
            mkDirSuccess = folder.mkdirs();
        }

        //新建文件夹成功，创建保存音频的文件
        if(mkDirSuccess){
            voiceFileName = System.currentTimeMillis()+".wav";
            voiceFile = new File(voicePath, voiceFileName);
        }

        return voiceFile;
    }

    /**
     * microPhone图片动态变化
     */
    private void animateVoice(final float maxPeak) {
        if(startAndStopBut != null){
            startAndStopBut.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak)
                    .setDuration(10).start();
        }
    }


    /**
     * 检查存储权限
     */
    private boolean isStoreAble(){
        boolean isAble = false;

        String[] storeAndReadPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        CheckPermissions checkPermissions = new CheckPermissions(RecordingActivity.this,mContext);
        if(checkPermissions.checkPermission(storeAndReadPermissions)){
            isAble = true;
        }else{
            int storeRequestCode = 701;
            String storeRational = "没有存储权限不能保存音频文件，现在去授予存储权限？";
            checkPermissions.requestPermissions(storeRational,storeRequestCode,storeAndReadPermissions);
        }

        return isAble;
    }
}
