package com.snuzj.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.adapters.AdapterSongUser;
import com.snuzj.musicplayer.databinding.FragmentSearchBinding;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    Context mContext;
    FragmentSearchBinding binding;

    ArrayList<ModelSong> songArrayList;

    private AdapterSongUser adapterSongUser;

    public SearchFragment() {
        // Required empty public constructor
    }

    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(LayoutInflater.from(mContext),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        binding.songUserRv.setLayoutManager(layoutManager);

        loadSongList();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) {
                try {
                    adapterSongUser.getFilter().filter(sequence);
                } catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadSongList() {
        songArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Songs");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelSong model = ds.getValue(ModelSong.class);
                    songArrayList.add(model);
                }
                //setup adapter
                adapterSongUser = new AdapterSongUser(getContext(),songArrayList);
                //set adapter to recyclerview
                binding.songUserRv.setAdapter(adapterSongUser);
                adapterSongUser.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}