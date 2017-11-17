package com.example.androidthings.aurai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

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

        setupButtons();

        //hide action bar
        getActionBar().hide();


    }





    /**
     * Sets up all the buttons in the view with on click listeners
     */
    private void setupButtons() {
        //back arrow button
        ImageButton backButton = (ImageButton) findViewById(R.id.backButtonRoom);
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "back clicked");

                //go back to previous activity that called it (home)
                finish();
            }

        });


        Button roomTempButton = (Button) findViewById(R.id.roomTempButtonGraph);
        roomTempButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "room Temp clicked");

                //TODO: switch graph to room temeperature view

            }

        });

        Button setPointTempButton = (Button) findViewById(R.id.setTempButtonGraph);
        setPointTempButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "set Point clicked");

                //TODO: switch graph to set point temeperature view

            }

        });

        Button outdoorTempButton = (Button) findViewById(R.id.outdoorTempGraph);
        outdoorTempButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "outdoor temp clicked");

                //TODO: switch graph to outdoor temeperature view

            }

        });

    }


}
