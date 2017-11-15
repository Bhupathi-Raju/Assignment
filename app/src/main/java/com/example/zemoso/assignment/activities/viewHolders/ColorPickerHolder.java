package com.example.zemoso.assignment.activities.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.zemoso.assignment.R;

/**
 * Created by zemoso on 15/11/17.
 */


public class ColorPickerHolder extends RecyclerView.ViewHolder {

    private final ImageView mImageview;

    public ColorPickerHolder(View itemView) {
        super(itemView);
        mImageview = itemView.findViewById(R.id.spec_color);
    }

    public ImageView getImageview(){
        return mImageview;
    }
}