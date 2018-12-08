package com.android.ailao;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ailao.data.MyPicture;
import com.android.ailao.data.MyRecord;
import com.android.ailao.data.MyVoiceData;
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.picture.PictureItem;
import com.android.ailao.picture.PictureRecyclerView;
import com.android.ailao.voice.VoiceItem;
import com.android.ailao.voice.VoiceRecyclerView;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CollectionInfoActivity extends AppCompatActivity {

    private ImageView goBackImg;
    private TextView goBackTxt;
    private TextView submitTxt;

    private EditText mEditText;

    private Context mContext;

    private RecyclerView picRecycler;
    private RecyclerView voiceRecycler;

    /**
     * 点击图片时显示的大图
     */
    private ImageView expendImage;

    /**
     * 装载图片的List
     */
    private List<PictureItem> pictureItems;

    /**
     * 装载声音的list
     */
    private List<VoiceItem> voiceItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_info);

        Toolbar toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        goBackImg = findViewById(R.id.helper_goback_img);
        goBackTxt = findViewById(R.id.helper_goback_txt);
        submitTxt = findViewById(R.id.helper_submit_txt);

        mEditText = findViewById(R.id.collection_info_inputTXT);

        expendImage = findViewById(R.id.collection_info_expendPic);

        mContext = CollectionInfoActivity.this;

        picRecycler = findViewById(R.id.recycleView_picture);
        voiceRecycler = findViewById(R.id.collection_info_voice_view);
    }

    @Override
    protected void onStart(){
        super.onStart();

        HelperClickListener helperClickListener = new HelperClickListener();

        goBackImg.setOnClickListener(helperClickListener);
        goBackTxt.setOnClickListener(helperClickListener);
        submitTxt.setOnClickListener(helperClickListener);

        /**
         * 查找图片，若图片存在，显示图片
         */
        queryPictureFromDB();
        /**
         * 查找音频文件
         */
        queryVoiceFromDB();
    }

    /**
     * toolBar各图标点击事件监听
     */
    private class HelperClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.helper_goback_img:{
                    finish();
                    break;
                }
                case R.id.helper_goback_txt:{
                    finish();
                    break;
                }
                case R.id.helper_submit_txt:{
                    Toast.makeText(mContext,"点击了提交",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    /**
     * 从数据库中查找是否存在图片
     */
    private void queryPictureFromDB(){
        /**
         * 查找未完成的记录
         */
        List<MyRecord> myRecords = LitePal.where("isOver=?","1").find(MyRecord.class);
        if(myRecords.size() > 0){
            long recordId = myRecords.get(0).getRecordId();
            LitePal.where("recordId=?",""+recordId)
                    .order("id")
                    .findAsync(com.android.ailao.data.MyPicture.class)
                    .listen(new FindMultiCallback<com.android.ailao.data.MyPicture>(){
                        @Override
                        public void onFinish(List<com.android.ailao.data.MyPicture> list) {
                            loadPictureFromLocal(list);
                        }
                    });
        }
    }

    /**
     * 根据从数据库中找出的文件名称从本地加载图片
     */
    private void loadPictureFromLocal(List<com.android.ailao.data.MyPicture> list){
        /**
         * 数据库中存在记录
         */
        if(list.size() > 0){
            pictureItems = new ArrayList<>();

            ImageView cancelImg = findViewById(R.id.collection_info_cancel_img);
            /**
             * 图片存储路径
             */
            String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator +"AiLaoShan"+ File.separator + "Picture" +File.separator;

            /**
             * 检查权限
             */
            String[] readPermission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            CheckPermissions checkPermissions = new CheckPermissions(CollectionInfoActivity.this,mContext);
            if(checkPermissions.checkPermission(readPermission)){
                for(int i=0; i<list.size(); i++){
                    try{
                        FileInputStream fileInputStream = new FileInputStream(picPath+list.get(i).getPicName());
                        /**
                         * 以最省内存的方式加载
                         */
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inPurgeable = true;
                        options.inInputShareable = true;

                        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream,null,options);
                        PictureItem pictureItem = new PictureItem(bitmap,cancelImg,list.get(i).getPicName());
                        pictureItems.add(pictureItem);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                showPictureFromDB(pictureItems);
            }
            /**
             * 不存在权限，去请求权限
             */
            else {
                int questPermissionCode = 161;
                String rational = "没有读取内容的权限，不能显示照片，现在去设置权限？";
                checkPermissions.requestPermissions(rational,questPermissionCode,readPermission);
            }
        }
    }


    /**
     * 展示已保存的图片
     */
    private void showPictureFromDB(final List<PictureItem> pictureItems){
        if(picRecycler != null){
            /**
             * 设置布局为横向布局
             */
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            picRecycler.setLayoutManager(layoutManager);

            PictureRecyclerView pictureRecyclerView = new PictureRecyclerView(pictureItems);
            picRecycler.setAdapter(pictureRecyclerView);

            /**
             * Item点击事件监听
             */
            pictureRecyclerView.setOnItemClickListener(new PictureRecyclerView.OnItemClickListener() {
                @Override
                public void onItemClick(final View view, int position) {

                    /**
                     * 查看软键盘是否已经打开
                     */
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        if(imm.isActive()){
                            imm.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
                        }
                    }

                    switch (view.getId()){
                        /**
                         * 若点击的是图片
                         */
                        case R.id.collection_info_picture_show:{
                            /**
                             * 从List中查看图片是否存在
                             */
                            if(pictureItems != null && pictureItems.get(position).getBitmap() != null){
                                /**
                                 * 查看外层显示大图的ImageView是否存在
                                 */
                                if(expendImage != null){
                                    expendImage.setImageBitmap(pictureItems.get(position).getBitmap());
                                    expendImage.setVisibility(View.VISIBLE);
                                    /**
                                     * 设置点击事件，点击时隐藏此视图
                                     */
                                    expendImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            expendImage.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                            break;
                        }
                        /**
                         * 若点击的是删除图片，则从数据库中删除记录，并重新刷新RecyclerView
                         */
                        case R.id.collection_info_cancel_img:{
                            /**
                             * pictureItems这个List是否为空
                             */
                            if(pictureItems != null && pictureItems.get(position) != null){
                                /**
                                 * 从数据库删除
                                 */
                                LitePal.deleteAll(MyPicture.class,"picName=?",""+pictureItems.get(position).getPicName());
                                Log.i("fz",pictureItems.get(position).getPicName());

                                /**
                                 * 刷新RecyclerView
                                 */
                                pictureItems.remove(position);
                                showPictureFromDB(pictureItems);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * 从数据库中查找存在的录音文件
     */
    private void queryVoiceFromDB(){
        /**
         * 查找未完成的记录
         */
        List<MyRecord> myRecords = LitePal.where("isOver=?","1").find(MyRecord.class);
        if(myRecords.size() > 0) {
            long recordId = myRecords.get(0).getRecordId();
            LitePal.where("recordId=?", "" + recordId)
                    .order("id")
                    .findAsync(MyVoiceData.class)
                    .listen(new FindMultiCallback<MyVoiceData>() {
                        @Override
                        public void onFinish(List<MyVoiceData> list) {
                            /**
                             * 从本地查找录音文件
                             */
                            loadVoiceFromLocal(list);
                        }
                    });
        }
    }


    /**
     * 显示录音文件
     */
    private void loadVoiceFromLocal(List<MyVoiceData> list){
        voiceItemList = new ArrayList<>();

        String voicePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator +"AiLaoShan"+ File.separator + "Voice" +File.separator;

        /**
         * 循环遍历，获取uri和时间
         */
        for(int i=0; i<list.size(); i++){
            String filePath = voicePath + list.get(i).getVoiceName();

            long time = 0;
            if(filePath != null){
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();

                    time = mediaPlayer.getDuration();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            /**
             * long型时间转为时分秒格式
             */
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            String hms = dateFormat.format(time);

            VoiceItem voiceItem = new VoiceItem(filePath,hms);

            voiceItemList.add(voiceItem);
        }

        /**
         * 用RecyclerView显示文件
         */
        showVoiceInRecycler(voiceItemList);
    }

    /**
     * 在RecyclerView中显示音频文件
     */
    private void showVoiceInRecycler(List<VoiceItem> list){
        if(voiceRecycler != null){
            /**
             * 设置布局为横向布局
             */
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            voiceRecycler.setLayoutManager(layoutManager);

            VoiceRecyclerView voiceRecyclerView = new VoiceRecyclerView(list);
            voiceRecycler.setAdapter(voiceRecyclerView);

            //固定Item大小
            voiceRecycler.setHasFixedSize(true);

            /**
             * 添加事件点击监听
             */
            voiceRecyclerView.setOnItemClickListener(new VoiceRecyclerView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if(voiceItemList != null && voiceItemList.get(position) != null){
                        File file = new File(voiceItemList.get(position).getVoiceUri());

                        if(file != null){
                            /**
                             * 构造打开音频的Intent
                             */
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("oneshot",0);
                            intent.putExtra("configchange",0);
                            intent.setDataAndType(getVoiceFileUri(intent,file),"audio/x-wav");

                            startActivity(intent);
                        }else{
                            Toast.makeText(mContext,"打开文件出错，文件不存在或被删除",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            /**
             * 长按事件监听
             */
            voiceRecyclerView.setOnItemLongClickListener(new VoiceRecyclerView.OnItemLongClickListener() {
                @Override
                public void OnItemLongClickListener(View view, int position) {
                    voiceDeleteFile(position);
                }
            });
        }
    }


    /**
     * 为适配7及以上系统，生成Uri
     */
    private Uri getVoiceFileUri(Intent intent,File file){
        Uri uri = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            uri = FileProvider.getUriForFile(mContext,
                    mContext.getPackageName()+".provider",file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


    /**
     * 音频文件长按弹出对话框提示删除
     */
    private void voiceDeleteFile(int i){
        if(voiceItemList != null && voiceItemList.get(i) != null){
            /**
             * 字符串处理，找出文件名、文件路径
             */
            final int position = i;
            final String fileUri = voiceItemList.get(i).getVoiceUri();
            int mDivide = fileUri.lastIndexOf(File.separator);
            final String mFileName = fileUri.substring(mDivide+1);

            AlertDialog.Builder voiceBuilder = new AlertDialog.Builder(mContext);
            voiceBuilder.setTitle(mFileName);
            voiceBuilder.setMessage(R.string.voice_delete_message);
            voiceBuilder.setPositiveButton(R.string.voice_delete_okBut, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /**
                     * 删除数据库中记录
                     */
                    LitePal.deleteAll(MyVoiceData.class,"voiceName = ?",""+mFileName);
                    /**
                     * 删除本地文件
                     */
                    File file = new File(fileUri);
                    if(file.exists()){
                        file.delete();
                    }
                    /**
                     * 从voiceItemList中移除
                     */
                    voiceItemList.remove(position);
                    /**
                     * 刷新voiceItemList
                     */
                    showVoiceInRecycler(voiceItemList);
                }
            });
            voiceBuilder.setNegativeButton(R.string.voice_delete_cancelBut, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
            });
            voiceBuilder.create().show();
        }
    }
}
