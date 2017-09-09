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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private ArrayAdapter<Car> carArrayAdapter;

    private Switch shareNoiseLevelSwitch;
    private TextView currentCarTextView;

    private enum SortCriteria {
        NAME, NOISE_LEVEL, NUM_PEOPLE
    }

    private SortCriteria sortCriteria = SortCriteria.NUM_PEOPLE;

    private Spinner sortBySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentCarTextView = (TextView) findViewById(R.id.current_car_text_view);

        setThisCarReference(defaultCarID);

        carListView = (ListView) findViewById(R.id.car_list_view);
        carList = new ArrayList<>();

        carArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carList);

        carListView.setAdapter(carArrayAdapter);

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
                updateCarList();
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
                updateCarList();
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
                updateCarList();
                canRecordSound = b;
                if (canRecordSound) {
                    requestSoundReporting();
                } else {
                    shareNoiseLevelSwitch.setText("Share Noise Level");
                    if (soundCheckTimer != null) {
                        soundCheckTimer.cancel();
                        soundCheckTimer.purge();
                    }
                }
            }
        });

        sortBySpinner = (Spinner) findViewById(R.id.sort_by_spinner);
        final String[] sortTextArray = getResources().getStringArray(R.array.sort_by_array);
        final Map<String, SortCriteria> sortTextMap = new HashMap<>();
        sortTextMap.put(sortTextArray[0], SortCriteria.NAME);
        sortTextMap.put(sortTextArray[1], SortCriteria.NUM_PEOPLE);
        sortTextMap.put(sortTextArray[2], SortCriteria.NOISE_LEVEL);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sort_by_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(spinnerAdapter);

        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                updateCarList(sortTextMap.get(item.toString()));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

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
    }

    protected void updateShareSwitchText(final double amplitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shareNoiseLevelSwitch.setText(amplitude > 0 ? amplitude + " decibels - " + Car.getNoiseLevelDescription((long) amplitude) : "Sharing Noise Level...");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SOUND_REQUEST_CODE: {
                if (soundMeter != null) {
                    soundMeter.stop();
                }
                canRecordSound = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (canRecordSound) {

                    soundMeter = new SoundMeter();
                    try {
                        soundMeter.start();

                        soundCheckTimer = new Timer();

                        soundCheckTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                double amplitude = soundMeter.getAmplitude();
                                if (amplitude > 0) {
                                    thisCarNoiseReference.setValue(amplitude);
                                }
                                updateShareSwitchText(amplitude);
                            }
                        }, 0, soundInterval);

                    } catch (IOException e) {
                        e.printStackTrace();
                        canRecordSound = false;
                    }
                } else {
                    soundCheckTimer.cancel();
                    soundCheckTimer.purge();
                }
                shareNoiseLevelSwitch.setChecked(canRecordSound);
            }
        }
    }

    private void updateCarList() {
        switch (sortCriteria) {
            case NAME:
                Collections.sort(carList, new Comparator<Car>() {
                    @Override
                    public int compare(com.example.debandjackie.martasense.Car car1, com.example.debandjackie.martasense.Car car2) {
                        return car1.getCarID().compareTo(car2.getCarID());
                    }
                });
                break;
            case NOISE_LEVEL:
                Collections.sort(carList, new Comparator<Car>() {
                    @Override
                    public int compare(com.example.debandjackie.martasense.Car car1, com.example.debandjackie.martasense.Car car2) {
                        return car1.getNoiseLevelDecibels() > car2.getNoiseLevelDecibels() ? 1 : -1;
                    }
                });
                break;
            case NUM_PEOPLE:
                Collections.sort(carList, new Comparator<Car>() {
                    @Override
                    public int compare(com.example.debandjackie.martasense.Car car1, com.example.debandjackie.martasense.Car car2) {
                        return car1.getNumPeople() > car2.getNumPeople() ? 1 : -1;
                    }
                });
                break;
        }
        carArrayAdapter.notifyDataSetChanged();
    }

    private void updateCarList(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
        updateCarList();
    }
}
