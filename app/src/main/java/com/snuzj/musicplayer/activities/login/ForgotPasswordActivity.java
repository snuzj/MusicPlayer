package com.snuzj.musicplayer.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;

    FirebaseAuth firebaseAuth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.submitBtn.setOnClickListener(view -> validateData());


    }

    private String email = "";
    private void validateData() {
        email = binding.emailEt.getText().toString().trim();

        if (email.isEmpty()){
            binding.emailEt.setError("Enter the correct email!");
            binding.emailEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid email format");
            binding.emailEt.requestFocus();
        } else{
            recoverPassword();
        }
    }

    private void recoverPassword() {
        progressDialog.setMessage("Sending instruction to "+email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "Instructions sent to "+email, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e->{
                    progressDialog.dismiss();
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                );
    }
}