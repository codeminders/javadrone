package com.codeminders.ardrone;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsPrefs extends PreferenceActivity {
     
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
