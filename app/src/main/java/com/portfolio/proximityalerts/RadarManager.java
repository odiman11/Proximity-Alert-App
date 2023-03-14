package com.portfolio.proximityalerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RadarManager {

    public static final String TAG = "RadarManager";
    public static final String HASH_STRING = "hashString";
    private static RadarManager managerInstance = null;
    static HashMap<String,EncounterView> encounters;
    static HashMap<String, String> favoriteList;
    SharedPreferences sharedPreferences;


    //CONSTRUCTOR
    private RadarManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        encounters = new HashMap<String, EncounterView>();
        loadSavedList();
    }

    public static void saveToTargetList(EncounterView view){
        favoriteList.put(view.mmsi, view.toString());
        updateEncounter(view);
    }
    public static void removeFromTargetList(String mmsi){
        if(favoriteList.containsKey(mmsi)){
            favoriteList.remove(mmsi);
        }
        if(encounters.containsKey(mmsi)){
            EncounterView updateView = encounters.get(mmsi);
            updateView.setEncounterColor(2);
            updateView.setEncounterDescription("");
            updateEncounter(updateView);
        }

    }

    public static HashMap<String, String> getSavedTargetList(){return favoriteList;}

    //update after adding to favorites
    public static void updateEncounter(EncounterView view){
        encounters.put(view.mmsi, view);
    }

    //update when new info comes from server
    public static void setEncounter(EncounterView view){
        float latitude = view.position.getLat();
        if(((latitude > 0.0) || (latitude < 0)) && ((latitude > -90) && (latitude < 90)) ) {
            if (encounters.containsKey(view.mmsi)) {
                encounters.get(view.mmsi).updateView(view);
                Log.e(TAG, "updating view");
            } else {
                Log.e(TAG, "adding new view");
                if (favoriteList.containsKey(view.mmsi)) {
                    EncounterView updatedView = view;
                    String [] savedViewData = favoriteList.get(view.mmsi).split(";");
                    updatedView.setEncounterColor(Integer.parseInt(savedViewData[3]));
                    updatedView.setEncounterDescription(savedViewData[2]);
                    encounters.put(view.mmsi, updatedView);
                } else {
                    encounters.put(view.mmsi, view);
                }
            }
        }
    }

    public void saveSavedList(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favoriteList);
        editor.putString(HASH_STRING, json).apply();
    }

    private void loadSavedList (){
        Gson gson = new Gson();
        String storedHashMapString = sharedPreferences.getString(HASH_STRING, null);
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        favoriteList = gson.fromJson(storedHashMapString, type);

        if (favoriteList == null) {
            favoriteList = new HashMap<>();
        }
    }

    public static synchronized RadarManager getInstance(Context context) {
        if (managerInstance == null) {
            managerInstance = new RadarManager(context);
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
