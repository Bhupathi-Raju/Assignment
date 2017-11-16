package com.example.zemoso.assignment.activities.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.adapters.VideoGalleryAdapter;
import com.example.zemoso.assignment.activities.model.VideoInfo;
import com.example.zemoso.assignment.activities.interfaces.SupportActionBar;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {


    private SupportActionBar mContext;

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance(){
        return new GalleryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_gallery, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gallery);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),3);
        final Realm bgRealm = Realm.getDefaultInstance();
        RealmResults<VideoInfo> videoInfoRealmResults = bgRealm.where(VideoInfo.class).findAll();
        List<VideoInfo> videoInfoList = new ArrayList<>(videoInfoRealmResults);
        final VideoGalleryAdapter videoGalleryAdapter = new VideoGalleryAdapter(
                getActivity(),
                videoInfoList,
                (VideoGalleryAdapter.VideoPreviewInterface)getActivity()
        );
        recyclerView.setAdapter(videoGalleryAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        bgRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(@NonNull Realm realm) {
                Log.d("realm","realm changed");
                RealmResults<VideoInfo> videoInfoRealmResults = bgRealm.where(VideoInfo.class).findAll();
                videoGalleryAdapter.updateList(videoInfoRealmResults);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (SupportActionBar) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.showSupportActionBar();
    }
}
