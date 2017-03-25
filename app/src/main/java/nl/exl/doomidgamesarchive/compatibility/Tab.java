package nl.exl.doomidgamesarchive.compatibility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * Abstract class that describes a tab.
 */
public abstract class Tab {
    public abstract void setText(int textResource);
    public abstract void setListener(TabListener listener);
    public abstract void setFragment(Fragment fragment);
    
    public abstract String getTag();
    public abstract String getText();
    public abstract TabListener getListener();
    public abstract Fragment getFragment();
    public abstract Object getTab();
    
    public interface TabListener {
        public void onTabSelected(Tab tab, FragmentTransaction ft);
        public void onTabUnselected(Tab tab, FragmentTransaction ft);
        public void onTabReselected(Tab tab, FragmentTransaction ft);
    }
}