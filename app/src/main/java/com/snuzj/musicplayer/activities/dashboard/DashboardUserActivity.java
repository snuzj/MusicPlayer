package com.snuzj.musicplayer.activities.dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.snuzj.musicplayer.R;
import com.snuzj.musicplayer.databinding.ActivityDashboardUserBinding;
import com.snuzj.musicplayer.fragments.AccountFragment;
import com.snuzj.musicplayer.fragments.HomeFragment;
import com.snuzj.musicplayer.fragments.SearchFragment;

public class DashboardUserActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActivityDashboardUserBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth= FirebaseAuth.getInstance();

        showHomeFragment();

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == R.id.menu_home){
                    showHomeFragment();
                    return true;
                } else if (itemId == R.id.menu_search) {
                    showSearchFragment();
                    return true;
                } else if (itemId == R.id.menu_you) {
                    showYouFragment();
                    return true;
                }
                else{
                    return false;
                }

            }
        });
    }

    private void showYouFragment() {
        AccountFragment fragment = new AccountFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.framelayoutFl.getId(),fragment,"AccountFragment");
        fragmentTransaction.commit();
    }

    private void showSearchFragment() {
        SearchFragment fragment = new SearchFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.framelayoutFl.getId(),fragment,"SearchFragment");
        fragmentTransaction.commit();
    }

    private void showHomeFragment() {
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.framelayoutFl.getId(),fragment,"HomeFragment");
        fragmentTransaction.commit();
    }
}