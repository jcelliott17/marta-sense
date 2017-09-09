package com.example.debandjackie.martasense;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference thisCarReference;
    private DatabaseReference thisCarNoiseReference;

    private DatabaseReference allCarsReference;

    private SoundMeter soundMeter;

    private static final String defaultCarID = "Car 1";
    private String thisCarID;
    private static final long soundInterval = 1000;
    private Timer soundCheckTimer;

    private static final int SOUND_REQUEST_CODE = 773;
    private boolean canRecordSound;

    private List<Car> carList;
    private ListView carListView;

    private Switch shareNoiseLevelSwitch;
    private TextView currentCarTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentCarTextView = (TextView) findViewById(R.id.current_car_text_view);

        setThisCarReference(defaultCarID);

        carListView = (ListView) findViewById(R.id.car_list_view);
        carList = new ArrayList<>();

        final ArrayAdapter<Car> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carList);

        carListView.setAdapter(arrayAdapter);

        allCarsReference = database.getReference().child("cars");
        allCarsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String carID = dataSnapshot.getKey();
                Car newCar = new Car(carID);
                if (dataSnapshot.child("noise-level").getValue() != null) {
                    newCar.setNoiseLevelDecibels(dataSnapshot.child("noise-level").getValue(Long.class));
                }
                if (dataSnapshot.child("num-people").getValue() != null) {
                    newCar.setNumPeople(dataSnapshot.child("num-people").getValue(Long.class));
                }
                carList.add(newCar);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String carID = dataSnapshot.getKey();
                Car newCar = new Car(carID);
                carList.remove(newCar);
                if (dataSnapshot.child("noise-level").getValue() != null) {
                    newCar.setNoiseLevelDecibels(dataSnapshot.child("noise-level").getValue(Long.class));
                }
                if (dataSnapshot.child("num-people").getValue() != null) {
                    newCar.setNumPeople(dataSnapshot.child("num-people").getValue(Long.class));
                }
                carList.add(newCar);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        carListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setThisCarReference(carList.get(i).getCarID());
            }
        });

        shareNoiseLevelSwitch = (Switch) findViewById(R.id.share_noise_level_switch);
        shareNoiseLevelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                arrayAdapter.notifyDataSetChanged();
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

    private void setThisCarReference(String carID) {
        this.thisCarID = carID;
        currentCarTextView.setText("Current Car: " + carID);
        thisCarReference = database.getReference().child("cars/" + carID);
        thisCarNoiseReference = thisCarReference.child("noise-level");
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
                                double amplitude = soundMeter.getAmplitude();
                                Log.d("HAS_SOUND_PERMISSION", Double.toString(amplitude));
                                if (amplitude > 0) {
                                    thisCarNoiseReference.setValue(amplitude);
                                }
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
