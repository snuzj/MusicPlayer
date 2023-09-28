package com.snuzj.musicplayer.activities.dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.activities.dashboard.admin.AddCategoryActivity;
import com.snuzj.musicplayer.activities.login.MainActivity;
import com.snuzj.musicplayer.adapters.AdapterCategory;
import com.snuzj.musicplayer.databinding.ActivityDashboardAdminBinding;
import com.snuzj.musicplayer.models.ModelCategory;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    ActivityDashboardAdminBinding binding;
    FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        loadCategories();

        binding.logoutBtn.setOnClickListener(view-> {
            firebaseAuth.signOut();
            checkUser();
        });

        binding.addCategoryBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, AddCategoryActivity.class));
            finishAffinity();
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) {
                try {
                    adapterCategory.getFilter().filter(sequence);
                } catch (Exception e){
                    
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    private void loadCategories() {
        categoryArrayList = new ArrayList<>();

        //get all categories from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
                //set adapter to recyclerView
                binding.categoryRv.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser==null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}