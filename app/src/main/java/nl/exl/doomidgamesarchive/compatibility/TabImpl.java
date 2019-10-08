package nl.exl.doomidgamesarchive.compatibility;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


class TabImpl extends Tab implements ActionBar.TabListener {
    private ActionBar.Tab mTab;
    private AppCompatActivity mActivity;
    private Tab.TabListener mListener;
    private Fragment mFragment;
    private String mTag;


    TabImpl(AppCompatActivity activity, Fragment fragment, String tag) {
        mActivity = activity;
        mFragment = fragment;
        mTag = tag;

        mTab = activity.getSupportActionBar().newTab();
        mTab.setTabListener(this);
    }

    @Override
    public void setText(int textResource) {
        mTab.setText(textResource);
    }

    @Override
    public String getText() {
        return mTab.getText().toString();
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
        return this.mTab;
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

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction f) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.disallowAddToBackStack();
        mListener.onTabReselected(this, ft);
        ft.commit();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction f) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.disallowAddToBackStack();
        mListener.onTabSelected(this, ft);
        ft.commit();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction f) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.disallowAddToBackStack();
        mListener.onTabUnselected(this, ft);
        ft.commit();
    }


}
