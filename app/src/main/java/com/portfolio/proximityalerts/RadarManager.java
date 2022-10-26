package com.portfolio.proximityalerts;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RadarManager {

    public static final String TAG = "RadarManager";
    private static RadarManager managerInstance = null;
    File directory;
    static FileOutputStream output;
    static HashMap<String,EncounterView> encounters;

    //CONSTRUCTOR
    private RadarManager() {
        encounters = new HashMap<String, EncounterView>();
    }

    public HashMap<String, EncounterView> setEncounter(EncounterView view){
        if(encounters.containsKey(view.mmsi)){
            encounters.get(view.mmsi).updateView(view);
            Log.e(TAG, "updating new view");
        }else{
            Log.e(TAG, "adding new view");
            encounters.put(view.mmsi, view);
        }
        return encounters;
    }

    public static void saveEncounters(){
        try {
            output = new FileOutputStream("Encounters");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream outputSer = null;
        try {
            outputSer = new ObjectOutputStream(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputSer.writeObject(encounters);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputSer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static synchronized RadarManager getInstance() {
        if (managerInstance == null) {
            managerInstance = new RadarManager();
        }
        return managerInstance;
    }

    public static void removeTarget(String mmsi){
        encounters.remove(mmsi);
    }
}
