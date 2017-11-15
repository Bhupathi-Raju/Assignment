package com.example.zemoso.assignment.activities.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.utils.AssignmnetApplication;
import com.example.zemoso.assignment.activities.interfaces.SupportActionBar;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CAMERA_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    //region Variables
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private TextureView textureView;
    private ImageButton mrecorder;
    private Chronometer mchronometer;
    private boolean isRecording =false;
    private File mVideoFolder;
    private String mVideoFileName;
    private CameraDevice mcameraDevice;
    private String mCameraId;
    private Size mPreviewSize;
    private int mTotalRotation;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    private SupportActionBar msupportActionBar;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    //endregion

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(){
        return new CameraFragment();
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            setupCamera(i, i1);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    //region CameraDeviceStateCallback
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mcameraDevice = cameraDevice;
            Log.d("recording state",String.valueOf(isRecording));
            if(isRecording){
                try {
                    createVideoFileName();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("record1","recording");
                startRecord();
                mMediaRecorder.start();

                        mchronometer.setBase(SystemClock.elapsedRealtime());
                        mchronometer.setVisibility(View.VISIBLE);
                        mchronometer.start();

            }else{
                startPreview();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mcameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mcameraDevice = null;
        }
    };
    //endregion


    private void setupCamera(int width, int height) {
        CameraManager mCameraManager = (CameraManager) getContext().getSystemService(CAMERA_SERVICE);
        try {
               String[] cameraIdList = mCameraManager.getCameraIdList();
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing!=null&&lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                 mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotedHeight = height;
                if (swapRotation) {
                    rotedHeight = width;
                    rotatedWidth = height;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotedHeight);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(),"You cannot use this app",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[1] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(),"app doesn't record audio",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(CAMERA_SERVICE);
        try {
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)==
                         PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                 getActivity(),Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                }
                else{
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(getActivity(),"Requires Access to Camera",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
                    }
                }
                }
             else {
                 cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
             }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void startRecord() {
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mcameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mcameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(),null,null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                              Log.d("configuration","failed");
                        }
                    },null);
        } catch (IOException | CameraAccessException e) {
           Log.e("error","Exception",e);
        }
    }


    private void startPreview(){
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mcameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mcameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),null,mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                              Toast.makeText(getActivity(),"unable to setup Camera",Toast.LENGTH_SHORT).show();
                        }
                    },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mcameraDevice != null) {
            Log.d("camera","closed");
            mcameraDevice.close();
            mcameraDevice = null;
        }
    }


    private static class CompareSizeByArea implements Comparator<Size>{
        @Override
        public int compare(Size size, Size t1) {
            return Long.signum((long)size.getWidth()*size.getHeight()/(long) t1.getWidth()*t1.getHeight());
        }
    }

    //region LifeCycle Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        boolean created = createVideoFolder();
        textureView = view.findViewById(R.id.textureView);
        mchronometer = view.findViewById(R.id.chronometer);
        mrecorder = view.findViewById(R.id.record);
        mMediaRecorder = new MediaRecorder();
        mrecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    mchronometer.stop();
                    mchronometer.setVisibility(View.INVISIBLE);
                    Log.d("recording2","toggling");
                    isRecording = false;
                    mrecorder.setImageResource(R.drawable.ic_action_name);
                    mMediaRecorder.stop();
                    Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                    getActivity().sendBroadcast(mediaStoreUpdateIntent);
                    mMediaRecorder.reset();
                    Fragment previewFragment = PreviewFragment.newInstance(mVideoFileName,0);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,previewFragment,"preview").
                            addToBackStack(null).commit();
                    closeCamera();
                }
                else {
                    isRecording = true;
                    mrecorder.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    try {
                        createVideoFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startRecord();
                    mMediaRecorder.start();
                    mchronometer.setBase(SystemClock.elapsedRealtime());
                    mchronometer.setVisibility(View.VISIBLE);
                    mchronometer.start();
                }
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
        startBackgroundThread();
        if (textureView.isAvailable()) {
            setupCamera(textureView.getWidth(), textureView.getHeight());
            connectCamera();
        } else
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);

    }

    @Override
    public void onPause() {
        Log.d("camera","closed");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = AssignmnetApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
    //endregion



    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Assignment");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        Integer sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) %360;
    }

    private static Size chooseOptimalSize(Size[] choices,int width, int height){
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices)
        {
            if(option.getHeight() == option.getWidth()*height/width &&
                    option.getWidth() >= width &&
                    option.getHeight()>=height
                    ){
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0 ){
            return Collections.min(bigEnough,new CompareSizeByArea());
        }else{
            return choices[0];
        }
    }

    //region Saving the recorded video in Cache Directory
    private boolean createVideoFolder() {
        mVideoFolder = new File(getContext().getCacheDir(), "Assignment Videos");
        return !mVideoFolder.exists() && mVideoFolder.mkdir();
    }

    private void createVideoFileName() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String prepend = "VIDEO" + timeStamp + "_";
        File videoFile = File.createTempFile(prepend,".mp4",mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
    }
    //endregion

    //region Setting up Recorder
    private void setupMediaRecorder() throws IOException{
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         mMediaRecorder.setOutputFile(mVideoFileName);
         mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
         mMediaRecorder.setVideoEncodingBitRate(1000000);
         mMediaRecorder.setVideoFrameRate(30);
         mMediaRecorder.setVideoSize(mVideoSize.getWidth(),mVideoSize.getHeight());
         mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
         mMediaRecorder.setOrientationHint(mTotalRotation);
         mMediaRecorder.prepare();

    }
    //endregion

}

