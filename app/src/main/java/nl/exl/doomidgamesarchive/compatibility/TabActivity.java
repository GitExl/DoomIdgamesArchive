package nl.exl.doomidgamesarchive.compatibility;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Base class for an activity with a tab interface.
 * Initializes the appropriate tab helper for the current SDK version.
 */
public abstract class TabActivity extends FragmentActivity {
    TabHelper mTabHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTabHelper = new TabHelperImpl(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTabHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTabHelper.onRestoreInstanceState(savedInstanceState);
    }

    protected TabHelper getTabHelper() {
        return mTabHelper;
    }
}
