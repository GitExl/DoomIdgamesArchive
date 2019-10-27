package nl.exl.doomidgamesarchive.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import nl.exl.doomidgamesarchive.Config;
import nl.exl.doomidgamesarchive.IdgamesListFragment;
import nl.exl.doomidgamesarchive.IdgamesListFragment.IdgamesListener;
import nl.exl.doomidgamesarchive.MainTabAdapter;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.SettingsMenu;
import nl.exl.doomidgamesarchive.idgamesapi.DirectoryEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Entry;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.VoteEntry;


/**
 * The main activity, containing the tabbed interface. Also instantiates the list fragments.
 */
public class MainActivity extends AppCompatActivity implements IdgamesListener, OnSharedPreferenceChangeListener {

    private final static String TAB_TAG_BROWSE = "browse";
    private final static String TAB_TAG_NEWFILES = "newfiles";
    private final static String TAB_TAG_NEWVOTES = "newvotes";
    private final static String TAB_TAG_SEARCH = "search";

    private MainTabAdapter mTabAdapter;
    private ViewPager2 mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            File httpCacheDir = new File(getCacheDir(), "https");
            long httpCacheSize = 5 * 1024 * 1024;
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("MainActivity", "HTTP response cache installation failed: " + e);
        }

        setContentView(R.layout.main);
        setupNavigation();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check for a network connection
        if (!testConnectivity()) {
            return;
        }

        // Register preference change listener
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void setupNavigation() {
        mTabAdapter = new MainTabAdapter(this);
        buildTabs(mTabAdapter);

        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, mViewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(mTabAdapter.getPageTitle(position));
                tab.setTag(mTabAdapter.getPageTag(position));
            }
        });
        mediator.attach();
    }

    /**
     * Builds the tabs and their initial fragments.
     * Reuses fragments if they already exist after this activity has been resumed.
     */
    private void buildTabs(MainTabAdapter adapter) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        FragmentManager manager = getSupportFragmentManager();

        // New files.
        IdgamesListFragment newFilesFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWFILES);
        if (newFilesFragment == null) {
            String limitPref = sharedPrefs.getString("ListLimitNew", Integer.toString(Config.LIMIT_NEWFILES));
            int limit = Integer.parseInt(limitPref);

            Bundle args = new Bundle();
            args.putInt("action", Request.GET_LATESTFILES);
            args.putLong("maxAge", Config.MAXAGE_NEWFILES);
            args.putInt("limit", limit);
            args.putBoolean("sort", false);

            newFilesFragment = new IdgamesListFragment();
            newFilesFragment.setArguments(args);
        }
        adapter.addFragment(newFilesFragment, "New", TAB_TAG_NEWFILES);

        // New votes.
        IdgamesListFragment newVotesFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWVOTES);
        if (newVotesFragment == null) {
            String limitPref = sharedPrefs.getString("ListLimitVotes", Integer.toString(Config.LIMIT_NEWVOTES));
            int limit = Integer.parseInt(limitPref);

            Bundle args = new Bundle();
            args.putInt("action", Request.GET_LATESTVOTES);
            args.putLong("maxAge", Config.MAXAGE_NEWVOTES);
            args.putInt("limit", limit);
            args.putBoolean("sort", false);

            newVotesFragment = new IdgamesListFragment();
            newVotesFragment.setArguments(args);
        }
        adapter.addFragment(newVotesFragment, "Votes", TAB_TAG_NEWVOTES);

        // Browser.
        IdgamesListFragment browseFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_BROWSE);
        if (browseFragment == null) {
            Bundle args = new Bundle();
            args.putInt("action", Request.GET_CONTENTS);
            args.putString("directoryName", "");
            args.putLong("maxAge", Config.MAXAGE_BROWSE);
            args.putBoolean("sort", true);

            browseFragment = new IdgamesListFragment();
            browseFragment.setArguments(args);
        }
        adapter.addFragment(browseFragment, "Browse", TAB_TAG_BROWSE);

        // Search.
        IdgamesListFragment searchFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_SEARCH);
        if (searchFragment == null) {
            Bundle args = new Bundle();
            args.putInt("action", Request.SEARCH);
            args.putString("query", null);
            args.putInt("category", Request.CATEGORY_FILENAME);
            args.putLong("maxAge", Config.MAXAGE_SEARCH);
            args.putBoolean("sort", true);

            searchFragment = new IdgamesListFragment();
            searchFragment.setArguments(args);
        }
        adapter.addFragment(searchFragment, "Search", TAB_TAG_SEARCH);
    }

    @Override
    public void onBackPressed() {
        int position = mViewPager.getCurrentItem();
        IdgamesListFragment fragment = (IdgamesListFragment)mTabAdapter.createFragment(position);

        if (!fragment.enterParentDirectory()) {
            finish();
        }
    }

    /**
     * Test for network data connectivity.
     * @return True if there is a working data connection, false if there is not.
     */
    private boolean testConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("No data connection available")
                .setMessage("Turn on data access or connect to a network to be able to use this application.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                }).create();
            alertDialog.show();

            return false;
        }

        return true;
    }

    /**
     * Raised from a IdgamesListFragment when an entry is selected.
     */
    public void onEntrySelected(IdgamesListFragment fragment, Entry entry) {
        // Display a new sub-folder.
        if (entry instanceof DirectoryEntry) {
            fragment.enterDirectory((DirectoryEntry)entry);

        // Display the details activity.
        } else if (entry instanceof FileEntry || entry instanceof VoteEntry) {
            int id;

            if (entry instanceof FileEntry) {
                FileEntry fileEntry = (FileEntry)entry;
                id = fileEntry.getId();
            } else {
                VoteEntry voteEntry = (VoteEntry)entry;
                id = voteEntry.getFileId();
            }

            Intent in = new Intent(this, DetailsActivity.class);
            in.putExtra("fileId", id);
            startActivity(in);
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return SettingsMenu.onOptionsItemSelected(item, this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return SettingsMenu.onCreateOptionsMenu(menu, this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        int position = mViewPager.getCurrentItem();
        String tag = mTabAdapter.getPageTag(position);
        IdgamesListFragment fragment = (IdgamesListFragment)mTabAdapter.createFragment(position);

        // Select the appropriate fragment to update.
        if (key.equals("ListLimitNew") && tag.equals(TAB_TAG_NEWFILES)) {
            int newLimit = Integer.parseInt(sharedPreferences.getString(key, Integer.toString(Config.LIMIT_DEFAULT)));
            fragment.setLimit(newLimit);
            fragment.updateList();

        } else if (key.equals("ListLimitVotes") && tag.equals(TAB_TAG_NEWVOTES)) {
            int newLimit = Integer.parseInt(sharedPreferences.getString(key, Integer.toString(Config.LIMIT_DEFAULT)));
            fragment.setLimit(newLimit);
            fragment.updateList();

        } else if (key.equals("ListLimitSearch") && tag.equals(TAB_TAG_SEARCH)) {
            int newLimit = Integer.parseInt(sharedPreferences.getString(key, Integer.toString(Config.LIMIT_DEFAULT)));
            fragment.setLimit(newLimit);
            fragment.updateList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

}
