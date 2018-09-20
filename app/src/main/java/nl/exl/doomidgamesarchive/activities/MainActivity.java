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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;

import nl.exl.doomidgamesarchive.Config;
import nl.exl.doomidgamesarchive.IdgamesListFragment;
import nl.exl.doomidgamesarchive.IdgamesListFragment.IdgamesListener;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.SettingsMenu;
import nl.exl.doomidgamesarchive.compatibility.Tab;
import nl.exl.doomidgamesarchive.compatibility.TabActivity;
import nl.exl.doomidgamesarchive.compatibility.TabHelper;
import nl.exl.doomidgamesarchive.idgamesapi.DirectoryEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Entry;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.VoteEntry;

/**
 * The main activity, containing the tabbed interface. Also instantiates the list fragments.
 */
public class MainActivity extends TabActivity implements IdgamesListener, OnSharedPreferenceChangeListener, Tab.TabListener {
    private final static String TAB_TAG_BROWSE = "browse";
    private final static String TAB_TAG_NEWFILES = "newfiles";
    private final static String TAB_TAG_NEWVOTES = "newvotes";
    private final static String TAB_TAG_SEARCH = "search";
    
    // The currently selected tab's tag.
    private String mSelectedTabTag;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            File httpCacheDir = new File(this.getCacheDir(), "https");
            long httpCacheSize = 5 * 1024 * 1024;
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("MainActivity", "HTTP response cache installation failed: " + e);
        }

        setContentView(R.layout.main);
        
        // Check for a network connection
        if (!testConnectivity()) {
            return;
        }
        
        // Add tabs to interface
        buildTabs(savedInstanceState);
        
        // Register preference change listener
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        // Find the currently active fragment.
        FragmentManager manager = getSupportFragmentManager();
        IdgamesListFragment fragment = (IdgamesListFragment)manager.findFragmentByTag(mSelectedTabTag);
        if (fragment == null) {
            Log.w("MainActivity", "No fragment with tag " + mSelectedTabTag);
            return;
        }
        
        // Finish this activity if the fragment back press function cannot go back any further.
        if (!fragment.enterParentDirectory()) {
            this.finish();
        }
    };
    
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
     * Builds the tabs and their initial fragments.
     * Reuses fragments if they already exist after this activity has been resumed.
     * 
     * @param savedInstanceState Bundle of values saved when this activity was stopped. 
     */
    private void buildTabs(Bundle savedInstanceState) {
        Tab tab = null;
        TabHelper tabHelper = getTabHelper();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        FragmentManager manager = getSupportFragmentManager();
        
        // Build the new files fragment.
        IdgamesListFragment newFilesFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWFILES); 
        if (newFilesFragment == null) {
            int limit = Integer.parseInt(sharedPrefs.getString("ListLimitNew", Integer.toString(Config.LIMIT_NEWFILES)));
            
            Bundle args = new Bundle();
            args.putInt("action", Request.GET_LATESTFILES);
            args.putLong("maxAge", Config.MAXAGE_NEWFILES);
            args.putInt("limit", limit);
            args.putBoolean("sort", false);
            args.putBoolean("addListIndex", true);
            
            newFilesFragment = new IdgamesListFragment();
            newFilesFragment.setArguments(args);
        }
        
        // Build the new files tab.
        tab = tabHelper.newTab(newFilesFragment, TAB_TAG_NEWFILES);
        tab.setText(R.string.Tabs_NewFiles);
        tab.setListener(this);
        tabHelper.addTab(tab);
        
        
        // Build the new votes fragment.
        IdgamesListFragment newVotesFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWVOTES);
        if (newVotesFragment == null) {        
            int limit = Integer.parseInt(sharedPrefs.getString("ListLimitVotes", Integer.toString(Config.LIMIT_NEWVOTES)));
            
            Bundle args = new Bundle();
            args.putInt("action", Request.GET_LATESTVOTES);
            args.putLong("maxAge", Config.MAXAGE_NEWVOTES);
            args.putInt("limit", limit);
            args.putBoolean("sort", false);
            args.putBoolean("addListIndex", true);
            
            newVotesFragment = new IdgamesListFragment();
            newVotesFragment.setArguments(args);
        }
        
        // Build the new votes tab.
        tab = tabHelper.newTab(newVotesFragment, TAB_TAG_NEWVOTES);
        tab.setText(R.string.Tabs_LatestVotes);
        tab.setListener(this);
        tabHelper.addTab(tab);
        
        
        // Build the browse fragment.
        IdgamesListFragment browseFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_BROWSE);
        if (browseFragment == null) {
            Bundle args = new Bundle();
            args.putInt("action", Request.GET_CONTENTS);
            args.putString("directoryName", "");
            args.putLong("maxAge", Config.MAXAGE_BROWSE);
            args.putBoolean("sort", true);
            args.putBoolean("addListIndex", false);
            
            browseFragment = new IdgamesListFragment();
            browseFragment.setArguments(args);
        }
        
        // Build the browse tab.
        tab = tabHelper.newTab(browseFragment, TAB_TAG_BROWSE);
        tab.setText(R.string.Tabs_Browse);
        tab.setListener(this);
        tabHelper.addTab(tab);
        
        
        // Build the search fragment.
        IdgamesListFragment searchFragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_SEARCH);
        if (searchFragment == null) {
            Bundle args = new Bundle();
            args.putInt("action", Request.SEARCH);
            args.putString("query", null);
            args.putInt("category", Request.CATEGORY_FILENAME);
            args.putLong("maxAge", Config.MAXAGE_SEARCH);
            args.putBoolean("sort", true);
            args.putBoolean("addListIndex", false);
            
            searchFragment = new IdgamesListFragment();
            searchFragment.setArguments(args);
        }
        
        // Build the search tab.
        tab = tabHelper.newTab(searchFragment, TAB_TAG_SEARCH);
        tab.setText(R.string.Tabs_Search);
        tab.setListener(this);
        tabHelper.addTab(tab);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        return SettingsMenu.onOptionsItemSelected(item, this);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return SettingsMenu.onCreateOptionsMenu(menu, this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        FragmentManager manager = getSupportFragmentManager();
        IdgamesListFragment fragment = null;
        
        // Select the appropriate fragment to update.
        if (key.equals("ListLimitNew")) {
            fragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWFILES);
        } else if (key.equals("ListLimitVotes")) {
            fragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_NEWVOTES);
        } else if (key.equals("ListLimitSearch")) {
            fragment = (IdgamesListFragment)manager.findFragmentByTag(TAB_TAG_SEARCH);
        }
        
        // Modify the request of chosen fragment and tell it to update.
        if (fragment != null) {
            fragment.setLimit(Integer.parseInt(sharedPreferences.getString(key, Integer.toString(Config.LIMIT_DEFAULT))));
            fragment.updateList();
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mSelectedTabTag = tab.getTag();

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(mSelectedTabTag);

        if (fragment == null) {
            ft.add(android.R.id.tabcontent, tab.getFragment(), mSelectedTabTag);
        } else {
            ft.attach(tab.getFragment());
        }

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    @Override
    protected void onStop() {
        super.onStop();

        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        ft.detach(tab.getFragment());
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}
