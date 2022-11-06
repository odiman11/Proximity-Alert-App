package com.portfolio.proximityalerts;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;


public class ObserverManager extends MediatorLiveData<Object[]> {

    static ObserverManager observeInstance = null;
   LiveData<String[]> direction;
   LiveData<Message> client;
   LiveData<Location> gpsData;

    public ObserverManager(LiveData<String[]> comp, LiveData<Location> gps, LiveData<Message> udpClient){
        direction = comp;
        gpsData = gps;
        client = udpClient;

        addSource(direction, value -> setValue(new Object[]{"compass", direction.getValue(), gpsData.getValue(),client.getValue()}));
        addSource(gpsData, value -> setValue(new Object[]{"gps", direction.getValue(), gpsData.getValue(),client.getValue()}));
        addSource(client, value -> setValue(new Object[]{"udpClient", direction.getValue(), gpsData.getValue(),client.getValue()}));

    }

    public static ObserverManager getInstance(LiveData<String[]> com, LiveData<Location> gps, LiveData<Message> updClient) {
        if (observeInstance == null) {
            observeInstance = new ObserverManager(com, gps, updClient);
        }
        return observeInstance;
    }

}
