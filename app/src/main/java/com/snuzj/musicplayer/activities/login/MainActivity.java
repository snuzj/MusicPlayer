package com.snuzj.musicplayer.activities.login;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.DashboardUserActivity;
import com.snuzj.musicplayer.databinding.ActivityMainBinding;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    GoogleSignInClient googleSignInClient;

    private static final String TAG = "LOGIN_OPTIONS_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);


        binding.emailBtn.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this,EmailLoginActivity.class)));

        binding.googleBtn.setOnClickListener(view -> beginGoogleLogin());

        binding.phoneBtn.setOnClickListener(view->
                startActivity(new Intent(MainActivity.this, PhoneLoginActivity.class)));
    }

    private void beginGoogleLogin() {
        Intent gsic = googleSignInClient.getSignInIntent();
        googleSignInInARL.launch(gsic);
    }

    private ActivityResultLauncher<Intent> googleSignInInARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "onActivityResult: ");
                if(result.getResultCode() == RESULT_OK){
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try{
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "onActivityResult: Account Id "+account.getId());
                        firebaseAuthWithGoogleAccount(account.getIdToken());
                    } catch (Exception e){
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                }
                else{
                    Log.d(TAG, "onActivityResult: Cancelled");
                    Toast.makeText(MainActivity.this,"Cancelled.",Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void firebaseAuthWithGoogleAccount(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken"+idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(AuthResult->
                        {
                            if(AuthResult.getAdditionalUserInfo().isNewUser()){
                                Log.d(TAG, "firebaseAuthWithGoogleAccount: New User, Created...");
                                updateUserToDb();
                            }
                            else{
                                Log.d(TAG, "firebaseAuthWithGoogleAccount: User Existed...");
                                startActivity(new Intent(MainActivity.this, DashboardUserActivity.class));
                                finish();
                            }
                        }
                        )
                .addOnFailureListener(e->
                        Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e));
    }

    private void updateUserToDb() {
        Log.d(TAG, "updateUserToDb: ");

        progressDialog.setMessage("Saving user info");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uid = firebaseAuth.getUid();
        String name = firebaseAuth.getCurrentUser().getDisplayName();

        //set up data to add to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("phoneCode","");
        hashMap.put("phoneNumber","");
        hashMap.put("name",name);
        hashMap.put("profileImage", "");
        hashMap.put("userType","user");
        hashMap.put("status","google");
        hashMap.put("timestamp",timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused->
                        {
                            progressDialog.dismiss();
                            startActivity(new Intent(MainActivity.this,DashboardUserActivity.class));
                            finish();
                        }

                )
                .addOnFailureListener(e->
                {
                    Log.e(TAG, "updateUserToDb: ", e);
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,"Failed to save user info"+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }
}