package com.snuzj.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.snuzj.musicplayer.databinding.ActivityPhoneLoginBinding;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    ActivityPhoneLoginBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private static final String TAG = "LOGIN_PHONE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.otpInputRl.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallBack();


        binding.backBtn.setOnClickListener(view -> onBackPressed());

        binding.sendOtpBtn.setOnClickListener(view ->
        {
            validateData();
        });

        binding.resendOtpTv.setOnClickListener(view -> resendVerificationWithCode(forceResendingToken)
            );

        binding.verifyOtpBtn.setOnClickListener(view -> {
            String otp = binding.otpEt.getText().toString().trim();
            Log.d(TAG, "onCreate: OTP " +otp);

            if(otp.isEmpty()){
                binding.otpEt.setError("Enter OTP.");
                binding.otpEt.requestFocus();
            } else if (otp.length() < 6) {
                binding.otpEt.setError("OTP length must be 6 characters.");
                binding.otpEt.requestFocus();
            }else{
                verifyPhoneNumberWithCode(mVerificationId,otp);
            }

        });

    }

    private String phoneCode, phoneNumber, phoneNumberWithCode = "";
    private void validateData() {
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode+phoneNumber;

        Log.d(TAG, "validateData: phoneCode " + phoneCode);
        Log.d(TAG, "validateData: phoneNumber " + phoneNumber);
        Log.d(TAG, "validateData: phoneNumberWithCode " + phoneNumberWithCode);

        if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.setError("Enter your phone number");
            binding.phoneNumberEt.requestFocus();
        }
        else{
            startPhoneNumberVerification();
        }

    }

    private void startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ");

        progressDialog.setMessage("Sending OTP to " + phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneLoginCallBack() {
        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ");

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);
                Toast.makeText(PhoneLoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);

                mVerificationId = verificationId;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(View.INVISIBLE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                Toast.makeText(PhoneLoginActivity.this,"Please type the verification code sent to " + phoneNumberWithCode,Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void verifyPhoneNumberWithCode(String verificationId, String otp) {
        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationWithCode(PhoneAuthProvider.ForceResendingToken token){
        Log.d(TAG, "resendVerificationWithCode: ForceResendingToken"+token);

        progressDialog.setMessage("Resending OTP to "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        progressDialog.setMessage("Logging In");
        progressDialog.show();

        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(
                new OnSuccessListener<AuthResult>() {
                   @Override
                   public void onSuccess(AuthResult authResult) {
                       Log.d(TAG, "onSuccess: ");
                       if (authResult.getAdditionalUserInfo().isNewUser()){
                           Log.d(TAG, "onSuccess: New User");
                           updateUserInfoDb();

                       } else{
                           Log.d(TAG, "onSuccess: Existing User");
                           startActivity(new Intent(PhoneLoginActivity.this,DashboardUserActivity.class));
                           finish();
                       }

                   }
               })
                .addOnFailureListener(e->{
                        Log.e(TAG, "signInWithPhoneAuthCredential: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(PhoneLoginActivity.this,"Failed login due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                );
    }

    private void updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ");
        progressDialog.setMessage("Saving user Info...");

        long timestamp = System.currentTimeMillis();

        String uid = firebaseAuth.getUid();

        //set up data to add to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email","");
        hashMap.put("phoneCode",phoneCode);
        hashMap.put("phoneNumber",phoneNumber);
        hashMap.put("name","");
        hashMap.put("profileImage", "");
        hashMap.put("userType","user");
        hashMap.put("status","phone");
        hashMap.put("timestamp",timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    startActivity(new Intent(PhoneLoginActivity.this, DashboardUserActivity.class));
                    finish();
                })
                .addOnFailureListener(e->{
                    Log.e(TAG, "updateUserInfoDb: ", e);
                    Toast.makeText(PhoneLoginActivity.this,"Failed to save info "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }
}