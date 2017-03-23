package nl.exl.doomidgamesarchive.activities;

import nl.exl.doomidgamesarchive.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {

    // Since we target pre-Honeycomb APIs as well, we need to continue to use a
    // PreferenceActivity instead of a PreferenceFragment.
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}