package nl.exl.doomidgamesarchive.idgamesapi;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Describes a single Idgames API review.
 */
public class Review {
    // The mText inside this review.
    private String mText;
    
    
    public String getText() {
        return mText;
    }
    
    public void setText(String text) {
        mText = text;
    }
    
    public void addText(String text) {
        if (mText == null) {
            mText = text;
        } else {
            mText += text;
        }
    }
    
    /**
     * Reads this review object from a JSON object.
     */
    public void fromJSON(JSONObject obj) {
        mText = obj.optString("text", null);
    }
    
    /**
     * Stores this review object into a JSON object.
     */
    public void toJSON(JSONObject obj) throws JSONException {
        obj.put("text", mText);
    }
}
