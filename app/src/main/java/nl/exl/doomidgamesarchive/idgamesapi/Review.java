package nl.exl.doomidgamesarchive.idgamesapi;

/**
 * Describes a single Idgames API review.
 */
public class Review {

    // The text inside this review.
    private String mText = "";
    
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
    
    void addText(String text) {
        mText += text;
    }
    
    void setUsername(String username) {
        mUsername = username;
    }
    
    public void setRating(float rating) {
        mRating = rating;
    }
}
