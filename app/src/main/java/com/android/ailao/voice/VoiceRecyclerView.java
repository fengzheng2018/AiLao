package com.android.ailao.voice;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ailao.R;

import java.util.List;

public class VoiceRecyclerView extends RecyclerView.Adapter<VoiceRecyclerView.ViewHolder> {

    private List<VoiceItem> voiceItemList;

    public VoiceRecyclerView(List<VoiceItem> voiceItemList) {
        this.voiceItemList = voiceItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.collection_info_voice,viewGroup,false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        VoiceItem voiceItem = voiceItemList.get(i);

        Log.e("fz",voiceItem.getVoiceTime());

        viewHolder.textView.setText(voiceItem.getVoiceTime());
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
}
