package com.snuzj.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snuzj.musicplayer.activities.dashboard.admin.SongListAdminActivity;
import com.snuzj.musicplayer.activities.dashboard.user.SongListUserActivity;
import com.snuzj.musicplayer.databinding.CategoryItemBinding;
import com.snuzj.musicplayer.databinding.RowCategoryBinding;
import com.snuzj.musicplayer.models.ModelCategory;

import java.util.ArrayList;

public class AdapterCategoryUser extends RecyclerView.Adapter<AdapterCategoryUser.HolderCategoryUser>  {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList;
    CategoryItemBinding binding;

    public AdapterCategoryUser(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public AdapterCategoryUser.HolderCategoryUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = CategoryItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new AdapterCategoryUser.HolderCategoryUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCategoryUser.HolderCategoryUser holder, int position) {
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.categoryTv.setText(category);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, SongListUserActivity.class);
            intent.putExtra("categoryId",id);
            intent.putExtra("categoryTitle",category);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    class HolderCategoryUser extends RecyclerView.ViewHolder{

        TextView categoryTv;
        public HolderCategoryUser(@NonNull View itemView) {
            super(itemView);
            categoryTv = binding.categoryTv;
        }
    }
}
