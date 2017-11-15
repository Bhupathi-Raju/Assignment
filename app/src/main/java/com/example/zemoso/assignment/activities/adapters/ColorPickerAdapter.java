package com.example.zemoso.assignment.activities.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.viewHolders.ColorPickerHolder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zemoso on 8/11/17.
 */

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerHolder> {


    //region Interface to setColor
    public interface TextColorPicker{
         void setColor(int color);
    }
    //endregion

    //region variables
    private Context mContext;
    private ImageView selectedImageView;
    private ArrayList<Integer> randomColorList = new ArrayList<>();
    private int mSelectedPosition;
    private TextColorPicker textColorPicker;
    private static final String TAG = ColorPickerAdapter.class.getSimpleName();
    //endregion

    //region Constructor
    public ColorPickerAdapter(Context context,TextColorPicker textColorPicker){
        this.mContext = context;
        this.textColorPicker = textColorPicker;
    }
    //endregion


    @Override
    public ColorPickerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.color_picker,parent,false);
        return new ColorPickerHolder(view);
    }

    @Override
    public void onBindViewHolder(final ColorPickerHolder holder, int position) {
        final Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        int randomColor = Color.rgb(r,g,b);
        randomColorList.add(randomColor);
        if(holder.getImageview()!=selectedImageView) {
            Log.d(TAG,String.valueOf(position));
            Log.d(TAG,String.valueOf(holder.getAdapterPosition()));
            holder.getImageview().setBackgroundColor(randomColorList.get(holder.getAdapterPosition()));
        }
        else {
            Log.d(TAG,"selected");
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setStroke(5,Color.GRAY);
            drawable.setColor(randomColorList.get(holder.getAdapterPosition()));
            holder.getImageview().setBackground(drawable);
        }

        holder.getImageview().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedImageView != null && selectedImageView != view) {
                    selectedImageView.setBackgroundColor(randomColorList.get(mSelectedPosition));
                }
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setStroke(5,Color.GRAY);
                drawable.setColor(randomColorList.get(holder.getAdapterPosition()));
                view.setBackground(drawable);
                selectedImageView = (ImageView) view;
                mSelectedPosition = holder.getAdapterPosition();
                textColorPicker.setColor(randomColorList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
