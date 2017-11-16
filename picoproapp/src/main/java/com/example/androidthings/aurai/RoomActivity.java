package com.example.androidthings.aurai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by MichaelOudenhoven on 11/16/17.
 */

public class RoomActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /* Screen initialization */
        setContentView(R.layout.roomlayout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        Log.d(TAG, "Display height in pixels: "+ dm.heightPixels);
//        Log.d(TAG, "Display width in pixels: "+ dm.widthPixels);
//        Log.d(TAG, "Display density in dpi: "+ dm.densityDpi);

        //hide action bar
        getActionBar().hide();


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}
