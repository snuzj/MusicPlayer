package com.snuzj.musicplayer.activities.dashboard.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.databinding.ActivityEditMusicBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class EditMusicActivity extends AppCompatActivity {

    ActivityEditMusicBinding binding;

    FirebaseAuth firebaseAuth;

    ProgressDialog progressDialog;

    private String songId;
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private String selectedCategoryId,selectedCategoryTitle = "";
    private String TAG = "EDIT_MUSIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        songId = getIntent().getStringExtra("id");
        loadCategories();
        loadSongInfo();

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.categoryTv.setOnClickListener(view -> categoryDialog());

        binding.updateBtn.setOnClickListener(view -> validateData());



    }

    private String title,album,artist, duration = "";
    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        album = binding.albumEt.getText().toString().trim();
        artist = binding.artistEt.getText().toString().trim();
        duration = binding.durationEt.getText().toString().trim();

        if(title.isEmpty()){
            Toast.makeText(this,"title is empty",Toast.LENGTH_SHORT).show();
        } else if (album.isEmpty()){
            Toast.makeText(this,"album is empty",Toast.LENGTH_SHORT).show();
        } else if(artist.isEmpty()){
            Toast.makeText(this,"artist is empty",Toast.LENGTH_SHORT).show();
        } else if (duration.isEmpty()) {
            Toast.makeText(this,"duration is empty", Toast.LENGTH_SHORT).show();
        } else{
            updateSong();
        }

    }

    private void updateSong() {
        Log.d(TAG, "updateSong: Starting updating pdf");

        progressDialog.setMessage("Updating song info.");
        progressDialog.show();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("album",""+album);
        hashMap.put("artist",""+artist);
        hashMap.put("duration",""+duration);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Songs");
        ref.child(songId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditMusicActivity.this,"Updated successfully.",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e->{
                        progressDialog.dismiss();
                        Toast.makeText(EditMusicActivity.this,"Update failed due to"+e.getMessage(),Toast.LENGTH_SHORT).show();
                });


    }

    private void loadSongInfo() {
        Log.d(TAG, "loadSongInfo: Loading song info");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Songs");
        reference.child(songId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        String album = ""+snapshot.child("album").getValue();
                        String artist = ""+snapshot.child("artist").getValue();
                        String duration = ""+snapshot.child("duration").getValue();

                        binding.titleEt.setText(title);
                        binding.albumEt.setText(album);
                        binding.artistEt.setText(artist);
                        binding.durationEt.setText(duration);

                        DatabaseReference referenceCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        referenceCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = ""+snapshot.child("category").getValue();
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void categoryDialog(){
        //make string array from arraylist
        String[] categoryArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoryArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category")
                .setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                    }
                }).show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID" +id);
                    Log.d(TAG, "onDataChange: Category"+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}