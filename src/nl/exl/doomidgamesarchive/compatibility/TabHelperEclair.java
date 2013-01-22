/**
 * Copyright (c) 2012, Dennis Meuwissen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */
package nl.exl.doomidgamesarchive.compatibility;

import java.util.HashMap;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

/**
 * Tab helper class for Eclair SDKs up to Gingerbread SDKs.
 */
public class TabHelperEclair extends TabHelper implements TabHost.OnTabChangeListener {
    private FragmentActivity mActivity;
    private TabHost mTabHost;
    private HashMap<String, Tab> mTabList;
    private Tab mLastTab;

    
    /**
     * A dummy factory class that returns an empty, dummy view for a TabHost.
     */
    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
    
    
    public TabHelperEclair(FragmentActivity activity) {
        mActivity = activity;
        
        mTabList = new HashMap<String, Tab>();
    }
    
    @Override
    public void addTab(Tab tab) {
        String tag = tab.getTag();
        
        // Set up the TabHost if it was not done already.
        if (mTabHost == null) {
            mTabHost = (TabHost)mActivity.findViewById(android.R.id.tabhost);
            mTabHost.setup();
            mTabHost.setOnTabChangedListener(this);
        }
        
        // Set the tab properties.
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(tab.getText());
        tabSpec.setContent(new DummyTabFactory(mActivity));
        
        // Add the tab to the interface.
        mTabList.put(tag, tab);
        mTabHost.addTab(tabSpec);
        
        // Shorten the tab's height because there is no icon to display.
        // The tab would display an empty space instead of the icon otherwise.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            TabWidget widget = mTabHost.getTabWidget();
            int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, mActivity.getResources().getDisplayMetrics());
            RelativeLayout child = (RelativeLayout)widget.getChildAt(widget.getChildCount() - 1);
            child.getLayoutParams().height = height;
        }
    }
    
    @Override
    public void onTabChanged(String tabId) {
        Tab newTab = mTabList.get(tabId);
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();

        // Determine what event to trigger based on the previously selected tab and the newly selected one.
        if (mLastTab != newTab) {
            if (mLastTab != null) {
                // Unselect last tab.
                if (mLastTab.getFragment() != null) {
                    mLastTab.getListener().onTabUnselected(mLastTab, ft);
                }
            }

            // Select new tab.
            if (newTab != null) {
                newTab.getListener().onTabSelected(newTab, ft);
            }

            mLastTab = newTab;
        
        // Reselect current tab.
        } else {
            newTab.getListener().onTabReselected(newTab, ft);
        }

        ft.commit();
        mActivity.getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public Tab newTab(Fragment fragment, String tag) {
        return new TabEclair(mActivity, fragment, tag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("tabPosition", mTabHost.getCurrentTab());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt("tabPosition");
        mTabHost.setCurrentTab(position);
    }
}
