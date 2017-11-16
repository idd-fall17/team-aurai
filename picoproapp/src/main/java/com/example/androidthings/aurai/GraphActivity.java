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

public class GraphActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();





    //open source chart library to use to create graphs
    //https://github.com/PhilJay/MPAndroidChart

    //weather API options. Most free for developer accounts
    //Need hourly forecast and likely a 5 day forecast
    //https://superdevresources.com/weather-forecast-api-for-developing-apps/




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Screen initialization */
        setContentView(R.layout.graphlayout);
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
