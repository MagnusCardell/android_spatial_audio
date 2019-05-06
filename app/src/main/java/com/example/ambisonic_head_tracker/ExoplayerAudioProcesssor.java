package com.example.ambisonic_head_tracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.ext.gvr.GvrAudioProcessor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

public class ExoplayerAudioProcesssor extends AppCompatActivity implements SensorEventListener{

    GvrAudioProcessor gvrAudioProcessor;
    private SimpleExoPlayer player;
    //Sensor managers
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //setup sensor data
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Setup media interaction
        TrackSelector trackSelector = new DefaultTrackSelector();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this) {
            @Override
            public AudioProcessor[] buildAudioProcessors() {
                gvrAudioProcessor = new GvrAudioProcessor();
                return new AudioProcessor[] {gvrAudioProcessor};
            }
        };
        player = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector);

        PlayerView playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Ambisonic_head_tracker"));
        Uri audioSourceUri = Uri.parse("file:///android_asset/hungarian_dance.mp3");
        MediaSource audioSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(audioSourceUri);
        player.prepare(audioSource);
        player.setPlayWhenReady(true);
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
            SensorManager.getOrientation(mR, mOrientation);
            //update UI
            TextView xView = (TextView) findViewById(R.id.x_c);
            TextView yView = (TextView) findViewById(R.id.y_c);
            TextView zView = (TextView) findViewById(R.id.z_c);

            xView.setText(String.format(Locale.getDefault(), "%.2f", mR[0]));
            yView.setText(String.format(Locale.getDefault(), "%.2f", mR[1]));
            zView.setText(String.format(Locale.getDefault(), "%.2f", mR[2]));
        }
        if (gvrAudioProcessor != null) {
            //headTransform.getQuaternion(headOrientation, 0);
            gvrAudioProcessor.updateOrientation(mR[0], mR[1],
                    mR[2], mR[3]);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this);
        player.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
