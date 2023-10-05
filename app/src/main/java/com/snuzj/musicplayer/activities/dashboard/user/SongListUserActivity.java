package com.snuzj.musicplayer.activities.dashboard.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.admin.SongListAdminActivity;
import com.snuzj.musicplayer.adapters.AdapterSongAdmin;
import com.snuzj.musicplayer.adapters.AdapterSongUser;
import com.snuzj.musicplayer.databinding.ActivitySongListUserBinding;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class SongListUserActivity extends AppCompatActivity {

    ActivitySongListUserBinding binding;
    FirebaseAuth firebaseAuth;

    private String categoryId,category = "";
    ArrayList<ModelSong> songArrayList;
    AdapterSongUser adapterSongUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        category = intent.getStringExtra("categoryTitle");

        loadSongList();

        binding.categoryTv.setText(category);
        binding.backBtn.setOnClickListener(view -> onBackPressed());

        // Set layout manager here
        LinearLayoutManager layoutManager = new LinearLayoutManager(SongListUserActivity.this);
        binding.songRv.setLayoutManager(layoutManager);
    }

    private void loadSongList() {
        songArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Songs");
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelSong model = ds.getValue(ModelSong.class);
                            songArrayList.add(model);
                        }

                        adapterSongUser = new AdapterSongUser(SongListUserActivity.this, songArrayList);
                        binding.songRv.setAdapter(adapterSongUser);
                        adapterSongUser.notifyDataSetChanged();  // Notify outside the loop

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}