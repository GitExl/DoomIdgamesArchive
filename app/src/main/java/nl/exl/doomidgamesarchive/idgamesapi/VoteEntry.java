package nl.exl.doomidgamesarchive.idgamesapi;

import androidx.annotation.NonNull;

/**
 * An IdgamesApi vote entry.
 */
public class VoteEntry extends Entry {

    // The database ID of this vote.
    private int mId = -1;
    
    // The database ID of the file that this vote was made on.
    private int mFileId = -1;
    
    // The title of the file voted on.
    private String mTitle = "";

    // The review text the voter entered. 
    private String mReviewText = "";
    
    // The rating the voter entered.
    private double mRating = 0;

    // Author of the entry (not the vote).
    private String mAuthor = "";

    public void setId(int id) {
        mId = id;
    }
    
    void setFileId(int fileId) {
        mFileId = fileId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    
    void addTitle(String title) {
        mTitle += title;
    }

    void addReviewText(String reviewText) {
        mReviewText += reviewText;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getAuthor() {
        return mAuthor;
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

    public String getReviewText() {
        return mReviewText;
    }

    public double getRating() {
        return mRating;
    }

    @NonNull
    public String toString() {
        return mTitle;
    }
}
