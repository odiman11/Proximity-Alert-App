package com.portfolio.proximityalerts;

import java.io.*;
import java.net.*;

public class Position {
    private double longitude;
    private double latitude;

    // create and initialize a point with given name and
    // (latitude, longitude) specified in degrees
    public Position(double lat, double lon) {
        latitude  = lat;
        longitude = lon;
    }

    public Position(String position) {
        String [] latlong = position.split(",");
        latitude = Double.parseDouble(latlong[0]);
        longitude = Double.parseDouble(latlong[1]);
    }

    public Position(Position p){
        this(p.latitude,p.longitude);
    }

    // return distance between this Position and that Position
    // measured in statute miles
    public double distanceTo(Position that) {
        double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(that.latitude);
        double lon2 = Math.toRadians(that.longitude);

        // great circle distance in radians, using law of cosines formula
        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        // each degree on a great circle of Earth is 60 nautical miles
        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

    // return string representation of this point
    public String toString() {
        return latitude + ", " + longitude;
    }

    public byte[] getBytes()
    {
        return this.toString().getBytes();
    }

    public void setLat(double lat){
        latitude = lat;
    }

    public void setLong(double lon){
        longitude  = lon;
    }

    public double getLat(){
        return latitude;
    }
    public double getLong(){
        return longitude;
    }


    // test client
    public static void main(String[] args) {

    }
}
