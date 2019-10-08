package nl.exl.doomidgamesarchive.idgamesapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a single response from the Idgames web API.
 */
public class Response {

    // The version number of the response.
    private Float mVersion;
    
    // The error returned in the response, if any.
    private String mErrorMessage;
    
    // The warning returned in the response, if any.
    private String mWarningType;
    
    // THe entries returned in the response, if any.
    private List<Entry> mEntries;
    
    
    Response() {
        mEntries = new ArrayList<>();
    }

    public float getVersion() {
        return this.mVersion;
    }
    
    public void setVersion(float newVersion) {
        this.mVersion = newVersion;
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
}
