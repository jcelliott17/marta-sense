package com.example.debandjackie.martasense;

/**
 * Represents a single car
 *
 * @author Deb
 */

public class Car {

    private String carID;
    private long noiseLevelDecibels;
    private long numPeople;

    Car(String carID) {
        if (carID == null) {
            throw new IllegalArgumentException("Car ID cannot be null");
        }
        this.carID = carID;
        this.numPeople = -1;
    }


    public String getCarID() {
        return carID;
    }

    public long getNoiseLevelDecibels() {
        return noiseLevelDecibels;
    }

    public void setNoiseLevelDecibels(long noiseLevelDecibels) {
        this.noiseLevelDecibels = noiseLevelDecibels;
    }

    public long getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(long numPeople) {
        this.numPeople = numPeople;
    }

    public String getNoiseLevelDescription() {
        return getNoiseLevelDescription(getNoiseLevelDecibels());
    }

    static String getNoiseLevelDescription(long noiseLevelDecibels) {
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
        if (noiseLevelDecibels > 30000) {
            result = "extremely loud";
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("\n");
        result.append(carID);
        if (numPeople >= 0) {
            result.append("\n\nAbout ");
            result.append(numPeople);
            result.append(" people");
        }
        if (noiseLevelDecibels > 0) {
            result.append("\n\n");
            result.append(getNoiseLevelDescription(getNoiseLevelDecibels()));
            result.append(" (");
            result.append(getNoiseLevelDecibels());
            result.append(" decibels)");
        }
        result.append("\n");
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o) {
            return true;
        }
        // null check
        if (o == null) {
            return false;
        }
        // type check and cast
        if (getClass() != o.getClass()) {
            return false;
        }
        return this.carID.equals(((Car) o).getCarID());
    }

    @Override
    public int hashCode() {
        return carID.hashCode();
    }
}
