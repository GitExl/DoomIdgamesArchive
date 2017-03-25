package nl.exl.doomidgamesarchive.idgamesapi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Pattern;

/**
 * IdgamesApi directory entry.
 */
public class DirectoryEntry extends Entry {
    // The database ID of this directory entry.
    private int mId = -1;
    
    // The name of this directory.
    private String mName;
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void toJSON(JSONObject obj) throws JSONException {
        obj.put("id", mId);
        obj.put("name", mName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void fromJSON(JSONObject obj) throws JSONException {
        mId = obj.getInt("id");
        mName = obj.getString("name");
    }
    
    public void setId(int id) {
        mId = id;
    }

    void addName(String name) {
        if (mName == null) {
            mName = name;
        } else {
            mName += name;
        }
    }
    
    public int getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public String toString() {
        String[] paths = mName.split(Pattern.quote(File.separator));
        return paths[paths.length - 1];
    }
}
