package nl.exl.doomidgamesarchive.compatibility;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Tab helper class for Honeycomb SDKs and up.
 */
class TabHelperImpl extends TabHelper {
    private ActionBar mActionBar;
    private AppCompatActivity mActivity;

    TabHelperImpl(AppCompatActivity activity) {
        mActivity = activity;

        // Request the ActionBar feature for the associated activity.
        mActivity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        // Setup the actionbar.
        mActionBar = activity.getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public void addTab(Tab tab) {
        mActionBar.addTab((ActionBar.Tab)tab.getTab());
    }

    @Override
    public Tab newTab(Fragment fragment, String tag) {
        return new TabImpl(mActivity, fragment, tag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("tabPosition", mActionBar.getSelectedTab().getPosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt("tabPosition");
        mActionBar.setSelectedNavigationItem(position);
    }
}
