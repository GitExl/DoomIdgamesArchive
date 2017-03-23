package nl.exl.doomidgamesarchive.idgamesapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An IdgamesApi vote entry.
 */
public class VoteEntry extends Entry {
    // The database ID of this vote.
    private int mId = -1;
    
    // The database ID of the file that this vote was made on.
    private int mFileId = -1;
    
    // The mTitle of the file voted on.
    private String mTitle;
    
    // THe mAuthor of the file voted on (not the mAuthor of this vote).
    private String mAuthor;
    
    // The mDescription of the file voted on.
    private String mDescription;
    
    // The review text the voter entered. 
    private String mReviewText;
    
    // THe mRating the voter entered.
    private double mRating = 0;
        
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void toJSON(JSONObject obj) throws JSONException {
        obj.put("id", mId);
        obj.put("fileId", mFileId);
        obj.put("title", mTitle);
        obj.put("author", mAuthor);
        obj.put("description", mDescription);
        obj.put("reviewText", mReviewText);
        obj.put("rating", mRating);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void fromJSON(JSONObject obj) throws JSONException {
        mId = obj.getInt("id");
        mFileId = obj.getInt("fileId");
        mTitle = obj.optString("title", null);
        mAuthor = obj.optString("author", null);
        mDescription = obj.optString("description", null);
        mReviewText = obj.optString("reviewText", null);
        mRating = obj.getDouble("rating");
    }
    
    public void setId(int id) {
        mId = id;
    }
    
    public void setFileId(int fileId) {
        mFileId = fileId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    
    public void addTitle(String title) {
        if (mTitle == null) {
            mTitle = title;
        } else {
            mTitle += title;
        }
    }

    public void addAuthor(String author) {
        if (mAuthor == null) {
            mAuthor = author;
        } else {
            mAuthor += author;
        }
    }

    public void addDescription(String description) {
        if (mDescription == null) {
            mDescription = description;
        } else {
            mDescription += description;
        }
    }
    
    public void addReviewText(String reviewText) {
        if (mReviewText == null) {
            mReviewText = reviewText;
        } else {
            mReviewText += reviewText;
        }
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public int getId() {
        return mId;
    }
    
    public int getFileId() {
        return mFileId;
    }
    
    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        if (mAuthor == null || mAuthor.length() == 0)
            return "Unknown";
        else
            return mAuthor;
    }

    public String getDescription() {
        return mDescription;
    }
    
    public String getReviewText() {
        return mReviewText;
    }

    public double getRating() {
        return mRating;
    }
    
    public String toString() {
        return mTitle;
    }
}
