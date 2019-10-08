package nl.exl.doomidgamesarchive.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import nl.exl.doomidgamesarchive.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}