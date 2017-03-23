package nl.exl.doomidgamesarchive;

import nl.exl.doomidgamesarchive.activities.AboutActivity;
import nl.exl.doomidgamesarchive.activities.PreferencesActivity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Main activity's settings menu.
 */
public final class SettingsMenu {
    
    public static boolean onOptionsItemSelected(MenuItem item, FragmentActivity activity) {
        switch (item.getItemId()) {
            case R.id.MenuMain_Settings:
                activity.startActivity(new Intent(activity, PreferencesActivity.class));
                return true;
            case R.id.MenuMain_About:
                activity.startActivity(new Intent(activity, AboutActivity.class));
                return true;
            default:
                return false;
        }
    }
    
    public static boolean onCreateOptionsMenu(Menu menu, FragmentActivity activity) {
        activity.getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
