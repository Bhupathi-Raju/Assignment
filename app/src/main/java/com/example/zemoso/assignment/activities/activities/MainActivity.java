package com.example.zemoso.assignment.activities.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.adapters.VideoGalleryAdapter;
import com.example.zemoso.assignment.activities.fragments.CameraFragment;
import com.example.zemoso.assignment.activities.fragments.CommentFragment;
import com.example.zemoso.assignment.activities.fragments.GalleryFragment;
import com.example.zemoso.assignment.activities.fragments.PreviewFragment;
import com.example.zemoso.assignment.activities.interfaces.SupportActionBar;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements
                                VideoGalleryAdapter.VideoPreviewInterface,
                                CommentFragment.SetCommentsOnVideo,
                                SupportActionBar {

    //region variables
    private static final String TAG = MainActivity.class.getSimpleName() ;
    //endregion

    //region LifeCycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Fragment galleryFragment = GalleryFragment.newInstance();
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fragmentContainer,galleryFragment,"gallery").
                addToBackStack("gallery").
                commit();
        Button camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment cameraFragment = CameraFragment.newInstance();
                        getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.fragmentContainer,cameraFragment)
                        .addToBackStack("camera")
                        .commit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.close();
    }

    //endregion

    //region Interface Methods
    @Override
    public void startPreview(String videoFileName) {
        Fragment previewFragment = PreviewFragment.newInstance(videoFileName,1);
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fragmentContainer,previewFragment).
                addToBackStack(null)
                .commit();
    }


    @Override
    public void setComments() {
         PreviewFragment previewFragment = (PreviewFragment) getSupportFragmentManager().findFragmentByTag("preview");
         previewFragment.setComments();
    }

    @Override
    public void hideSupportActionBar() {
        getSupportActionBar().hide();
    }

    @Override
    public void showSupportActionBar() {
        getSupportActionBar().show();
    }
    //endregion
}
