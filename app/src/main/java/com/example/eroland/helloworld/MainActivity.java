package com.example.eroland.helloworld;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocket.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = (LineChart) findViewById(R.id.chart);
        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        LineData data = new LineData();

        // add empty data
        mChart.setData(data);

        conectarServidor();
    }

    public void conectarServidor(){
        AsyncHttpClient.getDefaultInstance().websocket("http://192.168.0.2:8080/", null, new WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                webSocket.setStringCallback(new StringCallback() {
                    public void onStringAvailable(String s) {
                        //System.out.println(s);
                        try {
                            JSONObject jObject = new JSONObject(s);
                            int z = jObject.getInt("accZ");
                            addEntry((float) z);
                        }catch (JSONException e){
                            //TODO
                        }
                    }
                });
            }
        });
    }

    private void addEntry(final float accZ){
        mChart.getHandler().post(new Runnable() {
            @Override
            public void run() {
                LineData data = mChart.getData();
                if (data != null) {
                    //System.out.println(accZ);
                    ILineDataSet set = data.getDataSetByIndex(0);

                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }
                    data.addXValue("test");
                    data.addEntry(new Entry(accZ,set.getEntryCount()),0);
                    // let the chart know it's data has changed
                    mChart.notifyDataSetChanged();

                    // limit the number of visible entries
                    mChart.setVisibleXRangeMaximum(120);
                    // mChart.setVisibleYRange(30, AxisDependency.LEFT);

                    // move to the latest entry
                    //
                    mChart.moveViewToX(data.getXValCount() - 121);
                    //mChart.invalidate();
                }
            }
        });
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "MPU-6050 Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setDrawValues(false);
        set.setDrawCircles(false);
        return set;
    }
}
