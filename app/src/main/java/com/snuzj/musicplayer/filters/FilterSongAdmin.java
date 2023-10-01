package com.snuzj.musicplayer.filters;

import android.widget.Filter;

import com.snuzj.musicplayer.adapters.AdapterSongAdmin;
import com.snuzj.musicplayer.models.ModelCategory;
import com.snuzj.musicplayer.models.ModelSong;

import java.util.ArrayList;

public class FilterSongAdmin extends Filter {
    ArrayList<ModelSong> filterlist;
    AdapterSongAdmin adapterSongAdmin;

    public FilterSongAdmin(ArrayList<ModelSong> filterlist, AdapterSongAdmin adapterSongAdmin) {
        this.filterlist = filterlist;
        this.adapterSongAdmin = adapterSongAdmin;
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
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterSongAdmin.songArrayList = (ArrayList<ModelSong>) results.values;
        adapterSongAdmin.notifyDataSetChanged();
    }
}
