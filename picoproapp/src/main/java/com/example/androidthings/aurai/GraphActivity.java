package com.example.androidthings.aurai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MichaelOudenhoven on 11/16/17.
 */

public class GraphActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    //line chart for the data
    private LineChart chart;
    //chart data
    private LineDataSet chartData;


    //data for room temp, setpoint, and outdoor temp
    private float[] roomTempData;
    private float[] setpointTempData;
    private float[] outdoorTempData;

    //weather type image view
    private ImageView weatherTypeImage;


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

        //get graph from XML
        chart = findViewById(R.id.lineChart);
        //setup the graph with initial data
        setupGraph();

        //set setpoint temp to correct value
        Button setpointTemp = (Button) findViewById(R.id.setTempButtonGraph);
        setpointTemp.setText(Integer.toString(Constants.setPointTemp));

        Button outDoorTempButton = (Button) findViewById(R.id.outdoorTempGraph);
        String temperature = Integer.toString(Constants.outdoorTemp);
        outDoorTempButton.setText(temperature);

        Button roomTempButton = (Button) findViewById(R.id.roomTempButtonGraph);
        String roomTemperature = Integer.toString(Constants.roomTemp);
        roomTempButton.setText(roomTemperature);

        weatherTypeImage = findViewById(R.id.weatherTypeImageGraph);
        setWeatherType(Constants.weatherTypeString);
    }





    /**
     * Sets up all the buttons in the view with on click listeners
     */
    private void setupButtons() {
        //back arrow button
        ImageButton backButton = (ImageButton) findViewById(R.id.backButtonGraph);
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "back clicked");

                //go back to previous activity that called it (home)
                finish();
            }

        });


//        Button roomTempButton = (Button) findViewById(R.id.roomTempButtonGraph);
//        roomTempButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                Log.d(TAG, "room Temp clicked");
//
//                //create LineData using the room temp and set to global variable
//
//
//                refreshGraph();
//            }
//
//        });
//
//        Button setPointTempButton = (Button) findViewById(R.id.setTempButtonGraph);
//        setPointTempButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                Log.d(TAG, "set Point clicked");
//
//                //create LineData using the setpoint temp and set to global variable
//
//
//                refreshGraph();
//            }
//
//        });
//
//        Button outdoorTempButton = (Button) findViewById(R.id.outdoorTempGraph);
//        outdoorTempButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                Log.d(TAG, "outdoor temp clicked");
//
//                //create LineData using the outdoor temp and set to global variable
//
//                refreshGraph();
//            }
//
//        });

    }

    /**
     * Sets up the graph with the room temperature data over time
     */
    private void setupGraph() {

        //TODO: customize chart look
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawLimitLinesBehindData(false);
        chart.getAxisLeft().setDrawLimitLinesBehindData(false);
        chart.getAxisLeft().setTextSize(16);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawLimitLinesBehindData(false);
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setTextSize(16);
        chart.getXAxis().setTextSize(Color.WHITE);
//        chart.setDescription(new Description(""));
//        chart.setDescription("");
        chart.setDrawBorders(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.setBorderWidth(5);
        chart.setBorderColor(Color.WHITE);
        



        //placeholder data to test chart
        int[] x = new int[]{1,2,3,4,5,6,7};
        int[] y = new int[]{15, 16, 16, 14, 13, 10, 8};

        int[] x2 = new int[]{1,2,3,4,5,6,7};
        int[] y2 = new int[]{20,20,20,20,18,18,18};


        List<Entry> chartEntries1 = new ArrayList<Entry>();
        List<Entry> chartEntries2 = new ArrayList<Entry>();


        for (int i = 0; i < x.length; i++) {

            chartEntries1.add(new Entry(x[i], y[i]));
            chartEntries2.add(new Entry(x2[i], y2[i]));
        }

        Collections.sort(chartEntries1, new EntryXComparator());
        Collections.sort(chartEntries2, new EntryXComparator());


        chartData = new LineDataSet(chartEntries1, "Outdoor Temperature");
        chartData.setAxisDependency(YAxis.AxisDependency.LEFT);
        chartData.setDrawValues(false);
        chartData.setColor(Color.RED);
        chartData.setCircleColor(Color.RED);

        LineDataSet chartData2 = new LineDataSet(chartEntries2, "Setpoint Temperature");
        chartData2.setAxisDependency(YAxis.AxisDependency.LEFT);
        chartData2.setDrawValues(false);


        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(chartData);
        dataSets.add(chartData2);


        //set x-axis labels
        final HashMap<Integer, String> xLabel = new HashMap<>();
        xLabel.put(1, "2pm");
        xLabel.put(2, "4pm");
        xLabel.put(3, "6pm");
        xLabel.put(4, "8pm");
        xLabel.put(5, "10pm");
        xLabel.put(6, "12am");
        xLabel.put(7, "2am");

        XAxis xAxis = chart.getXAxis();

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xLabel.get((int)value);
            }



        });




        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();



    }

    /**
     * Takes new chart data and updates the graph. This will switch the chart between indoor,
     * outdoor, and setpoint changes.
     */
    private void refreshGraph() {

        //TODO: pull updated data from the server and create new data set



        //refreshes the chart
        chart.invalidate();
    }

    /**
     * Takes string of weather type from weather API and changes the image of the outside weather
     * type on the pico
     * @param type weather type as a string
     */
    public void setWeatherType(String type) {
        Log.d(TAG, "setting weather type");
        Log.d(TAG, "weather type " + type);

        switch (type) {
            case "Clear": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.sunny, null);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            case "Rain": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.rain_cloud, null);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

            case "Clouds": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.cloudy, null);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            default: {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.partly_cloudy, null);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

        }
    }

}
