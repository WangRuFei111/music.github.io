package com.example.musicplayer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.bean.MusicPlayerBean;

import java.util.List;

public class MusicPlayerAdapter extends RecyclerView.Adapter<MusicPlayerAdapter.MusicPlayerViewHolder> {

    Context context;
    List<MusicPlayerBean> mDatas;
    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }

    public MusicPlayerAdapter(Context context, List<MusicPlayerBean> datas) {
        this.context = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public MusicPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_player,parent,false);
        MusicPlayerViewHolder holder = new MusicPlayerViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicPlayerViewHolder holder, final int position) {
        MusicPlayerBean musicBean = mDatas.get(position);
        holder.idTv.setText(musicBean.getId());
        holder.songTv.setText(musicBean.getSong());
        holder.singerTv.setText(musicBean.getSinger());
        holder.albumTv.setText(musicBean.getAlbum());
        holder.timeTv.setText(musicBean.getDuration());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.OnItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MusicPlayerViewHolder extends RecyclerView.ViewHolder {
        TextView idTv,songTv,singerTv,albumTv,timeTv;
        public MusicPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            idTv = itemView.findViewById(R.id.item_music_player_num);
            songTv = itemView.findViewById(R.id.item_music_player_song);
            singerTv = itemView.findViewById(R.id.item_music_player_singer);
            albumTv = itemView.findViewById(R.id.item_music_player_album);
            timeTv = itemView.findViewById(R.id.item_music_player_durtion);
        }
    }
}
