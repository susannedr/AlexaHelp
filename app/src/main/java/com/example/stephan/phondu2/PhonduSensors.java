package com.example.stephan.phondu2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.stephan.phondu2.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhonduSensors extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "PhonduSensors";

    private static final int UI_ANIMATION_DELAY = 300;

    private boolean mIsPlaying;
    private View mContentView;

    private final Handler mHideHandler = new Handler();
    private MediaPlayer player;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private ImageView mStatusView;

    private SensorManager mSensorManager;
    private Sensor mLinearAccSensor;
    private Sensor mAccSensor;
    private Sensor mGravitySensor;

    private double mPeak = 0;
    private double mLow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "new1");
        setContentView(R.layout.activity_phondu_touch);

        mContentView = findViewById(R.id.fullscreen_content);

        mStatusView = (ImageView) findViewById(R.id.status_view);

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );


        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAlarmStop();
            }
        });
//        Log.d(TAG, "No long click listener");

        mContentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onAlarmStart();
                return true;
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLinearAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (mLinearAccSensor != null){
            Log.d(TAG, "Sensor.TYPE_LINEAR_ACCELERATION found!");
        }
        else {
            Log.d(TAG, "Sensor.TYPE_LINEAR_ACCELERATION not available!");
        }
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccSensor != null){
            Log.d(TAG, "Sensor.TYPE_ACCELEROMETER found!");
        }
        else {
            Log.d(TAG, "Sensor.TYPE_ACCELEROMETER not available!");
        }
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (mGravitySensor != null){
            Log.d(TAG, "Sensor.TYPE_GRAVITY found!");
        }
        else {
            Log.d(TAG, "Sensor.TYPE_GRAVITY not available!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mLinearAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
        registerReceiver(onBattery, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        registerReceiver(onBattery, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        registerReceiver(onBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mSensorManager.unregisterListener(this);
        unregisterReceiver(onBattery);
    }


    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "Menu key pressed");
                return true;
            case KeyEvent.KEYCODE_SEARCH:
                Log.d(TAG, "Search key pressed");
                return true;
            case KeyEvent.KEYCODE_HOME:
                Log.d(TAG, "Home key pressed");
                return false;
            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "Back key pressed");
                //onBackPressed();
                onAlarmStop();
                mStatusView.setImageResource(R.drawable.reserve);
                mStatusView.setVisibility(View.VISIBLE);

                return false;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d(TAG, "Volumen Up pressed");
                onAlarmStart();
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(TAG, "Volumen Down pressed");
                onAlarmStart();
                return false;
            case KeyEvent.KEYCODE_POWER:
                Log.d(TAG, "Power key pressed");
                onAlarmStart();
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onAlarmStart() {
        Log.d(TAG, "onAlarmStart");
        //Uri tone = Settings.System.DEFAULT_ALARM_ALERT_URI;
        //Uri tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (player != null) {
            player.stop();
            player.reset();
        }
        player = MediaPlayer.create(this, R.raw.beep);
        player.setLooping(true);
        player.start();
    }

    private void onAlarmStop() {
        Log.d(TAG, "onAlarmStop");
        if (player != null) {
            player.stop();
            mIsPlaying = false;
            player.reset();
        }
    }

    float prevX = 0;
    float prevY = 0;
    float prevZ = 0;
    int mKnockCount = 0;
    int mSilentCount = 0;
    int mDebounceCount = 0;
    boolean mPeekDetected = false;
float mPeakAmp;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float absAccSum = Math.abs(x) + Math.abs(y) + Math.abs(z);
            Log.d(TAG, event.sensor.getName() + x + ", " + y + ", " + z);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float dx = prevX - x;
            float dy = prevY - y;
            float dz = prevZ - z;
            prevX = x;
            prevY = y;
            prevZ = z;
            Log.d(TAG, "Data: " + x + ", " + y + ", " + z);
            float derivation = dx * dx + dy * dy + dz * dz;
            Log.d(TAG, "Derivation: " + derivation);
            if (mPeekDetected) {
                mDebounceCount++;
                if (mDebounceCount < 15) {
                    if (derivation < 0.05) {
                        mPeekDetected = false;
                        mKnockCount  = 0;
                        Log.d(TAG, "knock canceled");
                    }
                } else {
                    if (mPeekDetected) {
                        Log.d(TAG, "knock confirmed");
                        mKnockCount++;
                        mSilentCount = 0;
                        mPeekDetected = false;
                    }
                }
            } else {
                mSilentCount++;
                if (mSilentCount > 40) {
                    if (derivation > 4) {
                        Log.d(TAG, "peak detected: " + derivation);
                        mPeekDetected = true;
                        mDebounceCount = 0;
                        mPeakAmp = derivation;
                    }
                }
                if (mSilentCount > 200) {
                    mKnockCount = 0;
                }
            }
            Log.d(TAG, "mKnockCount: " + mKnockCount);
            Log.d(TAG, "mSilentCount: " + mSilentCount);
            if (mKnockCount == 3) {
                Log.d(TAG, "mKnockCount: alarm");
                onAlarmStart();
                mKnockCount = 0;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d(TAG, "Gravity: " + x + " | " + y + " | " + z + " | ");
        } else {
            Log.d(TAG, "SensorType: " + event.sensor.getName());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (accuracy) {
            case  SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                Log.d(TAG, "SENSOR_STATUS_ACCURACY_LOW");
                break;
            case  SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                Log.d(TAG, "SENSOR_STATUS_ACCURACY_MEDIUM");
                break;
            case  SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.d(TAG, "SENSOR_STATUS_ACCURACY_HIGH");
                break;
            case  SensorManager.SENSOR_STATUS_UNRELIABLE:
                Log.d(TAG, "SENSOR_STATUS_UNRELIABLE");
                break;
            default:
                Log.d(TAG, "SENSOR_STATUS_UNDEFINED");
        }
    }


    BroadcastReceiver onBattery=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // Are we charging / charged?
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            Log.d(TAG, "onBattery " + status + " chargeplug " + chargePlug);
            Drawable drawable;
            if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB || chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                Log.d(TAG, "BATTERY_STATUS_CHARGING");
                mStatusView.setImageResource(R.drawable.charging);
                mStatusView.setVisibility(View.VISIBLE);
            } else if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                Log.d(TAG, "BATTERY_STATUS_CHARGING");
                mStatusView.setImageResource(R.drawable.charging);
                mStatusView.setVisibility(View.VISIBLE);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                Log.d(TAG, "BATTERY_STATUS_FULL");
                mStatusView.setImageResource(R.drawable.full);
                mStatusView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "BATTERY_STATUS NOT charging");
                mStatusView.setVisibility(View.INVISIBLE);
            }
        }
    };
}

