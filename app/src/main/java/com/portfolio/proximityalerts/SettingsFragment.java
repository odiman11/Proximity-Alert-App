package com.portfolio.proximityalerts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences sharedPreferences;

    SwitchPreference sw_compass, sw_location_service, sw_coarse_or_fine, swFormat;


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

        swFormat = findPreference("sw_format");
        swFormat.setChecked(sharedPreferences.getBoolean("sw_format" , false));

        swFormat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                if(swFormat.isChecked()){
                    swFormat.setTitle(R.string.format_km);
                }else{
                    swFormat.setTitle(R.string.format_mile);
                }
                return false;
            }
        });



    }
}
