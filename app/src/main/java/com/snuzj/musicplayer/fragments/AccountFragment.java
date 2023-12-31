package com.snuzj.musicplayer.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snuzj.musicplayer.activities.dashboard.user.EditProfileActivity;
import com.snuzj.musicplayer.activities.login.MainActivity;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.adapters.AdapterSongFav;
import com.snuzj.musicplayer.databinding.FragmentAccountBinding;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class AccountFragment extends Fragment {

    FragmentAccountBinding binding;
    FirebaseAuth firebaseAuth;
    Context mContext;

    String songId, titleTv, artistTv, imageUrl;

    boolean isFavorite = false;
    ArrayList<ModelSong> songArrayList;
    AdapterSongFav adapterSongFav;

    public AccountFragment() {
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
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        loadFavoriteBooks();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        binding.favoriteRv.setLayoutManager(layoutManager);

        //handle click, edit btn
        binding.editBtn.setOnClickListener(View->{
            startActivity(new Intent(mContext, EditProfileActivity.class));
            getActivity().finish();
        });

        //handle click, moreBtn
        binding.moreBtn.setOnClickListener(View->showRequestMusicDialog());

        //handle click, logoutBtn
        binding.logoutBtn.setOnClickListener(View->{
            firebaseAuth.signOut();
            startActivity(new Intent(mContext, MainActivity.class));
            getActivity().finish();
        });


    }

    private void loadFavoriteBooks() {
        songArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String songId = ""+ds.child("songId").getValue();
                            ModelSong model = new ModelSong();
                            model.setId(songId);

                            songArrayList.add(model);
                        }

                        binding.favoritesTv.setText("Following: "+ songArrayList.size());
                        adapterSongFav = new AdapterSongFav(mContext,songArrayList);
                        binding.favoriteRv.setAdapter(adapterSongFav);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if the fragment is still attached to the activity
                        if (!isAdded()) {
                            return;
                        }

                        String name = "" + snapshot.child("name").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        String phone = phoneCode + phoneNumber;

                        // Set Text
                        binding.nameTv.setText(name);
                        binding.emailTv.setText(email);
                        binding.phoneTv.setText(phone);

                        // Load profile image using Glide (with the isAdded() check)
                        if (isAdded()) {
                            Glide.with(mContext)
                                    .load(profileImage)
                                    .placeholder(R.drawable.profilelogo)
                                    .into(binding.profileIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showRequestMusicDialog() {
        final String[] options = {"Request Music"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(options, (dialog, position) -> {
            // Handle item click
            if (position == 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/c1tDgn2vvFbDBW8i8"));
                startActivity(intent);
                }
            }).show();



    }
}