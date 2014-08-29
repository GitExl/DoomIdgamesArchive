package nl.exl.doomidgamesarchive.idgamesapi;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Describes a single Idgames API review.
 */
public class Review {
    // The text inside this review.
    private String mText;
    
    // Reviewer rating.
    private float mRating = 0.0f;
    
    // Reviewer name. "Anonymous" if there is none.
    private String mUsername = "Anonymous";
    
    
    public String getText() {
        return mText;
    }
    
    public String getUsername() {
        return mUsername;
    }
    
    public float getRating() {
        return mRating;
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
    
    public void setUsername(String username) {
        mUsername = username;
    }
    
    public void setRating(float rating) {
        mRating = rating;
    }
    
    
    /**
     * Reads this review object from a JSON object.
     */
    public void fromJSON(JSONObject obj) {
        mText = obj.optString("text", null);
        mRating = (float)obj.optDouble("rating", 0.0d);
        mUsername = obj.optString("username", "Anonymous");
    }
    
    /**
     * Stores this review object into a JSON object.
     */
    public void toJSON(JSONObject obj) throws JSONException {
        obj.put("text", mText);
        obj.put("rating", mRating);
        obj.put("username", mUsername);
    }
}
