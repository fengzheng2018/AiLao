package com.android.ailao.picture;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.ailao.R;

import java.util.List;

public class PictureRecyclerView extends RecyclerView.Adapter<PictureRecyclerView.ViewHolder> implements View.OnClickListener {

    private List<PictureItem> pictureItemList;

    public PictureRecyclerView(List<PictureItem> pictureItemList) {
        this.pictureItemList = pictureItemList;
    }

    /**
     * 声明点击接口变量
     */
    private OnItemClickListener mOnItemClickListener = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.collection_info_pictures,viewGroup,false);

        ViewHolder viewHolder = new ViewHolder(view);

        /**
         * 添加点击事件
         */
        viewHolder.mainPicture.setOnClickListener(this);
        viewHolder.cancelPicture.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        PictureItem pictureItem = pictureItemList.get(i);

        Bitmap bitmap = pictureItem.getBitmap();

        viewHolder.mainPicture.setImageBitmap(bitmap);

        /**
         * 已经显示到最后一张图片，开始显示添加图片按钮
         */
        if( (i == pictureItemList.size()-1) || (pictureItemList.size() == 0)){
            viewHolder.addPicture.setVisibility(View.VISIBLE);
        }

        /**
         * 将各个Item的position进行保存
         */
        viewHolder.mainPicture.setTag(i);
        viewHolder.cancelPicture.setTag(i);
    }

    @Override
    public int getItemCount() {
        return pictureItemList.size();
    }

    /**
     * 点击事件转给外面的观察者
     */
    @Override
    public void onClick(View v) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    /**
     * 静态内部类ViewHolder，从布局中获取元素
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mainPicture;
        ImageView cancelPicture;
        ImageView addPicture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mainPicture = itemView.findViewById(R.id.collection_info_picture_show);
            cancelPicture = itemView.findViewById(R.id.collection_info_cancel_img);
            addPicture = itemView.findViewById(R.id.collection_info_addPicture);
        }
    }

    /**
     * 声明一个点击接口
     */
    public static interface OnItemClickListener{
        void onItemClick(View view,int position);
    }
}
