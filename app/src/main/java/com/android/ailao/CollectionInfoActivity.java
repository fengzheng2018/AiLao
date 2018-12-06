package com.android.ailao;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import com.android.ailao.permissions.CheckPermissions;
import com.android.ailao.picture.PictureItem;
import com.android.ailao.picture.PictureRecyclerView;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class CollectionInfoActivity extends AppCompatActivity {

    private ImageView goBackImg;
    private TextView goBackTxt;
    private TextView submitTxt;

    private EditText mEditText;

    private Context mContext;

    private RecyclerView picRecycler;

    /**
     * 点击图片时显示的大图
     */
    private ImageView expendImage;

    /**
     * 装载图片的List
     */
    private List<PictureItem> pictureItems;

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
}
