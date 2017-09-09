package com.example.debandjackie.martasense;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference thisCarNoiseReference;

    private static final String THIS_CAR_ID = "car-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        thisCarNoiseReference = database.getReference().child("cars").child(THIS_CAR_ID).child("noise-level");
        thisCarNoiseReference.setValue("jackie-test");
    }
}
