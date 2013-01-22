/**
 * Copyright (c) 2012, Dennis Meuwissen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package nl.exl.doomidgamesarchive.idgamesapi;

import nl.exl.doomidgamesarchive.Config;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.util.Log;

/**
 * Generates an Idgames API request URL from an action and a number of parameters.  
 */
public class Request {
    // The URL of the Idgames API
    private static final String API_URL = "http://www.doomworld.com/idgames/api/api.php?";
    
    // Supported actions.
    public static final int GET_CONTENTS = 0;
    public static final int GET_LATESTFILES = 1;
    public static final int GET_LATESTVOTES = 2;
    public static final int GET_FILE = 3;
    public static final int SEARCH = 4;
    
    // Search categories.
    public static final int CATEGORY_FILENAME = 0;
    public static final int CATEGORY_TITLE = 1;
    public static final int CATEGORY_AUTHOR = 2;
    public static final int CATEGORY_EMAIL = 3;
    public static final int CATEGORY_DESCRIPTION = 4;
    public static final int CATEGORY_CREDITS = 5;
    public static final int CATEGORY_EDITORS = 6;
    public static final int CATEGORY_TEXTFILE = 7;
    
    // The action to execute.
    private int mAction;
    
    // The name of the directory to retrieve.
    private String mDirectoryName;
    
    // The database ID of the file to retrieve.
    private int mFileId;
    
    // The search query to execute.
    private String mQuery;
    
    // The search category type.
    private int mCategory;
    
    // The maximum number of entries to retrieve.
    private int mLimit = Config.LIMIT_DEFAULT;
    
    // The maximum age of this request. This is used by the ResponseCache to determine
    // when a new request needs to be sent to the web API.
    private long mMaxAge = Config.MAXAGE_DEFAULT;
    
    
    /**
     * Restore's this request's state from a Bundle object.
     * 
     * @param bundle The Bundle object to restore state from.
     */
    public void restoreFromBundle(Bundle bundle) {
        mAction = bundle.getInt("action", -1);
        mDirectoryName = getBundleString(bundle, "directoryName", null);
        mFileId = bundle.getInt("fileId", -1);
        mQuery = getBundleString(bundle, "query", null);
        mCategory = bundle.getInt("category", Config.CATEGORY_DEFAULT);
        mLimit = bundle.getInt("limit", Config.LIMIT_DEFAULT);
        mMaxAge = bundle.getLong("maxAge", Config.MAXAGE_DEFAULT);
    }
    
    /**
     * Saves this request's state to a Bundle object.
     * 
     * @param out The Bundle to save state to.
     */
    public void saveToBundle(Bundle out) {
        out.putInt("action", mAction);
        out.putString("directoryName", mDirectoryName);
        out.putInt("fileId", mFileId);
        out.putString("query", mQuery);
        out.putInt("category", mCategory);
        out.putInt("limit", mLimit);
        out.putLong("maxAge", mMaxAge);
    }
    
    /**
     * Returns a string from a Bundle object. Supports returning a default value.
     * 
     * @param bundle
     * @param key
     * @param defaultValue
     * @return
     */
    private String getBundleString(Bundle bundle, String key, String defaultValue) {
        String value = bundle.getString(key);
        
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
    
    public void setAction(int action) {
        this.mAction = action;
    }
    
    public void setDirectoryName(String newDirectoryName) {
        mDirectoryName = newDirectoryName;
    }
    
    public void setLimit(int limit) {
        this.mLimit = limit;
    }
    
    public void setQuery(String query) {
        mQuery = query;
    }
    
    public void setCategory(int category) {
        mCategory = category;
    }
    
    public void setMaxAge(long maxAge) {
        this.mMaxAge = maxAge;
    }
    
    public long getMaxAge() {
        return this.mMaxAge;
    }
    
    public String getDirectoryName() {
        return mDirectoryName;
    }
    
    public int getFileId() {
        return mFileId;
    }

    public void setFileId(int fileId) {
        this.mFileId = fileId;
    }
    
    public int getAction() {
        return mAction;
    }
    
    /**
     * Returns a hash string that describes this request. The hash is based on the URL that
     * this request generates. 
     * 
     * @return The hash string.
     */
    public String getHash() {
        return "request_" + getURL().hashCode();
    }
    
    /**
     * Returns the HTTP URL to execute for this request.
     * 
     * @return The HTTP URL string to make this request with.
     */
    public String getURL() {
        Builder builder = Uri.parse(API_URL).buildUpon();
        
        switch (mAction) {
            // Action for retrieving the contents of a directory.
            case GET_CONTENTS:
                builder.appendQueryParameter("action", "getcontents");
                if (mDirectoryName == null || mDirectoryName.equals(""))
                    builder.appendQueryParameter("id", "0");
                else
                    builder.appendQueryParameter("name", mDirectoryName);
                break;
                
            // Action for retrieving the newest files.
            case GET_LATESTFILES:
                builder.appendQueryParameter("action", "latestfiles");
                builder.appendQueryParameter("limit", Integer.toString(mLimit));
                break;
                
            // Action for retrieving the newest votes.
            case GET_LATESTVOTES:
                builder.appendQueryParameter("action", "latestvotes");
                builder.appendQueryParameter("limit", Integer.toString(mLimit));
                break;
                
            // Action for retrieving a single file's details.
            case GET_FILE:
                builder.appendQueryParameter("action", "get");
                builder.appendQueryParameter("id", Integer.toString(mFileId));
                break;
                
            case SEARCH:
                builder.appendQueryParameter("action", "search");
                builder.appendQueryParameter("query", mQuery);
                builder.appendQueryParameter("type", categoryString(mCategory));
                break;
                
            default:
                Log.w("Request", "Invalid or unhandled request action type.");
        }
        
        return builder.build().toString();
    }
    
    /**
     * Returns a string that represents the category type in an Idgames request.
     * 
     * @param category The category id to return in string form.
     * 
     * @return A string that can be used in an Idgames request.
     */
    private String categoryString(int category) {
        if (category == CATEGORY_FILENAME) {
            return "filename";
        } else if (category == CATEGORY_TITLE) {
            return "title";
        } else if (category == CATEGORY_AUTHOR) {
            return "author";
        } else if (category == CATEGORY_EMAIL) {
            return "email";
        } else if (category == CATEGORY_DESCRIPTION) {
            return "description";
        } else if (category == CATEGORY_CREDITS) {
            return "credits";
        } else if (category == CATEGORY_EDITORS) {
            return "editors";
        } else if (category == CATEGORY_TEXTFILE) {
            return "textfile";
        }
        
        return null;
    }
}
