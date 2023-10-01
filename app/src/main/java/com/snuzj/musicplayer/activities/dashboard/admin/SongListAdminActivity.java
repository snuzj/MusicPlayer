package com.snuzj.musicplayer.activities.dashboard.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.adapters.AdapterSongAdmin;
import com.snuzj.musicplayer.databinding.ActivitySongListAdminBinding;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class SongListAdminActivity extends AppCompatActivity {

    ActivitySongListAdminBinding binding;

    private String categoryId,category = "";
    ArrayList<ModelSong> songArrayList;
    AdapterSongAdmin adapterSongAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        category = intent.getStringExtra("categoryTitle");

        loadSongList();

        // Set layout manager here
        LinearLayoutManager layoutManager = new LinearLayoutManager(SongListAdminActivity.this);
        binding.songRv.setLayoutManager(layoutManager);


        binding.titleTv.setText(category);

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterSongAdmin.getFilter().filter(charSequence);
                } catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



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

                        adapterSongAdmin = new AdapterSongAdmin(SongListAdminActivity.this, songArrayList);
                        binding.songRv.setAdapter(adapterSongAdmin);
                        adapterSongAdmin.notifyDataSetChanged();  // Notify outside the loop

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}