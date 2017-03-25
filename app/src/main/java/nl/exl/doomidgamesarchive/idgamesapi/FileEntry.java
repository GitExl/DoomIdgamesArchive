package nl.exl.doomidgamesarchive.idgamesapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An IdgamesApi file entry. Contains a title, author, description, rating and more.
 */
public class FileEntry extends Entry {
    // File database id.
    private int mId = -1;
    
    // Title of this entry.
    private String mTitle;
    
    // The author of this entry.
    private String mAuthor;
    
    // The email of this entry's author.
    private String mEmail;
    
    // A description of this entry.
    private String mDescription;
    
    // File info of this entry.
    private String mFileName;
    private String mFilePath;
    private int mFileSize;
    
    // Temporal info of this entry. mDate can be null.
    private int mTimestamp;
    private String mDate;
    private String mLocaleDate;
    
    // Rating info of this entry.
    private double mRating;
    private int mVoteCount;
    
    // The URL at which to view this entry. Points to doomworld.com/idgames
    private String mUrl;
    
    // The idgames protocol URL of this entry.
    private String mIdgamesUrl;

    // Miscellaneous info of this entry.
    private String mCredits;
    private String mBase;
    private String mBuildTime;
    private String mEditorsUsed;
    private String mBugs;
    
    // The complete contents of the text file describing this entry.
    private String mTextFileContents;
    
    // List of associated reviews
    private List<Review> reviews;
    
    
    FileEntry() {
        reviews = new ArrayList<>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void toJSON(JSONObject obj) throws JSONException {
        obj.put("id", mId);
        
        obj.put("title", mTitle);
        obj.put("author", mAuthor);
        obj.put("description", mDescription);
        obj.put("email", mEmail);
        
        obj.put("fileName", mFileName);
        obj.put("filePath", mFilePath);
        obj.put("fileSize", mFileSize);
        
        obj.put("timestamp", mTimestamp);
        obj.put("date", mDate);
        
        obj.put("rating", mRating);
        obj.put("voteCount", mVoteCount);
        
        obj.put("url", mUrl);
        obj.put("idgamesUrl", mIdgamesUrl);
        
        obj.put("credits", mCredits);
        obj.put("base", mBase);
        obj.put("buildTime", mBuildTime);
        obj.put("editorsUsed", mEditorsUsed);
        obj.put("bugs", mBugs);
        
        obj.put("textFileContents", mTextFileContents);
        
        // Store reviews.
        JSONObject reviewObj;
        JSONArray reviewArray = new JSONArray();
        
        for (Review review : reviews) {
            reviewObj = new JSONObject();
            review.toJSON(reviewObj);
            reviewArray.put(reviewObj);
        }
        obj.put("reviews", reviewArray);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void fromJSON(JSONObject obj) throws JSONException {
        mId = obj.getInt("id");
        
        mTitle = obj.optString("title", null);
        mAuthor = obj.optString("author", null);
        mDescription = obj.optString("description", null);
        mEmail = obj.optString("email", null);
        
        mFileName = obj.optString("fileName", null);
        mFilePath = obj.optString("filePath", null);
        mFileSize = obj.optInt("fileSize");
        
        mTimestamp = obj.optInt("timestamp");
        mDate = obj.optString("date", null);
        
        mRating = obj.optDouble("rating");
        mVoteCount = obj.optInt("voteCount");
        
        mUrl = obj.optString("url", null);
        mIdgamesUrl = obj.optString("idgamesUrl", null);
        
        mCredits = obj.optString("credits", null);
        mBase = obj.optString("base", null);
        mBuildTime = obj.optString("buildTime", null);
        mEditorsUsed = obj.optString("editorsUsed", null);
        mBugs = obj.optString("bugs", null);
        
        mTextFileContents = obj.optString("textFileContents", null);
        
        // Read reviews.
        JSONObject reviewObj;
        JSONArray reviewArray = obj.optJSONArray("reviews");
        for (int i = reviewArray.length() - 1; i >= 0; i--) {
            reviewObj = reviewArray.getJSONObject(i);
            
            Review review = new Review();
            review.fromJSON(reviewObj);
            
            reviews.add(review);
        }
    }
    
    public void setId(int id) {
        mId = id;
    }

    void addTitle(String title) {
        if (mTitle == null) {
            mTitle = title;
        } else {
            mTitle += title;
        }
    }

    void addAuthor(String author) {
        if (mAuthor == null) {
            mAuthor = author;
        } else {
            mAuthor += author;
        }
    }

    void addDescription(String description) {
        if (mDescription == null) {
            mDescription = description;
        } else {
            mDescription += description;
        }
    }

    void addEmail(String email) {
        if (mEmail == null) {
            mEmail = email;
        } else {
            mEmail += email;
        }
    }

    void addFileName(String fileName) {
        if (mFileName == null) {
            mFileName = fileName;
        } else {
            mFileName += fileName;
        }
    }

    void addFilePath(String filePath) {
        if (mFilePath == null) {
            mFilePath = filePath;
        } else {
            mFilePath += filePath;
        }
    }

    void setFileSize(int fileSize) {
        mFileSize = fileSize;
    }

    void setTimeStamp(int timeStamp) {
        mTimestamp = timeStamp;
    }

    void addDate(String date) {
        if (mDate == null) {
            mDate = date;
        } else {
            mDate += date;
        }
    }
    
    public void setRating(double rating) {
        mRating = rating;
    }

    void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    void addUrl(String url) {
        if (mUrl == null) {
            mUrl = url;
        } else {
            mUrl += url;
        }
    }

    void addIdgamesUrl(String idgamesUrl) {
        if (mIdgamesUrl == null) {
            mIdgamesUrl = idgamesUrl;
        } else {
            mIdgamesUrl += idgamesUrl;
        }
    }

    void addCredits(String credits) {
        if (mCredits == null) {
            mCredits = credits;
        } else {
            mCredits += credits;
        }
    }

    void addBase(String base) {
        if (mBase == null) {
            mBase = base;
        } else {
            mBase += base;
        }
    }

    void addBuildTime(String buildTime) {
        if (mBuildTime == null) {
            mBuildTime = buildTime;
        } else {
            mBuildTime += buildTime;
        }
    }

    void addEditorsUsed(String editorsUsed) {
        if (mEditorsUsed == null) {
            mEditorsUsed = editorsUsed;
        } else {
            mEditorsUsed += editorsUsed;
        }
    }

    void addBugs(String bugs) {
        if (mBugs == null) {
            mBugs = bugs;
        } else {
            mBugs += bugs;
        }
    }

    void addTextFileContents(String textFileContents) {
        if (mTextFileContents == null) {
            mTextFileContents = textFileContents;
        } else {
            mTextFileContents += textFileContents;
        }
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
        if (mAuthor == null || mAuthor.length() == 0) {
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
        if (mDate == null) {
            return null;
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
     */
    public String toString() {
        if (mTitle == null || mTitle.length() == 0) {
            if (mFileName == null || mFileName.length() == 0) {
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