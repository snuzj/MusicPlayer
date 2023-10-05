package com.snuzj.musicplayer.activities.dashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.MyApplication;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.databinding.ActivityPlayMusicBinding;
import com.snuzj.musicplayer.models.ModelSong;
import java.io.IOException;
import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityPlayMusicBinding binding;
    private String songId;
    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private final String TAG = "PLAY_MUSIC";
    private MediaPlayer mediaPlayer;
    boolean isPlaying = false;

    private String categoryId = "";
    private ArrayList<ModelSong> songArrayList;

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(this);
        binding.playBtn.setOnClickListener(this);
        binding.previousBtn.setOnClickListener(this);
        binding.nextBtn.setOnClickListener(this);
        binding.downloadBtn.setOnClickListener(this);
        binding.favorteBtn.setOnClickListener(this);

        songId = getIntent().getStringExtra("id");

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        loadSongInfo();
        loadPlaylist();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> onCompletion());
        mediaPlayer.setOnPreparedListener(mp -> {
            progressDialog.dismiss();
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            onBackPressed();
        } else if (view.getId() == R.id.playBtn) {
            playPauseSong();
        } else if (view.getId() == R.id.previousBtn) {
            playPreviousSong();
        } else if (view.getId() == R.id.nextBtn) {
            playNextSong();
        }else if (view.getId() == R.id.downloadBtn) {
            downloadSong();
        } else if (view.getId() == R.id.favorteBtn) {
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(this, "Please relogin", Toast.LENGTH_SHORT).show();
            }
            else{
                if(isFavorite){
                    MyApplication.removeFromFavorite(PlayMusicActivity.this,songId);
                } else{
                    MyApplication.addToFavorite(PlayMusicActivity.this,songId);
                }
            }
        }
    }

    private void checkIsFavorite(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){

        } else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(songId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            isFavorite = snapshot.exists();
                            if (isFavorite){
                                binding.favorteBtn.setImageResource(R.drawable.ic_fav_fill);
                            } else {
                                binding.favorteBtn.setImageResource(R.drawable.ic_fav_border);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void downloadSong() {
        if (songId != null) {
            String title = binding.songTv.getText().toString();
            String audioUrl = songArrayList.get(getCurrentSongIndex()).getAudioUrl();

            // You can use the MyApplication class or handle the download logic here directly
            // For simplicity, let's use MyApplication
            MyApplication.downloadAndSaveSong(this, songId, title, audioUrl);
        }
    }
    private void updatePlayButton() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                binding.playBtn.setImageResource(R.drawable.ic_pause);
            } else {
                binding.playBtn.setImageResource(R.drawable.ic_play);
            }
        }
    }

    private void playPauseSong() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                binding.playBtn.setImageResource(R.drawable.ic_pause);
                pauseSong();
            } else {
                binding.playBtn.setImageResource(R.drawable.ic_play);
                playSong();
            }
        }
    }

    private void playSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            updatePlayButton();
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayButton();
        }
    }

    private void onCompletion() {
        isPlaying = false;
        updatePlayButton();
    }


    private void playNextSong() {
        stopAndReleaseMediaPlayer();
        if (songArrayList != null && !songArrayList.isEmpty()) {
            int currentIndex = getCurrentSongIndex();
            int nextIndex = (currentIndex + 1) % songArrayList.size();
            loadAndPlaySong(nextIndex);
        }
    }

    private void playPreviousSong() {
        stopAndReleaseMediaPlayer();
        if (songArrayList != null && !songArrayList.isEmpty()) {
            int currentIndex = getCurrentSongIndex();
            int previousIndex = (currentIndex - 1 + songArrayList.size()) % songArrayList.size();
            loadAndPlaySong(previousIndex);
        }
    }

    private void stopAndReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private void loadAndPlaySong(int index) {
        if (index >= 0 && index < songArrayList.size()) {
            ModelSong nextSong = songArrayList.get(index);
            songId = nextSong.getId();
            loadSongInfo();
            initializeMediaPlayer(nextSong.getAudioUrl());
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private void initializeMediaPlayer(String audioUrl) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(mp -> onCompletion());
                mediaPlayer.setOnPreparedListener(mp -> {
                    progressDialog.dismiss();
                });
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            progressDialog.show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Songs");
        reference.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    ModelSong song = snapshot.getValue(ModelSong.class);
                    if (song != null) {
                        String title = song.getTitle();
                        String artist = song.getArtist();
                        String duration = song.getDuration();
                        String imgUrl = song.getImageUrl();
                        String audioUrl = song.getAudioUrl();

                        binding.songTv.setText(title);
                        binding.artistTv.setText(artist);
                        binding.durationTv.setText(duration);

                        Glide.with(PlayMusicActivity.this)
                                .load(imgUrl)
                                .placeholder(R.drawable.ic_song)
                                .into(binding.songIv);

                        initializeMediaPlayer(audioUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error loading song info: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void loadPlaylist() {
        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference("Songs");
        songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    categoryId = String.valueOf(dataSnapshot.child("categoryId").getValue());
                    loadSongsInCategory(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void loadSongsInCategory(String categoryId) {
        DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference("Songs");
        playlistRef.orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    songArrayList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ModelSong song = snapshot.getValue(ModelSong.class);
                        songArrayList.add(song);
                    }
                    loadAndPlaySong(getCurrentSongIndex());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private int getCurrentSongIndex() {
        if (songArrayList != null && !songArrayList.isEmpty()) {
            for (int i = 0; i < songArrayList.size(); i++) {
                if (songArrayList.get(i).getId().equals(songId)) {
                    return i;
                }
            }
        }
        return -1;}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndReleaseMediaPlayer();
    }
}
