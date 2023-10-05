package com.snuzj.musicplayer.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.PlayMusicActivity;
import com.snuzj.musicplayer.activities.dashboard.admin.EditMusicActivity;
import com.snuzj.musicplayer.databinding.RowSongAdminBinding;
import com.snuzj.musicplayer.filters.FilterSongAdmin;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class AdapterSongAdmin extends RecyclerView.Adapter<AdapterSongAdmin.HolderSongAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelSong> songArrayList, filterList;

    private RowSongAdminBinding binding;
    private FilterSongAdmin filterSongAdmin;

    private ProgressDialog progressDialog;


    public AdapterSongAdmin(Context context, ArrayList<ModelSong> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
        this.filterList = songArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderSongAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowSongAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderSongAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSongAdmin holder, int position) {
        ModelSong modelSong = songArrayList.get(position);
        String title = modelSong.getTitle();
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

        holder.moreBtn.setOnClickListener(view -> moreOptionsDialog(modelSong, holder));




    }

    private void moreOptionsDialog(ModelSong modelSong, HolderSongAdmin holder) {
        String title = modelSong.getTitle();
        String album = modelSong.getAlbum();
        String artist = modelSong.getArtist();
        String audioUrl = modelSong.getAudioUrl();
        String imgUrl = modelSong.getImageUrl();
        String duration = modelSong.getDuration();
        String id = modelSong.getId();

        String[] options = {"Play","Edit","Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            Intent intent = new Intent(context, PlayMusicActivity.class);
                            intent.putExtra("id",id);
                            intent.putExtra("title",title);
                            intent.putExtra("album",album);
                            intent.putExtra("artist",artist);
                            intent.putExtra("audioUrl",audioUrl);
                            intent.putExtra("imgUrl",imgUrl);
                            intent.putExtra("duration",duration);
                            context.startActivity(intent);


                        } else if (i==1) {
                            Intent intent = new Intent(context, EditMusicActivity.class);
                            intent.putExtra("id",id);
                            intent.putExtra("title",title);
                            intent.putExtra("album",album);
                            intent.putExtra("artist",artist);
                            intent.putExtra("audioUrl",audioUrl);
                            intent.putExtra("imgUrl",imgUrl);
                            intent.putExtra("duration",duration);
                            context.startActivity(intent);
                        } else if (i==2) {
                            //delete clicked
                            deleteSong(modelSong,holder);
                        } else {
                            Toast.makeText(context,"Cancelled",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void deleteSong(ModelSong modelSong, HolderSongAdmin holder) {
        String songId = modelSong.getId();
        String audioUrl = modelSong.getAudioUrl();
        String imageUrl = modelSong.getImageUrl();
        String title = modelSong.getTitle();

        progressDialog.setMessage("Deleting song...");
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete().addOnSuccessListener(unused2 -> {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(audioUrl);
            storageReference.delete()
                    .addOnSuccessListener(unused -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
                        databaseReference.child(songId)
                                .removeValue()
                                .addOnSuccessListener(unused1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(context,"Song deleted successfully.",Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e-> {
                        progressDialog.dismiss();
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e->{
            progressDialog.dismiss();
            Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        });


    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterSongAdmin == null){
            filterSongAdmin = new FilterSongAdmin(filterList, this);
        }
        return filterSongAdmin;
    }


    class  HolderSongAdmin extends RecyclerView.ViewHolder{

        ImageView songIv;
        TextView titleTv, artistTv, durationTv;

        ImageButton moreBtn;
        public HolderSongAdmin(@NonNull View itemView) {
            super(itemView);
            songIv = binding.songIv;
            titleTv = binding.titleTv;
            artistTv = binding.artistTv;
            durationTv = binding.durationTv;
            moreBtn = binding.moreBtn;
        }
    }
}
