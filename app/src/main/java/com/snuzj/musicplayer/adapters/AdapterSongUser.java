package com.snuzj.musicplayer.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.PlayMusicActivity;
import com.snuzj.musicplayer.activities.dashboard.admin.SongListAdminActivity;
import com.snuzj.musicplayer.databinding.RowSongAdminBinding;
import com.snuzj.musicplayer.databinding.RowSongUserBinding;
import com.snuzj.musicplayer.filters.FilterSongAdmin;
import com.snuzj.musicplayer.filters.FilterSongUser;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class AdapterSongUser extends RecyclerView.Adapter<AdapterSongUser.HolderSongUser> implements Filterable {
    private ProgressDialog progressDialog;

    private FilterSongUser filterSongUser;

    private Context context;
    public ArrayList<ModelSong> songArrayList, filterList;

    private RowSongUserBinding binding;

    public AdapterSongUser(Context context, ArrayList<ModelSong> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
        this.filterList = songArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public Filter getFilter() {
        if (filterSongUser == null){
            filterSongUser = new FilterSongUser(filterList, this);
        }
        return filterSongUser;
    }

    @NonNull
    @Override
    public AdapterSongUser.HolderSongUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowSongUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderSongUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSongUser.HolderSongUser holder, int position) {
        ModelSong modelSong = songArrayList.get(position);
        String id = modelSong.getId();
        String title = modelSong.getTitle();
        String album = modelSong.getAlbum();
        String artist = modelSong.getArtist();
        String duration = modelSong.getDuration();
        String imgUrl = modelSong.getImageUrl();
        String audioUrl = modelSong.getAudioUrl();

        holder.titleTv.setText(title);
        holder.artistTv.setText(artist);
        holder.durationTv.setText(duration);

        try {
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_song)
                    .into(holder.songIv);
        }catch (Exception e){}

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PlayMusicActivity.class);
            intent.putExtra("id",id);
            intent.putExtra("title",title);
            intent.putExtra("album",album);
            intent.putExtra("artist",artist);
            intent.putExtra("audioUrl",audioUrl);
            intent.putExtra("imgUrl",imgUrl);
            intent.putExtra("duration",duration);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    class HolderSongUser extends RecyclerView.ViewHolder {
        ImageView songIv;
        TextView titleTv, artistTv, durationTv;

        public HolderSongUser(@NonNull View itemView) {
            super(itemView);
            songIv = binding.songIv;
            titleTv = binding.titleTv;
            artistTv = binding.artistTv;
            durationTv = binding.durationTv;
        }
    }
}
