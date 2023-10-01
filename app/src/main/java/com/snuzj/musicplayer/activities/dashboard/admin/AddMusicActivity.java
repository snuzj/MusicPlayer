package com.snuzj.musicplayer.activities.dashboard.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.DashboardAdminActivity;
import com.snuzj.musicplayer.activities.dashboard.user.EditProfileActivity;
import com.snuzj.musicplayer.databinding.ActivityAddMusicBinding;
import com.snuzj.musicplayer.models.ModelCategory;

import java.util.ArrayList;
import java.util.HashMap;

public class AddMusicActivity extends AppCompatActivity {

    ActivityAddMusicBinding binding;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    MediaMetadataRetriever metadataRetriever;
    private final String TAG = "ADD_MUSIC";

    private Uri imageUri = null;

    private Uri audioUri = null;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMusicCategories();

        binding.backBtn.setOnClickListener(view -> startActivity(new Intent(this, DashboardAdminActivity.class)));

        binding.addImageBtn.setOnClickListener(view -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                pickImageGallery();
            }else{
                requestGalleryPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        binding.categoryTv.setOnClickListener(view -> categoryPickDialog());

        binding.uploadSongBtn.setOnClickListener(view ->uploadSong());

        binding.updateBtn.setOnClickListener(view ->validateData());



    }

    private String title, album, artist, duration = "";

    private void validateData() {
        Log.d(TAG, "validateData: ");

        title = binding.titleEt.getText().toString().trim();
        album = binding.albumEt.getText().toString().trim();
        artist = binding.artistEt.getText().toString().trim();
        duration = binding.durationEt.getText().toString().trim();


        if(title.isEmpty()){
            binding.titleEt.setError("title is empty");
            binding.titleEt.requestFocus();
        } else if (album.isEmpty()) {
            binding.albumEt.setError("album is empty");
            binding.albumEt.requestFocus();
        } else if (artist.isEmpty()) {
            binding.artistEt.setError("artist is empty");
            binding.artistEt.requestFocus();
        } else if (duration.isEmpty()) {
            binding.durationEt.setError("duration is empty");
            binding.durationEt.requestFocus();
        } else if (selectedCategoryTitle.isEmpty()){
            Toast.makeText(AddMusicActivity.this,"Please choose category",Toast.LENGTH_SHORT).show();
        }
        else {
            uploadFiles();
        }


    }

    private void uploadFiles() {
        Log.d(TAG, "uploadFiles: ");
        progressDialog.setMessage("Uploading audio file");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String fileAndPathName = "Songs/Files/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileAndPathName);
        storageReference.putFile(audioUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Audio file uploaded to storage");
                        Log.d(TAG, "onSuccess: getting audio url");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedAudioUrl = ""+uriTask.getResult();

                        //upload to firebase db
                        uploadAudioImage(uploadedAudioUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Audio File upload failed due to" +e.getMessage());
                        Toast.makeText(AddMusicActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAudioImage(String uploadedAudioUrl, long timestamp) {
        Log.d(TAG, "uploadAudioImage: ");
        progressDialog.setMessage("Uploading audio image");
        progressDialog.show();

        String fileAndPathName = "Songs/Images/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileAndPathName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    Log.d(TAG, "uploadProfileImageStorage: in Progress");
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    progressDialog.setMessage("Uploading Audio image. Progress: " + (int)progress + "%");
                    progressDialog.show();
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "uploadProfileImageStorage: Uploaded");

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String uploadedImageUrl = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        updateSongtoDb(uploadedImageUrl,uploadedAudioUrl, timestamp);
                    }
                })
                .addOnFailureListener(e ->{
                    Log.e(TAG, "uploadProfileImageStorage: ", e);
                    progressDialog.dismiss();
                    Toast.makeText(AddMusicActivity.this,"Upload image failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });


    }

    private void updateSongtoDb(String uploadedImageUrl,String uploadedAudioUrl ,Long timestamp) {
        Log.d(TAG, "updateSongtoDb: ");
        progressDialog.setMessage("Uploading audio info");
        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("album",""+album);
        hashMap.put("artist",""+artist);
        hashMap.put("duration",""+duration);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("audioUrl",""+uploadedAudioUrl);
        hashMap.put("imageUrl",""+uploadedImageUrl);
        hashMap.put("timestamp",timestamp);

        //db reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
        databaseReference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded");
                        Toast.makeText(AddMusicActivity.this,"Successfully uploaded.",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload audio to db " +e.getMessage());
                        Toast.makeText(AddMusicActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadMusicCategories() {
        Log.d(TAG, "loadMusicCategories: ");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db ref
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get array of categories
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, (dialogInterface, which) -> {
                    selectedCategoryTitle = categoryTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);
                    binding.categoryTv.setText(selectedCategoryTitle);

                    Log.d(TAG, "categoryPickDialog: Selected Category "+selectedCategoryId+" "+selectedCategoryTitle);

                }).show();
    }

    private void pickImageGallery() {
        // Log the start of the image picking process
        Log.d(TAG, "pickImageGallery: ");

        // Create an intent to pick an image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // Launch the gallery activity using the activity result launcher
        galleryActivityResultLauncher.launch(intent);
    }

    // Activity result launcher for requesting gallery-related permissions
    private ActivityResultLauncher<String> requestGalleryPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                // Log whether the permission is granted or not
                Log.d(TAG, "onActivityResult: isGranted" + isGranted);

                // If permission is granted, proceed to pick an image; otherwise, show a toast
                if (isGranted) {
                    pickImageGallery();
                } else {
                    Toast.makeText(AddMusicActivity.this, "Storage Permission is denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Activity result launcher for starting an activity to pick an image from the gallery
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Check if the result code indicates success
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Log the successful image picking
                    Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);

                    // Get the data from the result
                    Intent data = result.getData();

                    // Set the image URI from the selected data
                    imageUri = data.getData();

                    try {
                        // Use Glide library to load and display the selected image
                        Glide.with(AddMusicActivity.this)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_song)
                                .into(binding.imageIv);
                    } catch (Exception e) {
                        // Log any exceptions that occur during image loading
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                } else {
                    // Show a toast indicating cancellation
                    Toast.makeText(AddMusicActivity.this, "Cancelled.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void uploadSong() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            audioUri = data.getData();

            // Initialize the metadataRetriever
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(this, audioUri);

            // Now you can retrieve metadata information
            String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            // Format the duration in hours:minutes:seconds
            String formattedDuration = formatDuration(Long.parseLong(duration));

            // Display the formatted duration in the durationEt EditText
            binding.durationEt.setText(formattedDuration);

            // Get the file name from the audioUri
            String fileName = getFileNameFromUri(audioUri);

            // Display the file name in the uploadTv TextView
            binding.uploadTv.setText(fileName);

            // You can also handle other metadata properties as needed
        }
    }

    // Helper method to format duration in hours:minutes:seconds
    private String formatDuration(long durationInMillis) {
        long seconds = durationInMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Helper method to get the file name from a Uri
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        Cursor cursor = null;

        try {
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            cursor = getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                result = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name from Uri", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }



}