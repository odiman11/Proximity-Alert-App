package com.portfolio.proximityalerts;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences sharedPreferences;

    SwitchPreference sw_compass, sw_location_service, sw_coarse_or_fine;
    EditTextPreference radar_radius;


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        setPreferencesFromResource(R.xml.prefrences, rootKey);
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        sw_compass = findPreference("switch_compass");
        sw_compass.setChecked(sharedPreferences.getBoolean("switch_compass" , true));

        sw_coarse_or_fine = findPreference("sw_save_power");
        sw_coarse_or_fine.setChecked(sharedPreferences.getBoolean("sw_save_power" , false));

        sw_location_service = findPreference("sw_location_services");
        sw_location_service.setChecked(sharedPreferences.getBoolean("sw_location_services" , true));

        radar_radius = findPreference("radar_radius");
        radar_radius.setText(sharedPreferences.getString("radar_radius", "20"));

    }
}
