package com.snuzj.musicplayer.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.activities.dashboard.DashboardAdminActivity;
import com.snuzj.musicplayer.adapters.AdapterCategory;
import com.snuzj.musicplayer.adapters.AdapterCategoryUser;
import com.snuzj.musicplayer.databinding.FragmentHomeBinding;
import com.snuzj.musicplayer.models.ModelCategory;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategoryUser adapterCategory;
    Context mContext;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        loadCategories();



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
                adapterCategory = new AdapterCategoryUser(mContext,categoryArrayList);
                //set adapter to recyclerView
                binding.categoryRv.setAdapter(adapterCategory);
                adapterCategory.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}