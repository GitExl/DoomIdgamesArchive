package nl.exl.doomidgamesarchive.compatibility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * A tab object for Eclair SDKs up to Gingerbread SDKs.
 */
public class TabEclair extends Tab {
    private FragmentActivity mActivity;
    private Tab.TabListener mListener;
    private Fragment mFragment;
    private String mTag;
    private String mText;
    
    
    protected TabEclair(FragmentActivity activity, Fragment fragment, String tag) {
        mActivity = activity;
        mFragment = fragment;
        mTag = tag;
    }
    
    @Override
    public void setText(int textResource) {
        mText = mActivity.getResources().getString(textResource);
    }
    
    @Override
    public String getText() {
        return mText;
    }

    @Override
    public void setListener(TabListener listener) {
        mListener = listener;
    }
    
    @Override
    public TabListener getListener() {
        return mListener;
    }

    @Override
    public Object getTab() {
        return null;
    }

    @Override
    public String getTag() {
        return mTag;
    }

    @Override
    public Fragment getFragment() {
        return mFragment;
    }
    
    @Override
    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }
}
