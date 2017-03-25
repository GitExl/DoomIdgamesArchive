package nl.exl.doomidgamesarchive.compatibility;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Tab helper class for Honeycomb SDKs and up.
 */
class TabHelperImpl extends TabHelper {
    private ActionBar mActionBar;
    private FragmentActivity mActivity;

    TabHelperImpl(FragmentActivity activity) {
        mActivity = activity;

        // Request the ActionBar feature for the associated activity.
        mActivity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        // Setup the actionbar.
        mActionBar = activity.getActionBar();
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
