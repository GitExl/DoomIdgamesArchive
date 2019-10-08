package nl.exl.doomidgamesarchive.idgamesapi;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * An IdgamesApi file entry. Contains a title, author, description, rating and more.
 */
public class FileEntry extends Entry {

    // File database id.
    private int mId = -1;
    
    // Title of this entry.
    private String mTitle = "";
    
    // The author of this entry.
    private String mAuthor = "";
    
    // The email of this entry's author.
    private String mEmail = "";
    
    // A description of this entry.
    private String mDescription = "";
    
    // File info of this entry.
    private String mFileName = "";
    private String mFilePath = "";
    private int mFileSize;
    
    // Temporal info of this entry.
    private String mDate = "";
    private String mLocaleDate = "";
    
    // Rating info of this entry.
    private double mRating;
    private int mVoteCount;

    // Miscellaneous info of this entry.
    private String mCredits = "";
    private String mBase = "";
    private String mBuildTime = "";
    private String mEditorsUsed = "";
    private String mBugs = "";
    
    // The complete contents of the text file describing this entry.
    private String mTextFileContents = "";
    
    // List of associated reviews
    private List<Review> reviews;
    
    
    FileEntry() {
        reviews = new ArrayList<>();
    }

    public void setId(int id) {
        mId = id;
    }

    void addTitle(String title) {
        mTitle += title;
    }

    void addAuthor(String author) {
        mAuthor += author;
    }

    void addDescription(String description) {
        mDescription += description;
    }

    void addEmail(String email) {
        mEmail += email;
    }

    void addFileName(String fileName) {
        mFileName += fileName;
    }

    void addFilePath(String filePath) {
        mFilePath += filePath;
    }

    void setFileSize(int fileSize) {
        mFileSize = fileSize;
    }

    void addDate(String date) {
        mDate += date;
    }
    
    public void setRating(double rating) {
        mRating = rating;
    }

    void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    void addCredits(String credits) {
        mCredits += credits;
    }

    void addBase(String base) {
        mBase += base;
    }

    void addBuildTime(String buildTime) {
        mBuildTime += buildTime;
    }

    void addEditorsUsed(String editorsUsed) {
        mEditorsUsed += editorsUsed;
    }

    void addBugs(String bugs) {
        mBugs += bugs;
    }

    void addTextFileContents(String textFileContents) {
        mTextFileContents += textFileContents;
    }

    void addReview(Review review) {
        reviews.add(review);
    }

    public int getId() {
        return mId;
    }
    
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns this entry's author name.
     * 
     * @return "Unknown" if there is no author name, the author name otherwise.
     */
    public String getAuthor() {
        if (mAuthor.isEmpty()) {
            return "Unknown";
        } else {
            return mAuthor;
        }
    }

    public String getDescription() {
        return mDescription;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public int getFileSize() {
        return mFileSize;
    }
    
    /**
     * Returns the size of this file as a formatted size string.
     *  
     * @return The file size.
     */
    public String getFileSizeString() {
        if (mFileSize < 1024) {
            return mFileSize + " B";
        }
        
        int exp = (int) (Math.log(mFileSize) / Math.log(1024));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        
        return String.format(Locale.getDefault(), "%.1f %sB", mFileSize / Math.pow(1024, exp), pre);
    }

    /**
     * Returns this entry's date as a locale formatted date.
     * 
     * @return The locale formatted date of this entry.
     */
    public String getLocaleDate() {
        if (mDate.isEmpty()) {
            return "";
        }
        
        // Parse the date string and construct a localized date string.
        if (mLocaleDate == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            
            Date date = null;
            try {
                date = dateFormat.parse(mDate);
            } catch (ParseException e) {
                Log.w("FileEntry", "Could not parse date " + mDate);
            }

            // Store the localized string to prevent date parsing next time it is needed.
            if (date != null) {
                mLocaleDate = DateFormat.getDateInstance().format(date);
            } else {
                mLocaleDate = "Unknown";
            }
        }
        
        return mLocaleDate;
    }

    public double getRating() {
        return mRating;
    }

    public int getVoteCount() {
        return mVoteCount;
    }

    public String getCredits() {
        return mCredits;
    }

    public String getBase() {
        return mBase;
    }

    public String getBuildTime() {
        return mBuildTime;
    }

    public String getEditorsUsed() {
        return mEditorsUsed;
    }

    public String getBugs() {
        return mBugs;
    }

    public String getTextFileContents() {
        return mTextFileContents;
    }
    
    /**
     * Returns this entry's title. If the entry has no title, the filename is returned instead.
     *
     * @return String
     */
    @NonNull
    public String toString() {
        if (mTitle.isEmpty()) {
            if (mFileName.isEmpty()) {
                return "Unknown";
            } else {
                return mFileName;
            }
        } else {
            return mTitle;
        }
    }
    
    public List<Review> getReviews() {
        return reviews;
    }
}
