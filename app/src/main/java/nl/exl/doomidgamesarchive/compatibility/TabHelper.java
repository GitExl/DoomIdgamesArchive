package nl.exl.doomidgamesarchive.compatibility;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

/**
 * Abstract class of a tab helper.
 */
public abstract class TabHelper {
    public abstract Tab newTab(Fragment fragment, String tag);
    public abstract void addTab(Tab tab);
    public abstract void onSaveInstanceState(Bundle outState);
    public abstract void onRestoreInstanceState(Bundle savedInstanceState);
}
