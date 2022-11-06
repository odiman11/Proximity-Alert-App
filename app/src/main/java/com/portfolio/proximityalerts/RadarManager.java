package com.portfolio.proximityalerts;

import android.util.Log;

import java.io.FileOutputStream;
import java.util.HashMap;

public class RadarManager {

    public static final String TAG = "RadarManager";
    private static RadarManager managerInstance = null;
    static FileOutputStream output;
    static HashMap<String,EncounterView> encounters;

    //CONSTRUCTOR
    private RadarManager() {
        encounters = new HashMap<String, EncounterView>();
    }

    public static void setEncounter(EncounterView view){
        if(encounters.containsKey(view.mmsi)){
            encounters.get(view.mmsi).updateView(view);
            Log.e(TAG, "updating view");
        }else{
            Log.e(TAG, "adding new view");
            encounters.put(view.mmsi, view);
        }
    }

    public static synchronized RadarManager getInstance() {
        if (managerInstance == null) {
            managerInstance = new RadarManager();
        }
        return managerInstance;
    }
    public static HashMap<String,EncounterView> getTargetList(){
        return encounters;
    }
    public static void removeTarget(String mmsi){
        encounters.remove(mmsi);
    }
}
