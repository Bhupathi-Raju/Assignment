package com.example.zemoso.assignment.activities.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.model.VideoInfo;
import com.example.zemoso.assignment.activities.utils.AssignmnetApplication;
import com.example.zemoso.assignment.activities.interfaces.SupportActionBar;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewFragment extends Fragment{

    private SupportActionBar msupportActionBar;

    public PreviewFragment() {
        // Required empty public constructor
    }


    public static PreviewFragment newInstance(String videoFileName,int flag)
    {
        PreviewFragment previewFragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_name",videoFileName);
        bundle.putInt("from_gallery",flag);
        previewFragment.setArguments(bundle);
        return previewFragment;
    }

    //region Variables
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private SimpleExoPlayer exoPlayer;
    private Realm bgRealm;
    private ImageButton save;
    private VideoInfo videoInfo = new VideoInfo();
    private Long mCurrentPosition = null;
    private File videoFile;
    private String videoFileName;
    private int previewFromGallery =0;
    private int saved;
    private boolean exoplayerState = true;
    private MediaSource videoSource;
    private RelativeLayout relativeLayout;
    private View.OnTouchListener onTouchListener;
    private int i=1;
    private static final String TAG = PreviewFragment.class.getSimpleName();
    //endregion

    //region LifeCycleMethods
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        videoFileName = this.getArguments().getString("file_name");
        previewFromGallery = this.getArguments().getInt("from_gallery");
        save = view.findViewById(R.id.save);
        if(previewFromGallery == 1)
            save.setVisibility(View.GONE);
        final TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(null);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        relativeLayout = view.findViewById(R.id.comments_holder);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
        final SimpleExoPlayerView exoPlayerView = view.findViewById(R.id.exoplayer);
        exoPlayerView.setPlayer(exoPlayer);
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "Assignment"), null);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        Uri mp4VideoUri = Uri.parse(videoFileName);
        videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory,
                extractorsFactory, null, null);

        // LoopingMediaSource loopingMediaSource = new LoopingMediaSource(videoSource);

        // exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                    Log.d(TAG, "reached end");
                    exoPlayer.seekTo(0);
                    exoPlayer.setPlayWhenReady(true);
                }
                exoplayerState = playWhenReady;
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "Player Error",error);
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

        final GestureDetector detector = new GestureDetector(getActivity(),
                new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        return true;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        boolean isPlaying;
                        Log.d(TAG, "on tap");
                        isPlaying = exoPlayer.getPlayWhenReady();
                        exoPlayer.setPlayWhenReady(!isPlaying);
                        if (isPlaying && previewFromGallery !=1) {
                            save.setVisibility(View.INVISIBLE);
                        } else if(previewFromGallery!=1)
                        {
                            save.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {
                         Log.d(TAG,"long tap");
                         if(!exoPlayer.getPlayWhenReady())
                         if(previewFromGallery!=1)
                         {
                             save.setVisibility(View.INVISIBLE);
                             Fragment commentFragment = CommentFragment.newInstance(videoInfo);
                             getActivity().getSupportFragmentManager().beginTransaction()
                                     .replace(R.id.prev_container,commentFragment).addToBackStack(null).commit();
//                             getFragmentManager().beginTransaction()
//                                    .replace(R.id.frag_container,commentFragment).addToBackStack(null).commit();
                         }
                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }
                });


        exoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return detector.onTouchEvent(motionEvent);
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"saved");
                saveVideoToGallery();
                getActivity().getSupportFragmentManager().popBackStack("gallery",0);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        msupportActionBar = (SupportActionBar) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        msupportActionBar.hideSupportActionBar();
        if(exoPlayer!=null) {
            exoPlayer.prepare(videoSource);
            if(mCurrentPosition!=null)
                exoPlayer.seekTo(mCurrentPosition);
            exoPlayer.setPlayWhenReady(exoplayerState);
        }

        onTouchListener = new View.OnTouchListener() {
            private int mActivePointerId = INVALID_POINTER_ID;
            private int mSeconPointerId = INVALID_POINTER_ID;
            float mLastTouchX,mLastTouchY,mSecondTouchX,mSecondTouchY;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getActionMasked()){
                    case MotionEvent.ACTION_DOWN: {
                    Log.d(TAG,"action down");
                        final int pointerIndex = motionEvent.getActionIndex();
                        Log.d(TAG, String.valueOf(pointerIndex));
                        final float x = motionEvent.getX(pointerIndex);
                        final float y = motionEvent.getY(pointerIndex);
                        Log.d(TAG, "X " + String.valueOf(x) + "Y " + String.valueOf(y));
                        mLastTouchX = x;
                        mLastTouchY = y;
                        mActivePointerId = motionEvent.getPointerId(pointerIndex);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {

                      //  final double d = Math.sqrt(Math.pow(mSecondTouchX - mLastTouchX, 2) + Math.pow(mSecondTouchY - mLastTouchY, 2));
                        final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                        Log.d(TAG,"PointerIndex" + String.valueOf(pointerIndex));
                        if(pointerIndex!=-1) {
                            final float x = motionEvent.getX(pointerIndex);
                            final float y = motionEvent.getY(pointerIndex);

                            final float dx = x - mLastTouchX;
                            final float dy = y - mLastTouchY;

                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                            layoutParams.topMargin = layoutParams.topMargin + Math.round(dy);
                            layoutParams.leftMargin = layoutParams.leftMargin +Math.round(dx);
                            view.setLayoutParams(layoutParams);

                           /* view.setX(view.getX()+dx);
                            view.setY(view.getY()+dy);*/
                        }
                 /*       if(motionEvent.getPointerCount() ==2)
                        {
                             final float x_active = motionEvent.getX(pointerIndex);
                             final float y_active = motionEvent.getY(pointerIndex);
                             final int pointerIndexSecondary = motionEvent.findPointerIndex(mSeconPointerId);
                             final float x_second = motionEvent.getX(pointerIndexSecondary);
                             final float y_second = motionEvent.getY(pointerIndexSecondary);
                             double d2 = Math.sqrt(Math.pow(x_second - x_active, 2) + Math.pow(y_second - y_active, 2));
                             double angle = Math.asin(d2/d);

                            view.setRotation((float) (angle * 180/Math.PI));

                        }
*/
/*
                        int x_cord = (int) motionEvent.getRawX();
                        int y_cord = (int) motionEvent.getRawY();
*/
                       /* float mPosX = view.getX();
                        float mPosY = view.getY();

                        if (x_cord > windowwidth) {
                            x_cord = windowwidth;
                        }
                        if (y_cord > windowheight) {
                            y_cord = windowheight;
                        }*/
                        //   Log.d("changed", "moved" + String.valueOf(x_cord) + "y " + String.valueOf(y_cord));
/*
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = layoutParams.leftMargin + Math.round(dx);
                        layoutParams.topMargin = layoutParams.topMargin + Math.round(dy);
                        Log.d("pointer margins",String.valueOf(view.getLeft()) + "top "
                        +String.valueOf(view.getTop()));
                        Log.d("pointer delta",String.valueOf(dx) + "Y " + String.valueOf(dy));
//                        view.setLeft(view.getLeft() + Math.round(dx));
//                        view.setTop(view.getTop()  + Math.round(dy));
                        view.setLayoutParams(layoutParams);
                        view.invalidate();*/
                        // view.postInvalidate();

//                        view.setTop(view.getTop() - Math.round(dx));
//                        view.setLeft(view.getLeft() - Math.round(dy));
//                        mLastTouchX =x;
//                        mLastTouchY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:{
                        if (previewFromGallery!=1) {
                            videoInfo.setxPosList(view.getX());
                            videoInfo.setyPosList(view.getY());
                        }
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN:{
                        final int pointerIndex = motionEvent.getActionIndex();
                        mSeconPointerId = motionEvent.getPointerId(pointerIndex);
                        mSecondTouchX = motionEvent.getX(pointerIndex);
                        mSecondTouchY = motionEvent.getY(pointerIndex);
                        Log.d(TAG,"Action pointer down");
                        break;

                    }
                    case MotionEvent.ACTION_POINTER_UP:{
                        Log.d(TAG,"Action pointer up");
                        final int pointerIndex = motionEvent.getActionIndex();
                        final int pointerId = motionEvent.getPointerId(pointerIndex);
                        if (pointerId == mActivePointerId) {
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            mLastTouchX = motionEvent.getX(newPointerIndex);
                            mLastTouchY = motionEvent.getY(newPointerIndex);
                            mActivePointerId = motionEvent.getPointerId(newPointerIndex);
                        }
                        break;

                    }
                    case MotionEvent.ACTION_CANCEL:{
                        Log.d(TAG,"action cancel");
                        mActivePointerId = INVALID_POINTER_ID;
                    }

                }
                return true;
            }
        };

        if(previewFromGallery==1) {
            videoInfo = Realm.getDefaultInstance().where(VideoInfo.class).equalTo("videoPath", videoFileName).findFirst();

            if (videoInfo != null)
                if (videoInfo.getCommentPath() != null) {
                    Log.d(TAG, "setting Comments");
                    setComments();
                } else
                    Log.d(TAG, "no comments");
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentPosition = exoPlayer.getCurrentPosition();
        Log.d("on pause calling", "called");
    }


    @Override
    public void onStop() {
        Log.d("on stop calling", "called");
        super.onStop();
        if(saved!=1 && previewFromGallery !=1) {
            RealmList<String> commentList = videoInfo.getCommentPath();
            for(String commentPath : commentList) {
                File toDelete = new File(commentPath);
                if(toDelete.exists()){
                    if(toDelete.delete()) {
                        Log.d("delete", "deleted");
                    }
                    else {
                        Log.d("delete", "not deleted");
                    }
                }
            }
        }
        exoPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = AssignmnetApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
   //endregion

    //region saving Video
    private void saveVideoToGallery() {
        String timeStamp = null;
        saved = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File mVideoFolder = new File(movieFile, "Assignment Videos");
                if (!mVideoFolder.exists()) {
                   if(mVideoFolder.mkdir()){
                       Log.d("folder","created");
                   }
                }
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                String prepend = "VIDEO" + timeStamp + "_";
                try {
                    videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    InputStream in = new FileInputStream(new File(videoFileName));
                    OutputStream out = new FileOutputStream(videoFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                catch (IOException e) {
                    Log.e("exception","Unable to read file",e);
                }
            }
            else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getContext(), "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        }
        else {
            File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File mVideoFolder = new File(movieFile, "Assignment Videos");
            if (!mVideoFolder.exists()) {
                if(mVideoFolder.mkdir()){
                    Log.d("folder","created");
                }
            }
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
            String prepend = "VIDEO" + timeStamp + "_";
            try {
                videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
            }
            catch (IOException e) {
                Log.d("exception","unable to create file",e);
            }
            try {
                InputStream in = new FileInputStream(new File(videoFileName));
                OutputStream out = new FileOutputStream(videoFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            catch (IOException e) {
                Log.d("exception","unable to read file",e);
            }
        }
        bgRealm  = Realm.getDefaultInstance();
        videoInfo.setVideoPath(videoFile.getAbsolutePath());
        videoInfo.setId(timeStamp);
        bgRealm.beginTransaction();
        bgRealm.insertOrUpdate(videoInfo);
        bgRealm.commitTransaction();
        Log.d("realm",bgRealm.where(VideoInfo.class).findAll().toString());
    }
    //endregion


    //region SettingComments
    public void setComments() {
        if (videoInfo.getCommentPath().size()!=0) {
            RealmList<String> pathList = videoInfo.getCommentPath();
            RealmList<Float> xPosList = videoInfo.getxPosList();
            RealmList<Float> yPosList = videoInfo.getyPosList();
            int j =1;
            for (String path : pathList) {
                File bitFile = new File(path);
                if (bitFile.exists() && (j>=i) ){
                   final ImageView imageView = new ImageView(getContext());
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    imageView.setLayoutParams(layoutParams);
                    imageView.setClickable(true);
                    if(previewFromGallery ==1) {
                        imageView.setX(xPosList.get(j-1));
                        imageView.setY(yPosList.get(j-1));
                    }
                    imageView.setOnTouchListener(onTouchListener);
                    Log.d("padding",String.valueOf(i));
                    Bitmap bitmap = BitmapFactory.decodeFile(bitFile.getPath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        relativeLayout.addView(imageView);
                    }
                    i++;
                }
                j++;
            }
        }
    }
    //endregion
}
