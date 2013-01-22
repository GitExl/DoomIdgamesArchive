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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Tab helper class for Honeycomb SDKs and up.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabHelperHoneycomb extends TabHelper {
    private ActionBar mActionBar;
    private FragmentActivity mActivity;
    
    
    public TabHelperHoneycomb(FragmentActivity activity) {
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
        return new TabHoneycomb(mActivity, fragment, tag);
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
