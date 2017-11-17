package com.example.androidthings.aurai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by MichaelOudenhoven on 11/16/17.
 */

public class RoomActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();


    private TextView percentOpen;


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


        setupButtons();

        //hide action bar
        getActionBar().hide();


    }


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


        ImageButton openButton = (ImageButton) findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "open windows clicked");

                //TODO: send bluetooth signal to open all of the windows to 100%
            }

        });

        ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "close windows clicked");

                //TODO: send bluetooth signal to close all windows to 0%
            }

        });

    }

}
