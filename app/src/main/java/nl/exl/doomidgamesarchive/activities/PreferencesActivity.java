package nl.exl.doomidgamesarchive.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import nl.exl.doomidgamesarchive.R;

public class PreferencesActivity extends PreferenceActivity {

    // Since we target pre-Honeycomb APIs as well, we need to continue to use a
    // PreferenceActivity instead of a PreferenceFragment.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}