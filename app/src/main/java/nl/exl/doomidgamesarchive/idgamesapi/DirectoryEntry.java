package nl.exl.doomidgamesarchive.idgamesapi;

import java.io.File;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * IdgamesApi directory entry.
 */
public class DirectoryEntry extends Entry {

    // The database ID of this directory entry.
    private int mId = -1;
    
    // The name of this directory.
    private String mName = "";

    public void setId(int id) {
        mId = id;
    }

    void addName(String name) {
        mName += name;
    }
    
    public int getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }

    @NonNull
    public String toString() {
        String[] paths = mName.split(Pattern.quote(File.separator));
        return paths[paths.length - 1];
    }
}
