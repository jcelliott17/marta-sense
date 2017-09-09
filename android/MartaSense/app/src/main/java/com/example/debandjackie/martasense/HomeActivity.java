package com.example.debandjackie.martasense;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference thisCarNoiseReference;

    private SoundMeter soundMeter;

    private static final String THIS_CAR_ID = "car-1";
    private static final long soundInterval = 1000;
    private Timer soundCheckTimer;

    private static final int SOUND_REQUEST_CODE = 773;
    private boolean canRecordSound;

    private Switch shareNoiseLevelSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        thisCarNoiseReference = database.getReference().child("cars").child(THIS_CAR_ID).child("noise-level");
        thisCarNoiseReference.setValue("jackie-test");

        shareNoiseLevelSwitch = (Switch) findViewById(R.id.share_noise_level_switch);
        shareNoiseLevelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                canRecordSound = b;
                if (canRecordSound) {
                    requestSoundReporting();
                } else if (soundCheckTimer != null) {
                    soundCheckTimer.cancel();
                    soundCheckTimer.purge();
                }
            }
        });
    }

    private void requestSoundReporting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //check if permission request is necessary
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, SOUND_REQUEST_CODE);
        }
//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.RECORD_AUDIO)
//                == PackageManager.PERMISSION_GRANTED) {
//            Log.e("HAS_SOUND_PERMISSION", "TRUE");
//        } else {
//            Log.e("HAS_SOUND_PERMISSION", "FALSE");
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SOUND_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (soundMeter != null) {
                    soundMeter.stop();
                }
                canRecordSound = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (canRecordSound) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Log.e("HAS_SOUND_PERMISSION", "TRUE");
                    soundMeter = new SoundMeter();
                    try {
                        soundMeter.start();


                        soundCheckTimer = new Timer();

                        soundCheckTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // do your task here
                                Log.d("HAS_SOUND_PERMISSION", "tick");
                                Log.d("HAS_SOUND_PERMISSION", Double.toString(soundMeter.getAmplitude()));
                                thisCarNoiseReference.setValue(soundMeter.getAmplitude());

                            }
                        }, 0, soundInterval);

                    } catch (IOException e) {
                        e.printStackTrace();
                        canRecordSound = false;
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.e("HAS_SOUND_PERMISSION", "FALSE");
                    soundCheckTimer.cancel();
                    soundCheckTimer.purge();
                }
                shareNoiseLevelSwitch.setChecked(canRecordSound);
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
