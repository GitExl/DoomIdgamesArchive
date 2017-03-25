package nl.exl.doomidgamesarchive.idgamesapi;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache for IdgamesApi response objects. It stores serialized versions of response
 * objects on disk, while limiting the total size of the cache.
 */
class ResponseCache {
    // Maximum size of this cache, in bytes.
    private final static long MAX_SIZE = 1024 * 1024 * 5;

    // FileEntry extension to identify cache files with.
    private final static String CACHE_FILE_EXTENSION = ".igc";
    
    // Filename containing the cache version.
    private final static String CACHE_VERSION_FILE = "doomidgamesarchive.version";
    
    // Current version index of this cache.
    private int mVersion;
    
    // Current total size of cache, in bytes. 
    private int mCurrentSize;
    
    // The cache's directory object.
    private File mDirectory;
    
    // Map of all entries currently in the cache.
    private Map<String, File> mEntries;

    
    ResponseCache(Context context) {
        mDirectory = context.getCacheDir();
        
        // Generate list of available entries and keep track of cache size.
        mEntries = new HashMap<>();
        for (File file : mDirectory.listFiles()) {
            if (file.getName().endsWith(CACHE_FILE_EXTENSION)) {
                mEntries.put(file.getName(), file);
                mCurrentSize += file.length();
            
            // Record cache version.
            } else if (file.getName().equals(CACHE_VERSION_FILE)) {
                readVersion(file);
            }
        }
        
        // Get app version code.
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e("ResponseCache", "Could not get package info: " + e.toString());
            return;
        }

        // Test whether the cache is from a previous application version.
        // If the cache is not of the current app version, erase it.
        if (mVersion == 0 || mVersion != pInfo.versionCode) {
            Log.i("ResponseCache", "Clearing cache from version " + mVersion + " to version " + pInfo.versionCode);
            clearCache(pInfo.versionCode);
        }
    }
    
    /**
     * Clears the cache by removing all cache related files and generating a new version file.
     * 
     * @param newVersionCode The version code to write into this cache's version file.
     */
    private void clearCache(int newVersionCode) {
        // Clear the list of cache entries.
        mEntries = new HashMap<>();

        // Delete all files related to this cache.
        for (File file : mDirectory.listFiles()) {
            if (file.getName().endsWith(CACHE_FILE_EXTENSION) || file.getName().equals(CACHE_VERSION_FILE)) {
                if (!file.delete()) {
                    Log.w("ResponseCache", "Could not delete cache file.");
                }
            }
        }
        
        // Generate a version object.
        JSONObject version = new JSONObject();
        try {
            version.put("version", newVersionCode);
        } catch (JSONException e) {
            Log.e("ResponseCache", "Could not create version object: " + e.toString());
            return;
        }
        
        // Write the new version file.
        try {
            File file = new File(mDirectory.getPath() + "/" + CACHE_VERSION_FILE);
            FileWriter writer = new FileWriter(file);
            writer.write(version.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("ResponseCache", "Could not write version file: " + e.toString());
        }
    }
    
    /**
     * Reads a cache version file and records it's information.
     * 
     * @param file The file object to read.
     */
    private void readVersion(File file) {
        FileReader reader;
        
        // Read version file as a string.
        char[] versionBuffer = new char[(int)file.length()];
        try {
            reader = new FileReader(file);
            int readBytes = reader.read(versionBuffer, 0, (int)file.length());
            reader.close();

            if (readBytes != file.length()) {
                Log.w("ResponseCache", "Did not read entire cache file.");
            }
        } catch (IOException e) {
            Log.e("ResponseCache", "Could not read cache version file: " + e.toString());
            return;
        }
        String versionData = new String(versionBuffer);
        
        // Parse version file into JSON.
        JSONObject version;
        try {
            version = new JSONObject(versionData);
        } catch (JSONException e) {
            Log.e("ResponseCache", "Could not parse cache version file: " + e.toString());
            return;
        }
        
        mVersion = version.optInt("version", 0);
    }
    
    /**
     * Will query the cache for a response to the request.
     * 
     * @param request The IdGamesApiRequest object to query for.
     * @param maxAge The maximum age of the response, in milliseconds.
     * @return An IdGamesApiResponse object if one was in the cache. null if the response is not in the cache or older than maxAge.
     * @throws IOException if any IO error occurred.
     */
    Response get(Request request, long maxAge) throws IOException {
        String hash = request.getHash() + CACHE_FILE_EXTENSION;
        
        // Test whether the request exists in this cache.
        if (mEntries.containsKey(hash)) {
            File entry = mEntries.get(hash);
            
            // The entry is too old, do not return it.
            if (System.currentTimeMillis() - entry.lastModified() > maxAge) {
                return null;
            }
            
            // Read response data into a string.
            char[] responseBuffer = new char[(int)entry.length()];
            FileReader reader = new FileReader(entry);
            int bytesRead = reader.read(responseBuffer, 0, (int)entry.length());
            reader.close();

            if (bytesRead != entry.length()) {
                Log.w("ResponseCache", "Did not read entire response file from cache.");
            }

            String responseData = new String(responseBuffer);
            
            // Create a new response with data from cache.
            Response response = new Response();
            try {
                response.fromJSON(new JSONObject(responseData));
            } catch (JSONException e) {
                Log.w("ResponseCache", "Could not parse cached JSON data: " + e.toString());
                response = null;
            }
            
            return response;
        }
        
        return null;
    }
    
    /**
     * Adds a new response object to this cache.
     * 
     * @param request The original request object that was used to generate the response.
     * @param response The response object generated from the request.
     * @throws IOException if an IO error occurred.
     */
    void put(Request request, Response response) throws IOException {
        // Create new cache file.
        String filePath = mDirectory.getPath() + "/" + request.getHash() + CACHE_FILE_EXTENSION;
        File entry = new File(filePath);
        
        // Write the response data to the cache file.
        FileWriter writer = new FileWriter(entry);
        writer.write(response.toJSON().toString());
        writer.flush();
        writer.close();
    
        addEntry(entry);
    }
    
    /**
     * Removes oldest cache files until the total cache size is < mMaxSize
     */
    private void prune() {
        File entry;

        // Keep deleting oldest files until the cache has shrunk to below the maximum size.
        while (mCurrentSize > MAX_SIZE) {
            entry = getOldestEntry();
            if (entry == null) {
                break;
            }
            
            removeEntry(entry);
        }
    }
    
    /**
     * Adds a new entry to the cache.
     * 
     * @param entry The entry to add.
     */
    private void addEntry(File entry) {
        mCurrentSize += entry.length();
        mEntries.put(entry.getName(), entry);
        
        // Prune old entries if needed.
        if (mCurrentSize > MAX_SIZE)
            prune();
    }
    
    /**
     * Removes a single cache entry from this cache.
     * 
     * @param entry The entry to remove.
     */
    private void removeEntry(File entry) {
        mCurrentSize -= entry.length();
        mEntries.remove(entry.getName());
        if (!entry.delete()) {
            Log.w("ResponseCache", "Could not remove entry.");
        }
    }
    
    /**
     * Search this cache for the oldest entry.
     * 
     * @return The oldest entry in this cache.
     */
    private File getOldestEntry() {
        File oldest = null;
        
        for (File entry : mEntries.values()) {
            if (oldest == null) {
                oldest = entry;
            } else if (entry.lastModified() < oldest.lastModified()) {
                oldest = entry;
            }
        }
        
        return oldest;
    }
}
