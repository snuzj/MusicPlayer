package com.snuzj.musicplayer;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void downloadAndSaveSong(Context context, String id, String title, String audioUrl) {
        Log.d(TAG_DOWNLOAD, "downloadAndSaveSong: downloading and saving song...");
        String nameWithExtension = title + ".mp3";
        Log.d(TAG_DOWNLOAD, "downloadAndSaveSong: NAME:" + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Downloading " + nameWithExtension);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(audioUrl);
        storageReference.getBytes(10*1024 * 1024) // Set the maximum allowed size in bytes
                .addOnSuccessListener(bytes -> {
                    progressDialog.dismiss();
                    saveDownloadedAudio(context, bytes, title);
                })
                .addOnFailureListener(exception -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG_DOWNLOAD, "downloadAndSaveSong: Download failed", exception);
                });
    }

    public static void saveDownloadedAudio(Context context, byte[] audioData, String title) {
        try {
            // Save the downloaded data to a file
            File audioFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), title + ".mp3");
            FileOutputStream fos = new FileOutputStream(audioFile);
            fos.write(audioData);
            fos.close();

            Toast.makeText(context, "Audio saved: " + audioFile.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG_DOWNLOAD, "saveDownloadedAudio: Error saving downloaded audio", e);
            Toast.makeText(context, "Error saving downloaded audio", Toast.LENGTH_SHORT).show();
        }
    }

    public static void addToFavorite(Context context, String songId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context,"Failed to load your info, relogin",Toast.LENGTH_SHORT).show();
        }
        else{
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("songId",""+songId);
            hashMap.put("timestamp",""+timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(songId)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context,"Added to favorites lists",Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e->{
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static void removeFromFavorite(Context context,String songId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context,"Failed to load your info, relogin",Toast.LENGTH_SHORT).show();
        }
        else{


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(songId)
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context,"Removed to favorites lists",Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e->{
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
