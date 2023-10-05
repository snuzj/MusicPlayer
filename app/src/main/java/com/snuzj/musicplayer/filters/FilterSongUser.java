package com.snuzj.musicplayer.filters;

import android.widget.Filter;

import com.snuzj.musicplayer.adapters.AdapterSongUser;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class FilterSongUser extends Filter {

    ArrayList<ModelSong> filterlist;
    AdapterSongUser adapterSongUser;

    public FilterSongUser(ArrayList<ModelSong> filterlist, AdapterSongUser adapterSongUser) {
        this.filterlist = filterlist;
        this.adapterSongUser = adapterSongUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0){
            //change to uppercase
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelSong> filteredModels = new ArrayList<>();
            for (int i = 0; i< filterlist.size();i++){
                //validateData
                if (filterlist.get(i).getTitle().toUpperCase().contains(constraint)){
                    //add to filterlist
                    filteredModels.add(filterlist.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterlist.size();
            results.values = filterlist;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        adapterSongUser.songArrayList = (ArrayList<ModelSong>) results.values;
        adapterSongUser.notifyDataSetChanged();
    }
}
