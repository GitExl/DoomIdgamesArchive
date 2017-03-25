package nl.exl.doomidgamesarchive.idgamesapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a single response from the Idgames web API.
 */
public class Response {
    // Types of Idgames API entries.
    private static final int ENTRY_TYPE_FILE = 0;
    private static final int ENTRY_TYPE_DIRECTORY = 1;
    private static final int ENTRY_TYPE_VOTE = 2;
    
    // The version number of the response.
    private Float mVersion;
    
    // The error returned in the response, if any.
    private String mErrorType;
    private String mErrorMessage;
    
    // The warning returned in the response, if any.
    private String mWarningType;
    private String mWarningMessage;
    
    // THe entries returned in the response, if any.
    private List<Entry> mEntries;
    
    
    Response() {
        mEntries = new ArrayList<Entry>();
    }
    
    /**
     * Serializes this response object into a new JSON object.
     * 
     * @return A JSON object containing this response.
     */
    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        
        try {
            json.put("version", mVersion);
            
            json.put("errorType", mErrorType);
            json.put("errorMessage", mErrorMessage);
            
            json.put("warningType", mWarningType);
            json.put("warningMessage", mWarningMessage);
            
            // Entry list.
            Entry entry;
            JSONObject obj;
            
            JSONArray entryList = new JSONArray();
            for (int i = 0; i < mEntries.size(); i++) {
                entry = mEntries.get(i);
                obj = new JSONObject();
                
                // Determine the entry type.
                if (entry instanceof FileEntry) {
                    obj.put("type", ENTRY_TYPE_FILE);
                } else if (entry instanceof DirectoryEntry) {
                    obj.put("type", ENTRY_TYPE_DIRECTORY);
                } else if (entry instanceof VoteEntry) {
                    obj.put("type", ENTRY_TYPE_VOTE);
                }
                
                // Convert the entry to JSON.
                entry.toJSON(obj);
                entryList.put(obj);
            }
            json.put("entries", entryList);
            
        } catch (JSONException e) {
            Log.e("Response", "Cannot convert response to JSON: " + e.toString());
            json = null;
        }
        
        return json;
    }
    
    /**
     * Restores this response's state from a JSON object.
     * 
     * @param json THe JSON object to restore state from.
     */
    void fromJSON(JSONObject json) {
        try { 
            mVersion = (float)json.getDouble("version");
            
            mErrorType = json.optString("errorType", null);
            mErrorMessage = json.optString("errorMessage", null);
            
            mWarningType = json.optString("warningType", null);
            mWarningMessage = json.optString("warningMessage", null);
            
            // Entry list.
            Entry entry;
            JSONObject obj;
            
            JSONArray entryList = json.getJSONArray("entries");
            for (int i = 0; i < entryList.length(); i++) {
                obj = (JSONObject)entryList.get(i);
                
                // Determine the entry's type.
                switch (obj.getInt("type")) {
                    case ENTRY_TYPE_FILE:
                        entry = new FileEntry();
                        break;
                    case ENTRY_TYPE_DIRECTORY:
                        entry = new DirectoryEntry();
                        break;
                    case ENTRY_TYPE_VOTE:
                        entry = new VoteEntry();
                        break;
                    default:
                        entry = null;
                        break;
                }
                
                // Read the new entry and add it.
                if (entry != null) {
                    entry.fromJSON(obj);
                    mEntries.add(entry);
                }
            }
        } catch (JSONException e) {
            Log.e("Response", "Cannot read JSON: " + e.toString());
        }
    }
    
    public float getVersion() {
        return this.mVersion;
    }
    
    public void setVersion(float newVersion) {
        this.mVersion = newVersion;
    }
    
    public String getErrorType() {
        return this.mErrorType;
    }
    
    void setErrorType(String newErrorType) {
        this.mErrorType = newErrorType;
    }
    
    public String getWarningType() {
        return this.mWarningType;
    }
    
    void setWarningType(String newWarningType) {
        this.mWarningType = newWarningType;
    }

    void addEntry(Entry entry) {
        this.mEntries.add(entry);
    }
    
    public List<Entry> getEntries() {
        return this.mEntries;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    void setErrorMessage(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }

    void setWarningMessage(String warningMessage) {
        this.mWarningMessage = warningMessage;
    }
}
