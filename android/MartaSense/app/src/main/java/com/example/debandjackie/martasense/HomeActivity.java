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

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference thisCarNoiseReference;

    private static final String THIS_CAR_ID = "car-1";

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
                canRecordSound = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (canRecordSound) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Log.e("HAS_SOUND_PERMISSION", "TRUE");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Log.e("HAS_SOUND_PERMISSION", "FALSE");
                }
                shareNoiseLevelSwitch.setChecked(canRecordSound);
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
