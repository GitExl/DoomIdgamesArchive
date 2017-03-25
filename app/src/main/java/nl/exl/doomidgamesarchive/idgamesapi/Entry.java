package nl.exl.doomidgamesarchive.idgamesapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * IdgamesApi entry abstract class.
 */
public abstract class Entry {
    /**
     * Adds the properties of this entry to a JSON object.
     * 
     * @param obj The object to add the JSON properties to.
     * @throws JSONException if a JSON object could not be generated.
     */
    public abstract void toJSON(JSONObject obj) throws JSONException;
    
    /**
     * Reads properties from a JSON object into this entry.
     * 
     * @param obj The JSON object to read properties from.
     * @throws JSONException if required properties are absent.
     */
    public abstract void fromJSON(JSONObject obj) throws JSONException;
}
