package nl.exl.doomidgamesarchive;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainTabAdapter extends FragmentStateAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();
    private final List<String> mFragmentTags = new ArrayList<>();

    public MainTabAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    public String getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }

    public String getPageTag(int position) {
        return mFragmentTags.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public void addFragment(Fragment fragment, String title, String tag) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
        mFragmentTags.add(tag);
    }

}
