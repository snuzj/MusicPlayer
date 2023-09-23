package com.snuzj.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.snuzj.musicplayer.databinding.ActivityEmailRegisterBinding;

import java.util.HashMap;

public class EmailRegisterActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActivityEmailRegisterBinding binding;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back loginActivity
        binding.backBtn.setOnClickListener(view -> onBackPressed());

        //handle click, register btn
        binding.registerBtn.setOnClickListener(view -> validateData());
    }

    private String name, email, password, cPassword = "";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cpasswordEt.getText().toString().trim();

        //validateData
        if(name.isEmpty()){
            binding.nameEt.setError("Enter your name");
            binding.nameEt.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Enter correct email");
            binding.emailEt.requestFocus();
        } else if(password.isEmpty()){
            binding.passwordEt.setError("Enter password");
            binding.passwordEt.requestFocus();
        } else if (!cPassword.equals(password) && cPassword.isEmpty()) {
            binding.cpasswordEt.setError("Confirm password doesnt match");
            binding.cpasswordEt.requestFocus();
        }else{
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        //create user in firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(AuthResult->
                {
                    progressDialog.dismiss();
                    updateUserInfo();

                })
                .addOnFailureListener(e->
                {
                    progressDialog.dismiss();
                    Toast.makeText(EmailRegisterActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current uid
        String uid = firebaseAuth.getUid();

        //set up data to add to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("phoneCode","");
        hashMap.put("phoneNumber","");
        hashMap.put("name",name);
        hashMap.put("profileImage", "");
        hashMap.put("userType","user");
        hashMap.put("status","email");
        hashMap.put("timestamp",timestamp);

        //set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused ->
                        {
                            progressDialog.dismiss();
                            Toast.makeText(EmailRegisterActivity.this,"Account created.",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EmailRegisterActivity.this,DashboardUserActivity.class));
                            finish();
                        }
                        )
                .addOnFailureListener(e->
                        {
                         progressDialog.dismiss();
                         Toast.makeText(EmailRegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        });

    }
}