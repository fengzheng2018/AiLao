package com.android.ailao.voice;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ailao.R;

import java.util.List;

public class VoiceRecyclerView extends RecyclerView.Adapter<VoiceRecyclerView.ViewHolder>
                               implements View.OnClickListener,View.OnLongClickListener{

    private List<VoiceItem> voiceItemList;
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener = null;

    public VoiceRecyclerView(List<VoiceItem> voiceItemList) {
        this.voiceItemList = voiceItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.collection_info_voice,viewGroup,false);

        ViewHolder viewHolder = new ViewHolder(view);

        /**
         * 添加点击事件
         */
        viewHolder.imageView.setOnClickListener(this);

        /**
         * 添加长按事件
         */
        viewHolder.imageView.setOnLongClickListener(this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        VoiceItem voiceItem = voiceItemList.get(i);

        viewHolder.textView.setText(voiceItem.getVoiceTime());

        /**
         * 保存各个控件的位置
         */
        viewHolder.imageView.setTag(i);
    }

    @Override
    public int getItemCount() {
        return voiceItemList.size();
    }

    /**
     * 内部类ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.voice_show_img);
            textView = itemView.findViewById(R.id.voice_show_text);
        }
    }

    /**
     * 声明一个点击接口
     */
    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if(onItemClickListener != null){
            onItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    /**
     * 声明一个长按接口
     */
    public interface OnItemLongClickListener{
        void OnItemLongClickListener(View view,int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.onItemLongClickListener = listener;
    }

    @Override
    public boolean onLongClick(View view) {
        if(onItemLongClickListener != null){
            onItemLongClickListener.OnItemLongClickListener(view, (int)view.getTag());
            return true;
        }else{
            return false;
        }
    }
}
