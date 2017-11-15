package com.example.zemoso.assignment.activities.adapters;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.model.VideoInfo;
import com.example.zemoso.assignment.activities.viewHolders.GalleryViewHolder;

import java.io.File;
import java.util.List;

/**
 * Created by zemoso on 6/11/17.
 */

public class VideoGalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {

    public interface VideoPreviewInterface{
        void startPreview(String videoFileName);
    }

    private VideoPreviewInterface videoPreviewInterface;
    private final Activity mActivity;
    private List<VideoInfo> videoInfoList;
    private static final String TAG = VideoGalleryAdapter.class.getSimpleName();

    public VideoGalleryAdapter(Activity mActivity,List<VideoInfo> videoInfoList,VideoPreviewInterface videoPreviewInterface){
        this.mActivity = mActivity;
        this.videoInfoList = videoInfoList;
        this.videoPreviewInterface= videoPreviewInterface;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity)
                .inflate(R.layout.video_viewer,parent,false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
       final String videoName = videoInfoList.get(position).getVideoPath();
            Glide.with(mActivity)
                    .load(Uri.fromFile(new File(videoName)))
                    .centerCrop()
                    .into(holder.getImageView());
        holder.getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPreviewInterface.startPreview(videoName);
            }
        });
    }


    @Override
    public int getItemCount() {
        if(videoInfoList ==null)
            return 0;
        Log.d(TAG,String.valueOf(videoInfoList.size()));
        return videoInfoList.size();
    }




 /*   private Bitmap getBitmapFromFolder(int position){
        int idIndex = mVideoStoreCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        mVideoStoreCursor.moveToPosition(position);
        return MediaStore.Video.Thumbnails.getThumbnail(
                mActivity.getContentResolver(),
                mVideoStoreCursor.getLong(idIndex),
                MediaStore.Video.Thumbnails.MICRO_KIND,
                null
        );
    }*/

    public void updateList(List<VideoInfo> videoInfoList)
    {
        this.videoInfoList = videoInfoList;
        notifyDataSetChanged();
    }
}
