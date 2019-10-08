package nl.exl.doomidgamesarchive.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.fragments.PreferencesFragment;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.Preferences, new PreferencesFragment())
            .commit();
    }
}
