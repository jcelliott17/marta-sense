package com.example.debandjackie.martasense;

import android.util.StringBuilderPrinter;

/**
 * Represents a single car
 *
 * @author Deb
 */

public class Car {

    private String carID;
    private int noiseLevelDecibels;
    private int numPeople;

    Car(String carID) {
        this.carID = carID;
        this.numPeople = -1;
    }


    public String getCarID() {
        return carID;
    }

    public int getNoiseLevelDecibels() {
        return noiseLevelDecibels;
    }

    public void setNoiseLevelDecibels(int noiseLevelDecibels) {
        this.noiseLevelDecibels = noiseLevelDecibels;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public String getNoiseLevelDescription() {
        return getNoiseLevelDescription(getNoiseLevelDecibels());
    }

    static String getNoiseLevelDescription(int noiseLevelDecibels) {
        String result = "Unknown";
        if (noiseLevelDecibels > 0) {
            result = "very quiet";
        }
        if (noiseLevelDecibels > 7000) {
            result = "quiet";
        }
        if (noiseLevelDecibels > 10000) {
            result = "moderate";
        }
        if (noiseLevelDecibels > 17000) {
            result = "loud";
        }
        if (noiseLevelDecibels > 25000) {
            result = "very loud";
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(carID);
        result.append(": ");
        if (numPeople >= 0) {
            result.append("\nAbout ");
            result.append(numPeople);
            result.append("people");
        }
        if (noiseLevelDecibels > 0) {
            result.append("\n");
            result.append(getNoiseLevelDescription(getNoiseLevelDecibels()));
            result.append(" (");
            result.append(getNoiseLevelDecibels());
            result.append(" decibels)");
        }
        return result.toString();
    }
}
