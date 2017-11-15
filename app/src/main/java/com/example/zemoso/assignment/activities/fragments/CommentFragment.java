package com.example.zemoso.assignment.activities.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zemoso.assignment.R;
import com.example.zemoso.assignment.activities.adapters.ColorPickerAdapter;
import com.example.zemoso.assignment.activities.model.VideoInfo;
import com.example.zemoso.assignment.activities.utils.AssignmnetApplication;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends Fragment implements ColorPickerAdapter.TextColorPicker{


    //region Interface to setComments
    public interface SetCommentsOnVideo{
         void setComments();
    }
    //endregion

    public CommentFragment() {
        // Required empty public constructor
    }


    public static CommentFragment newInstance(VideoInfo videoInfo) {

        CommentFragment commentFragment = new CommentFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("videoInfo",videoInfo);
        commentFragment.setArguments(bundle);
        return commentFragment;
    }

    //region Variables
    private EditText mEditText;
    private ImageButton mDone;
    private VideoInfo videoInfo;
    private String timeStamp;
    private int color;
    private File bitFile;
    private RecyclerView recyclerView;
    private View rootView;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;
    private SetCommentsOnVideo setCommentsOnVideo;
    private static final String TAG = CommentFragment.class.getSimpleName();
    //endregion


    //region Lifecycle Methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity) {
            setCommentsOnVideo = (SetCommentsOnVideo) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        videoInfo = (VideoInfo) this.getArguments().getSerializable("videoInfo");
        final View view =  inflater.inflate(R.layout.fragment_comment, container, false);
        mEditText = view.findViewById(R.id.comment);
        mEditText.requestFocus();
        ColorPickerAdapter.TextColorPicker textColorPicker = this;
        mDone = view.findViewById(R.id.done);
        recyclerView = view.findViewById(R.id.color_picker);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getContext(),textColorPicker);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(colorPickerAdapter);
        final InputMethodManager inputMethodManager =
                (InputMethodManager)getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        mDone.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view1) {
                inputMethodManager.hideSoftInputFromWindow(view1.getWindowToken(),0);
                mDone.setVisibility(View.GONE);
                String comment = mEditText.getText().toString();
                if(!comment.equals("")) {
                    mEditText.setVisibility(View.GONE);
                    Log.d(TAG, comment);
                    TextView textView = new TextView(getContext());
                    Rect bounds = new Rect();
                    DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                    float dp = 20f;
                    float fpixels = metrics.density * dp;
                    int pixels = (int) (fpixels + 0.5f);
                    textView.setTextSize(pixels);
                    if(color!=0)
                    textView.setTextColor(color);
                    else
                        textView.setTextColor(Color.RED);
                    textView.setText(comment);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setDrawingCacheEnabled(true);
                    textView.measure(metrics.widthPixels,metrics.heightPixels);
                    int width = textView.getMeasuredWidth();
                    textView.layout(0, 0,width,textView.getMeasuredHeight());
                    Log.d(TAG,String.valueOf(bounds.width()));
                    Log.d(TAG,String.valueOf(textView.getWidth()));
                    Log.d(TAG,String.valueOf(bounds.height()));
                    Bitmap textImage = Bitmap.createBitmap(textView.getDrawingCache(true));
                    try {
                        File bitmapFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                        File mBitFolder = new File(bitmapFile, "Assignment Videos");
                        if (!mBitFolder.exists()) {
                           if(mBitFolder.mkdir()){
                               Log.d("folder","created");
                           }
                        }
                        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String prepend = "Bitmap" + timeStamp + "_";
                        try {
                            bitFile = File.createTempFile(prepend, ".png", mBitFolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textImage.compress(Bitmap.CompressFormat.PNG,100,new FileOutputStream(bitFile));
                        videoInfo.setCommentPath(bitFile.getAbsolutePath());
                        setCommentsOnVideo.setComments();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            }
        });

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                    if(!b) {
                        Log.d(TAG, "focus changed");
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 if(charSequence.length()!=0)
                 {
                     mDone.setVisibility(View.VISIBLE);
                 }
                 if(charSequence.length()==0)
                     mDone.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        rootView = view.findViewById(R.id.root_view);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
          globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    Log.d(TAG,"Keyboard shown");
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)recyclerView.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW,R.id.comment);
                    recyclerView.setLayoutParams(params);
                    //rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if(keypadHeight == 0){
                    Log.d(TAG,String.valueOf(keypadHeight));
                   RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)recyclerView.getLayoutParams();
                    params.removeRule(RelativeLayout.BELOW);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    recyclerView.setLayoutParams(params);
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = AssignmnetApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    //endregion

    //region Interface Methods
    @Override
    public void setColor(int color) {
        mEditText.setTextColor(color);
        this.color = color;
    }
    //endregion

}

