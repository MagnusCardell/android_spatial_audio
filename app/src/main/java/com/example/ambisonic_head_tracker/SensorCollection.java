package com.example.ambisonic_head_tracker;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class SensorCollection extends AppCompatActivity implements SensorEventListener {
    //Sensor managers
    private SensorManager systemSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    //Sensor variables
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    //Parent
    FullscreenActivity mainAct;

    //public void init(FullscreenActivity origin){
    //    this.mainAct = origin;

   //}

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        systemSensorManager = (android.hardware.SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = systemSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = systemSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            android.hardware.SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            android.hardware.SensorManager.getOrientation(mR, mOrientation);
        }
        //mainAct.updateRotation(this.mOrientation, this.mR);


        //Intent replyIntent = new Intent();
        returnReply();
    }

    public void returnReply() {
        float[] reply = mR;
        Intent replyIntent = new Intent();
        replyIntent.putExtra("MATRIXKEY", reply);
        setResult(RESULT_OK, replyIntent);
        finish();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        systemSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        systemSensorManager.registerListener(this, mAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        systemSensorManager.registerListener(this, mMagnetometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
