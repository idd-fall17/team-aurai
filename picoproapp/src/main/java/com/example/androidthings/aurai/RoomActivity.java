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
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by MichaelOudenhoven on 11/16/17.
 */

public class RoomActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();




    //seek bar for window position
    private SeekBar seekBar;
    private Button seekBarPercentButton;
    //private TextView seekBarPercent;
    private int seekBarSetPoint = 0;

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


        //setup buttons and seekbar
        setupButtons();
        setupSeekBar();


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
                //TODO: show progress on screen of windows opening







                //update percentage and seek bar to meet value
                seekBarSetPoint = 100;
                //set to previous percentage - may have changed from bluetooth call from feather
                seekBarPercentButton.setText(Integer.toString(seekBarSetPoint)+ "%");

                //get seekbar location and set it to the correct place on the slider
                int seekLocation = seekBarSetPoint/10;
                seekBar.setProgress(seekLocation);
                seekBar.setVisibility(View.INVISIBLE);


            }

        });

        ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "close windows clicked");

                //TODO: send bluetooth signal to close all windows to 0%
                //TODO: show progress on screen of windows opening







                seekBarSetPoint = 0;
                //set to previous percentage - may have changed from bluetooth call from feather
                seekBarPercentButton.setText(Integer.toString(seekBarSetPoint)+ "%");

                //get seekbar location and set it to the correct place on the slider
                int seekLocation = seekBarSetPoint/10;
                seekBar.setProgress(seekLocation);
                seekBar.setVisibility(View.INVISIBLE);
            }

        });


        //percent button to open up seek bar
        seekBarPercentButton = (Button) findViewById(R.id.windowAdjustButton);
        seekBarPercentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if seekbar is currently not shown - show it
                if (seekBar.getVisibility() == View.INVISIBLE) {
                    //set to previous percentage - may have changed from bluetooth call from feather
                    seekBarPercentButton.setText(Integer.toString(seekBarSetPoint)+ "%");

                    //get seekbar location and set it to the correct place on the slider
                    int seekLocation = seekBarSetPoint/10;
                    seekBar.setProgress(seekLocation);

                    //make appear on click
                    seekBar.setVisibility(View.VISIBLE);


                }
                //seek bar is currently up turn it off
                else {
                    seekBar.setVisibility(View.INVISIBLE);

                }
            }
        });

    }



    /**
     * Sets up the seek bar to move in 10% increments to open the windows. Includes listener methods
     * for when the seek bar is moved.
     */
    private void setupSeekBar() {
//        seekBarPercent = findViewById(R.id.windowPercent);
        //set to startup value
        seekBarPercentButton.setText(Integer.toString(seekBarSetPoint)+ "%");


        //TODO: if time customize the color of the seekbar to be more noticeable



        seekBar = findViewById(R.id.windowSeekRoom);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.d(TAG, Integer.toString(i));
                seekBarSetPoint = i*10;
                seekBarPercentButton.setText(Integer.toString(seekBarSetPoint)+ "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //don't need to do anything here as of now
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //TODO: send bluetooth characteristic
                //TODO: pop up view saying the window is being moved


                //TODO: if time make timeout rather than invisible right away
                //hide once done changing
                seekBar.setVisibility(View.INVISIBLE);


            }
        });


        //hide on startup
        seekBar.setVisibility(View.INVISIBLE);
    }

}
