package com.snuzj.musicplayer.activities.dashboard.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.DashboardUserActivity;
import com.snuzj.musicplayer.databinding.ActivityEditProfileBinding;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private final String TAG = "EDIT_PROFILE";

    ActivityEditProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        //handle click,backBtn
        binding.backBtn.setOnClickListener(view -> startActivity(new Intent(EditProfileActivity.this, DashboardUserActivity.class)));

        //handle click,add profile image
        binding.profileImagePickFab.setOnClickListener(view -> imagePickDialog());

        //handle click, update btn
        binding.updateBtn.setOnClickListener(view -> validateData());

    }

    private String name = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneCode  = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();

        if (imageUri == null){
            updateProfileDb(null);
        } else {
            uploadProfileImageStorage();
        }
    }

    private void uploadProfileImageStorage(){
        Log.d(TAG, "uploadProfileImageStorage: ");
        progressDialog.setMessage("Uploading profile image...");
        progressDialog.show();

        String filePathAndName = "UserImages/"+"profile_"+firebaseAuth.getUid();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    Log.d(TAG, "uploadProfileImageStorage: in Progress");
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    progressDialog.setMessage("Uploading profile image. Progress: " + (int)progress + "%");
                    progressDialog.show();
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "uploadProfileImageStorage: Uploaded");

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String uploadedImageUrl = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        updateProfileDb(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(e ->{
                    Log.e(TAG, "uploadProfileImageStorage: ", e);
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this,"Upload image failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileDb(String imageUrl){
        progressDialog.setMessage("Updating user info.");
        progressDialog.show();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name",name);

        if(imageUrl != null){
            hashMap.put("profileImage",imageUrl);
        }
        
        if (!status.equalsIgnoreCase("email")&&!status.equalsIgnoreCase("google")){
            hashMap.put("email",email);
        } else if (!status.equalsIgnoreCase("phone")){
            hashMap.put("phoneCode",phoneCode);
            hashMap.put("phoneNumber",phoneNumber);
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "updateProfileDb: Info Updated");
                    progressDialog.dismiss();
                    Toast.makeText(this,"Profile Updated.",Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e->{
                    progressDialog.dismiss();
                    Log.e(TAG, "updateProfileDb: ", e);
                    Toast.makeText(this,"Failed to update"+e.getMessage(),Toast.LENGTH_SHORT).show();
                });

    }
    private String status = "";
    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("name").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String phoneCode = ""+snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+snapshot.child("phoneNumber").getValue();
                        String profileImage =  ""+snapshot.child("profileImage").getValue();
                        status = ""+snapshot.child("status").getValue();

                        String phone = phoneCode + phoneNumber;

                        if(status.equalsIgnoreCase("email") || status.equalsIgnoreCase("google")){
                            binding.emailTil.setEnabled(false);
                            binding.emailEt.setEnabled(false);
                        } else {
                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.phoneCodeTil.setEnabled(false);
                        }
                        binding.emailEt.setText(email);
                        binding.phoneNumberEt.setText(phoneNumber);
                        binding.nameEt.setText(name);

                        try{
                            int phoneCodeInt = Integer.parseInt(phoneCode.replace("+",""));
                            binding.phoneCodeTil.setCountryForPhoneCode(phoneCodeInt);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }

                        try{
                            Glide.with(EditProfileActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.profilelogo)
                                    .into(binding.profileIv);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void imagePickDialog(){
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == 1){
                Log.d(TAG, "onMenuItemClick: Camera Clicked, check permission if granted");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                } else {
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            } else if (itemId == 2) {
                Log.d(TAG, "imagePickDialog: Gallery Clicked, check permission if gallery");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery();
                }else{
                    requestGalleryPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
            return false;
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Log.d(TAG, "onActivityResult: "+result.toString());

                boolean areAllGranted = true;
                for (Boolean isGranted: result.values()){
                    areAllGranted = areAllGranted && isGranted;
                }

                if (areAllGranted){
                    Log.d(TAG, "onActivityResult: All Granted e.g Camera, Storage");
                    pickImageCamera();
                } else {
                    Log.d(TAG, "onActivityResult: All or either denied");
                    Toast.makeText(EditProfileActivity.this,"Access is denied",Toast.LENGTH_SHORT).show();
                }
            }
    );

    private ActivityResultLauncher<String> requestGalleryPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted->{
                Log.d(TAG, "onActivityResult: isGranted" +isGranted);

                if (isGranted){
                    pickImageGallery();
                } else {
                    Toast.makeText(EditProfileActivity.this,"Storage Permission is denied",Toast.LENGTH_SHORT).show();
                }
            }
    );


    private Uri imageUri = null;
    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result->{
                if (result.getResultCode() == Activity.RESULT_OK){
                    Log.d(TAG, "onActivityResult: Image Captured: "+imageUri);

                    try {
                        Glide.with(EditProfileActivity.this)
                                .load(imageUri)
                                .placeholder(R.drawable.profilelogo)
                                .into(binding.profileIv);
                    } catch (Exception e){
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                } else{
                    Toast.makeText(EditProfileActivity.this,"Cancelled.",Toast.LENGTH_SHORT).show();
                }
            }
    );
    
    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Log.d(TAG, "onActivityResult: Image Picked From Gallery: "+imageUri);

                    Intent data = result.getData();
                    imageUri = data.getData();
                    
                    try {
                        Glide.with(EditProfileActivity.this)
                                .load(imageUri)
                                .placeholder(R.drawable.profilelogo)
                                .into(binding.profileIv);
                    } catch (Exception e){
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                }else{
                    Toast.makeText(EditProfileActivity.this,"Cancelled.",Toast.LENGTH_SHORT).show();
                }
            }
    );
}