package com.snuzj.musicplayer.activities.dashboard.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.snuzj.musicplayer.activities.dashboard.DashboardAdminActivity;
import com.snuzj.musicplayer.databinding.ActivityAddCategoryBinding;

import java.util.HashMap;

public class AddCategoryActivity extends AppCompatActivity {

    ActivityAddCategoryBinding binding;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, DashboardAdminActivity.class));
            finish();
        });

        binding.updateBtn.setOnClickListener(view -> validateData());
    }

    private String category = "";
    private void validateData() {
        category = binding.categoryEt.getText().toString().trim();

        //validate Data
        if (category.isEmpty()){
            Toast.makeText(this,"Category's title is empty",Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        progressDialog.setMessage("Adding category...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        //set data to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Category add successfully.", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e->{
                    progressDialog.dismiss();
                    Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }
}