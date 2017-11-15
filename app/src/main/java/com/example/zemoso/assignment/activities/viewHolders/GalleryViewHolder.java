package com.example.zemoso.assignment.activities.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.zemoso.assignment.R;

/**
 * Created by zemoso on 15/11/17.
 */

public  class GalleryViewHolder extends RecyclerView.ViewHolder {

    private final ImageView mimageView;

    public GalleryViewHolder(View itemView) {
        super(itemView);
        mimageView = itemView.findViewById(R.id.imageview);
    }

    public ImageView getImageView(){
        return mimageView;
    }
}