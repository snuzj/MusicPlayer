package com.snuzj.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.PlayMusicActivity;
import com.snuzj.musicplayer.databinding.SongFavItemBinding;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class AdapterSongFav extends RecyclerView.Adapter<AdapterSongFav.HolderSongFav> {

    private Context context;
    private ArrayList<ModelSong> songArrayList;
    SongFavItemBinding binding;

    private String TAG = "FAV_SONG_TAG";

    public AdapterSongFav(Context context, ArrayList<ModelSong> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
    }

    @NonNull
    @Override
    public HolderSongFav onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = SongFavItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderSongFav(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSongFav holder, int position) {
        ModelSong modelSong = songArrayList.get(position);
        String id = modelSong.getId();
        String title = modelSong.getTitle();
        String album = modelSong.getAlbum();
        String artist = modelSong.getArtist();
        String duration = modelSong.getDuration();
        String imgUrl = modelSong.getImageUrl();
        String audioUrl = modelSong.getAudioUrl();

        loadSongDetails(modelSong,holder);
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

    private void loadSongDetails(ModelSong modelSong, HolderSongFav holder) {
        String songId = modelSong.getId();
        Log.d(TAG, "loadSongDetails: GET SONG ID: "+songId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Songs");
        reference.child(songId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = ""+snapshot.child("title").getValue();
                        String artist = ""+snapshot.child("artist").getValue();
                        String imgUrl = ""+snapshot.child("imageUrl").getValue();

                        holder.titleTv.setText(title);
                        holder.artistTv.setText(artist);
                        Glide.with(context)
                                .load(imgUrl)
                                .placeholder(R.drawable.ic_song)
                                .into(holder.songIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }



    class HolderSongFav extends RecyclerView.ViewHolder {

        TextView titleTv, artistTv;
        ImageView songIv;
        public HolderSongFav(@NonNull View itemView) {
            super(itemView);
            titleTv = binding.titleTv;
            artistTv = binding.artistTv;
            songIv = binding.songIv;
        }
    }


}
