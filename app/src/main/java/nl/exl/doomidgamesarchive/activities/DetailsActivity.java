package nl.exl.doomidgamesarchive.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nl.exl.doomidgamesarchive.Config;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.RatingView;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;
import nl.exl.doomidgamesarchive.idgamesapi.Review;

/**
 * Displays details from an IdgamesApi file.
 * Builds a number of views to form a custom mLayout.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailsActivity extends Activity {

    // Activity states for UI choices.
    private static final int STATE_INVALID = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_READY = 2;

    // Invalid file.
    private static final int FILE_ID_INVALID = -1;

    // Permission requests.
    private static final int PERMISSION_CALLBACK_DOWNLOAD = 1;
    
    // Layout references.
    private LinearLayout mLayout;
    private RelativeLayout mTitleLayout;
    private ImageView mProgress;
    private TextView mTitleText;
    private RatingView mRatingView;
    private TextView mVoteCount;

    // ID of the IdgamesApi file being displayed.
    private int mFileId = FILE_ID_INVALID;
    
    // Info from the IdgamesApi file being displayed.
    // This is stored here so that it is immediately available after the activity restarts.
    private String mFileName = null;
    private String mFilePath = null;
    private String mFileTitle = null;
    private String mTextFileContents = null;
    
    private int mState = STATE_INVALID;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_idgames_details);

        // Get mLayout references.
        mLayout = findViewById(R.id.IdgamesDetails_Layout);
        mTitleLayout = findViewById(R.id.IdgamesDetails_TitleLayout);
        mTitleText = findViewById(R.id.IdgamesDetails_Title);
        mRatingView = findViewById(R.id.IdgamesDetails_Rating);
        mVoteCount = findViewById(R.id.IdgamesDetails_VoteCount);
        
        mProgress = findViewById(R.id.IdgamesDetails_Progress);
        mProgress.setBackgroundResource(R.drawable.cacodemon);
        
        // Restore state from a saved instance.
        if (savedInstanceState != null) {
            mFileId = savedInstanceState.getInt("fileId");
            mFileName = savedInstanceState.getString("fileName");
            mFilePath = savedInstanceState.getString("filePath");
            mFileTitle = savedInstanceState.getString("fileTitle");
            mTextFileContents = savedInstanceState.getString("textFileContents");
            
        // Get the file ID to display.
        } else {
            // Test for idgames:// protocol link.
            Uri data = this.getIntent().getData();
            if (data != null && data.getScheme().equals("idgames")) {
                try {
                    mFileId = Integer.parseInt(data.getHost());
                } catch (NumberFormatException e) {
                    mFileId = FILE_ID_INVALID;
                }

            // Use the file id from the regular intent.
            } else {
                mFileId = this.getIntent().getIntExtra("fileId", FILE_ID_INVALID);
            }
        }

        if (mFileId == FILE_ID_INVALID) {
            buildInvalidView();
            invalidateOptionsMenu();

        } else {
            // Build a request for the file ID's info.
            Request request = new Request();
            request.setAction(Request.GET_FILE);
            request.setFileId(mFileId);
            request.setMaxAge(Config.MAXAGE_DETAILS);

            // Run task to fetch file info.
            ResponseTask responseTask = new ResponseTask() {
                @Override
                protected void onPostExecute(Response response) {
                    if (response.getErrorMessage() == null) {
                        if (response.getEntries().size() > 0) {
                            FileEntry responseFile = (FileEntry) response.getEntries().get(0);
                            buildDetailView(responseFile);
                        }
                    }

                    mState = STATE_READY;
                    hideProgressIndicator();
                    invalidateOptionsMenu();
                }

                @Override
                protected void onPreExecute() {
                    mState = STATE_LOADING;
                    showProgressIndicator();
                    invalidateOptionsMenu();
                }
            };
            responseTask.execute(request);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        
        savedInstanceState.putInt("fileId", mFileId);
        savedInstanceState.putString("fileName", mFileName);
        savedInstanceState.putString("filePath", mFilePath);
        savedInstanceState.putString("fileTitle", mFileTitle);
        savedInstanceState.putString("textFileContents", mTextFileContents);
    }

    /**
     * Builds the view so as to display an invalid file.
     */
    private void buildInvalidView() {
        mTitleText.setText(R.string.IdgamesDetails_InvalidTitle);
        mRatingView.setRating(0);
    }

    /**
     * Builds the views to display an IdgamesApi file's information.
     * 
     * @param fileEntry The file to display information from.
     */
    public void buildDetailView(FileEntry fileEntry) {
        if (fileEntry == null)
            return;
        
        // Set title area contents.
        mTitleText.setText(fileEntry.toString());
        mRatingView.setRating((float)fileEntry.getRating());
        
        // Set number of votes.
        if (fileEntry.getVoteCount() == 0) {
            mVoteCount.setText(R.string.IdgamesDetails_NoVotes);
        } else if (fileEntry.getVoteCount() == 1) {
            mVoteCount.setText(R.string.IdgamesDetails_SingleVote);
        } else {
            String votes = getString(R.string.IdgamesDetails_PluralVotes, fileEntry.getVoteCount());
            mVoteCount.setText(votes);
        }
        
        // Create individual sections.
        createSection("Description", true, fileEntry.getDescription());
        createSection("Author", true, fileEntry.getAuthor(), fileEntry.getEmail());
        createSection("File", false, fileEntry.getFileName(), fileEntry.getFileSizeString(), fileEntry.getLocaleDate());
        createSection("Credits", true, fileEntry.getCredits());
        createSection("Based on", false, fileEntry.getBase());
        createSection("Build time", false, fileEntry.getBuildTime());
        createSection("Editors used", false, fileEntry.getEditorsUsed());
        createSection("Bugs", false, fileEntry.getBugs());
        
        List<Review> reviews = fileEntry.getReviews();
        addHeader("Reviews");
        if (reviews.size() > 0) {
            Review review;
            for (int i = 0; i < reviews.size(); i++) {
                review = reviews.get(i);
                addReview(review);
            }
        } else {
            addText("This file has no reviews.", R.layout.idgames_details_listtext, false);
        }
        
        // Store this info for use in other UI functions.
        mFileId = fileEntry.getId();
        mFileName = fileEntry.getFileName();
        mFilePath = fileEntry.getFilePath();
        mFileTitle = fileEntry.toString();
        mTextFileContents = fileEntry.getTextFileContents();
    }
    
    /**
     * Creates a new section of file info.
     * 
     * @param title The title of the section to create.
     * @param texts Variable number of strings to place under this section.
     */
    private void createSection(String title, boolean parseLinks, String... texts) {
        List<String> items = new ArrayList<>();
        
        for (String text : texts) {
            if (text != null && text.trim().length() > 0) {
                items.add(text);
            }
        }
        
        if (items.size() == 0) {
            return;
        }
        
        addHeader(title);
        
        StringBuilder total = new StringBuilder();
        for (String text : items) {
            text = text.trim();
            if (text.length() > 0) {
                total.append(" - ");
                total.append(text);
            }
        }
        addText(total.substring(2), R.layout.idgames_details_listtext, parseLinks);
    }
    
    /**
     * Adds a header to the mLayout view.
     * 
     * @param title The title to give this header.
     * @param resource The resource ID to construct this header from. An ID of 0 will use the default idgames_details_listheader.
     */
    private void addHeader(String title) {
        // Do not create empty headers at all.
        if (title == null || title.length() == 0) {
            return;
        }
        
        // Get resource ID to inflate.
        int resource = R.layout.idgames_details_listheader;

        // Build view.
        View view = getLayoutInflater().inflate(resource, mLayout, false);
        ((TextView)view.findViewById(R.id.IdgamesListHeader_Title)).setText(title);
        mLayout.addView(view);
    }
    
    /**
     * Adds a TextView to the mLayout.
     * 
     * @param text The text to add.
     * @param resource The resource id of the layout to use.
     */
    private void addText(String text, int resource, boolean parseLinks) {
        text = text.trim();
        
        // Do not create empty text layouts at all.
        if (text.length() == 0) {
            return;
        }
        
        // Build view.
        View view = getLayoutInflater().inflate(resource, mLayout, false);
        
        TextView textView = view.findViewById(R.id.IdgamesListText_Text);
        if (parseLinks) {
            textView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        } else {
            textView.setAutoLinkMask(0);
        }
        textView.setText(Html.fromHtml(text));
        
        mLayout.addView(view);
    }
    
    private void addReview(Review review) {
        View view = getLayoutInflater().inflate(R.layout.idgames_details_listreview, mLayout, false);
        
        TextView textView = view.findViewById(R.id.IdgamesListReview_Text);
        TextView usernameView = view.findViewById(R.id.IdgamesListReview_Username);
        RatingView ratingView = view.findViewById(R.id.IdgamesListReview_Rating);
        
        textView.setText(Html.fromHtml(review.getText()));
        usernameView.setText(review.getUsername());
        ratingView.setRating(review.getRating());
        
        mLayout.addView(view);
    }
    
    /**
     * Displays the mProgressBar indicator and hides the rest of the activity.
     */
    private void showProgressIndicator() {
        mTitleLayout.setVisibility(View.GONE);
        mLayout.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.start();
    }
    
    /**
     * Hides the mProgressBar indicator and displays the rest of the activity.
     */
    private void hideProgressIndicator() {
        mTitleLayout.setVisibility(View.VISIBLE);
        mLayout.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.stop();
    }
    
    /**
     * Downloads the current IdgamesFile to the public external download directory.
     * Will use DownloadManager on Ice Cream Sandwich and up, a custom DownloadTask on older versions.
     */
    @SuppressLint("SdCardPath")
    private void downloadFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CALLBACK_DOWNLOAD);
                return;
            }
        }

        // Get the download mirror URL to use.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = sharedPrefs.getString("DownloadMirror", Config.IDGAMES_MIRROR_DEFAULT) + mFilePath + mFileName;

        // Let DownloadManager handle the download.
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(mFileName);
        request.setDescription(mFileTitle);
        request.setAllowedOverRoaming(false);

        try {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileName);
        } catch (IllegalStateException e) {
            Toast.makeText(this, this.getString(R.string.IdgamesDetails_ToastNoDirectory), Toast.LENGTH_SHORT).show();
        }

        DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CALLBACK_DOWNLOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadFile();
                }
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.idgames_details, menu);
        
        // Hide action bar menu items if no details have been loaded yet.
        if (mState != STATE_READY || mFileId == FILE_ID_INVALID) {
            setMenuVisible(menu, false);
        } else {
            setMenuVisible(menu, true);
        }
        
        return true;
    }
    
    /**
     * Sets the visibility of all of a menu's items.
     * 
     * @param menu The menu to change visibility of.
     * @param isVisible True if the items should be visible, false if they should not be.
     */
    private void setMenuVisible(Menu menu, boolean isVisible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(isVisible);
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mFileId == FILE_ID_INVALID) {
            return true;
        }
        
        switch (item.getItemId()) {
            case R.id.MenuDetails_ViewText:
                Intent intent = new Intent(this, TextFileActivity.class);
                intent.putExtra("textfile", mTextFileContents);
                startActivity(intent);
                return true;
                
            case R.id.MenuDetails_Download:
                downloadFile();
                return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
