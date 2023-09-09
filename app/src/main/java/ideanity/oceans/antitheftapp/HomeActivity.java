package ideanity.oceans.antitheftapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    Switch motionSwitch, proximitySwitch, chargerSwitch;
    MediaPlayer mediaPlayer;
    CountDownTimer cdt;
    CountDownTimer proximityTimer;
    private SensorManager sensorMan;
    private SensorManager mSensorManager;

    private Sensor mSensor;
    private Sensor accelerometer;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    AlertDialog alertDialog;
    private static final int SENSOR_SENSITIVITY = 4;

    TextView rememberPassword;

    int mSwitchSet, pSwitchSet = 0;
    int chargerFlag, chargerFlag1, chargerFlag2 = 0;
    boolean headsetDetectionActivated = false; // Track if headset detection is activated
    boolean isHeadsetConnected = false; // Track whether a headset is connected

    BroadcastReceiver headsetReceiver;
    boolean headsetFlag = false;
    AudioManager audioManager;

    // Add a boolean variable to track the current activity
    boolean isInHeadsetActivity = false;

    // Add a field to track whether motion detection alert is active
    private boolean isMotionAlertActive = false;

    @Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        registerHeadsetReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
        mSensorManager.unregisterListener(this);
        unregisterHeadsetReceiver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        // Check for headset permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.MODIFY_AUDIO_SETTINGS},
                    1);
        }

        // Update your onClickListener for the "headset" activity to set isInHeadsetActivity
        findViewById(R.id.headset1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHeadsetConnected) {
                    // Start the 'headset' activity when the headset is clicked
                    Intent intent = new Intent(HomeActivity.this, headset.class);
                    startActivity(intent);
                    // Set the flag to indicate that we are in the "headset" activity
                    isInHeadsetActivity = true;
                } else {
                    // Show a toast message to connect a headset
                    Toast.makeText(HomeActivity.this, "Connect a headset first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        alertDialog = new AlertDialog.Builder(this).create();
        chargerSwitch = findViewById(R.id.sCharger);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                    chargerFlag = 1;
                } else if (plugged == 0) {
                    chargerFlag1 = 1;
                    chargerFlag = 0;
                    func();
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);

        chargerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (chargerFlag != 1) {
                        Toast.makeText(HomeActivity.this, "Connect To Charger", Toast.LENGTH_SHORT).show();
                        chargerSwitch.setChecked(false);
                    } else {
                        Toast.makeText(HomeActivity.this, "Charger Protection Mode Activated", Toast.LENGTH_SHORT).show();
                        chargerFlag2 = 1;
                        func();
                    }
                } else {
                    chargerFlag2 = 0;
                }
            }
        });

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        motionSwitch = findViewById(R.id.sMotion);
        motionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    alertDialog.setTitle("Will Be Activated In 5 Seconds");
                    alertDialog.setMessage("00:5");

                    cdt = new CountDownTimer(5000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            alertDialog.setMessage("00:" + (millisUntilFinished / 1000));
                        }

                        @Override
                        public void onFinish() {
                            mSwitchSet = 1;
                            alertDialog.hide();
                            isMotionAlertActive = true; // Set the motion alert as active
                            Toast.makeText(HomeActivity.this, "Motion Detection Mode Activated", Toast.LENGTH_SHORT).show();
                        }
                    }.start();
                    alertDialog.show();
                    alertDialog.setCancelable(false);
                } else {
                    Toast.makeText(HomeActivity.this, "Motion Switch Off", Toast.LENGTH_SHORT).show();
                    mSwitchSet = 0;
                    isMotionAlertActive = false; // Set the motion alert as inactive when switched off
                }
            }
        });

        proximitySwitch = findViewById(R.id.sProximity);
        proximitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // Check if the proximity timer is not already running
                    if (proximityTimer == null) {
                        alertDialog.setTitle("Keep Phone In Your Pocket Before Activation");
                        alertDialog.setMessage("00:10");

                        proximityTimer = new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                alertDialog.setMessage("00:" + (millisUntilFinished / 1000));
                            }

                            @Override
                            public void onFinish() {
                                pSwitchSet = 1;
                                alertDialog.hide();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(new Intent(HomeActivity.this, PocketService.class));
                                } else {
                                    startService(new Intent(HomeActivity.this, PocketService.class));
                                }
                            }
                        }.start();
                        alertDialog.show();
                        alertDialog.setCancelable(false);
                    }
                } else {
                    // Stop and reset the proximity timer
                    if (proximityTimer != null) {
                        proximityTimer.cancel();
                        proximityTimer = null;
                    }
                    Toast.makeText(HomeActivity.this, "Proximity Switch Off", Toast.LENGTH_SHORT).show();
                    pSwitchSet = 0;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void func() {
        if (chargerFlag == 0 && chargerFlag1 == 1 && chargerFlag2 == 1) {
            if (isInHeadsetActivity) {
                // If we are in the "headset" activity, just finish it
                finish();
            } else {
                // If we are not in the "headset" activity, start the "EnterPin" activity
                startActivity(new Intent(HomeActivity.this, EnterPin.class));
            }
            chargerFlag2 = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 0.5) {
                if (mSwitchSet == 1) {
                    startActivity(new Intent(HomeActivity.this, EnterPin.class));
                    finish();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void registerHeadsetReceiver() {
        headsetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                int state;
                if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    state = intent.getIntExtra("state", -1);
                    if (state == 0) {
                        // Headset unplugged
                        isHeadsetConnected = false;
                        headsetFlag = false;
                        if (isInHeadsetActivity && headsetDetectionActivated) {
                            if (mediaPlayer == null) {
                                mediaPlayer = MediaPlayer.create(HomeActivity.this, R.raw.beepbeep);
                                mediaPlayer.setLooping(true);
                                mediaPlayer.start();
                            }
                        }
                    }
                    if (state == 1) {
                        // Headset plugged in
                        isHeadsetConnected = true;
                        headsetFlag = true;
                        if (isInHeadsetActivity && headsetDetectionActivated) {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        }
                        headsetDetectionActivated = true;
                    }
                }
            }
        };

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, receiverFilter);
    }

    private void unregisterHeadsetReceiver() {
        if (headsetReceiver != null) {
            unregisterReceiver(headsetReceiver);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void resetMotionAlert() {
        isMotionAlertActive = false;
        motionSwitch.setChecked(false); // Turn off the motion detection switch
        mSwitchSet = 0; // Reset the switch state
    }
}
