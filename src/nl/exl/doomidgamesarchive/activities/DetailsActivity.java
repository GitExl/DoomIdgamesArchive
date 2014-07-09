package nl.exl.doomidgamesarchive.activities;

import java.util.ArrayList;
import java.util.List;

import nl.exl.doomidgamesarchive.Config;
import nl.exl.doomidgamesarchive.DownloadTask;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.RatingView;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;
import nl.exl.doomidgamesarchive.idgamesapi.Review;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	
    // Layout references.
    private LinearLayout mLayout;
    private RelativeLayout mTitleLayout;
    private ImageView mProgress;
    private TextView mTitleText;
    private RatingView mRatingView;
    private TextView mVoteCount;

    // ID of the IdgamesApi file being displayed.
    private int mFileId = -1;
    
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
        mLayout = (LinearLayout)findViewById(R.id.IdgamesDetails_Layout);
        mTitleLayout = (RelativeLayout)findViewById(R.id.IdgamesDetails_TitleLayout);
        mTitleText = (TextView)findViewById(R.id.IdgamesDetails_Title);
        mRatingView = (RatingView)findViewById(R.id.IdgamesDetails_Rating);
        mVoteCount = (TextView)findViewById(R.id.IdgamesDetails_VoteCount);
        
        mProgress = (ImageView)findViewById(R.id.IdgamesDetails_Progress);
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
        		mFileId = Integer.parseInt(data.getHost());
        		// TODO: Bad file Id. Show dialog, or set activity ui state to show message?

			// Use the file id from the regular intent.
        	} else {
        		mFileId = this.getIntent().getIntExtra("fileId", -1);
        	}
        }
        
        // Build a request for the file ID's info.
        Request request = new Request();
        request.setAction(Request.GET_FILE);
        request.setFileId(mFileId);
        request.setMaxAge(Config.MAXAGE_DETAILS);
        
        // Run task to fetch file info.
        ResponseTask responseTask = new ResponseTask(this) {
            @Override
            protected void onPostExecute(Response response) {
                if (response.getErrorMessage() == null) {
                    if (response.getEntries().size() > 0) {
                        FileEntry responseFile = (FileEntry)response.getEntries().get(0);
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
            mVoteCount.setText("No votes yet");
        } else if (fileEntry.getVoteCount() == 1) {
            mVoteCount.setText(fileEntry.getVoteCount() + " vote");
        } else {
            mVoteCount.setText(fileEntry.getVoteCount() + " votes");
        }
        
        // Create individual sections.
        createSection("Description", fileEntry.getDescription());
        createSection("Author", fileEntry.getAuthor(), fileEntry.getEmail());
        createSection("File", fileEntry.getFileName(), fileEntry.getFileSizeString(), fileEntry.getLocaleDate());
        createSection("Credits", fileEntry.getCredits());
        createSection("Based on", fileEntry.getBase());
        createSection("Build time", fileEntry.getBuildTime());
        createSection("Editors used", fileEntry.getEditorsUsed());
        createSection("Bugs", fileEntry.getBugs());
        
        List<Review> reviews = fileEntry.getReviews();
        addHeader("Reviews", 0);
        if (reviews.size() > 0) {
            Review review;
            for (int i = 0; i < reviews.size(); i++) {
                review = reviews.get(i);
                addReview(review);
            }
        } else {
            addText("This file has no reviews.", R.layout.idgames_details_listtext);
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
    private void createSection(String title, String... texts) {
        List<String> items = new ArrayList<String>();
        
        for (String text : texts) {
            if (text != null && text.trim().length() > 0) {
                items.add(text);
            }
        }
        
        if (items.size() == 0) {
            return;
        }
        
        addHeader(title, 0);
        
        StringBuilder total = new StringBuilder();
        for (String text : items) {
            text = text.trim();
            if (text.length() > 0) {
            	total.append(" - ");
            	total.append(text);
            }
        }
        addText(total.substring(2), R.layout.idgames_details_listtext);
    }
    
    /**
     * Adds a header to the mLayout view.
     * 
     * @param title The title to give this header.
     * @param resource The resource ID to construct this header from. An ID of 0 will use the default idgames_details_listheader.
     */
    private void addHeader(String title, int resource) {
        // Do not create empty headers at all.
        if (title == null || title.length() == 0) {
            return;
        }
        
        // Get resource ID to inflate.
        if (resource == 0) {
            resource = R.layout.idgames_details_listheader;
        }
        
        // Build view.
        View view = getLayoutInflater().inflate(resource, null);
        ((TextView)view.findViewById(R.id.IdgamesListHeader_Title)).setText(title);
        mLayout.addView(view);
    }
    
    /**
     * Adds a TextView to the mLayout.
     * 
     * @param text The text to add.
     * @param resource The resource id of the layout to use.
     */
    private void addText(String text, int resource) {
        text = text.trim();
        
        // Do not create empty text layouts at all.
        if (text == null || text.length() == 0) {
            return;
        }
        
        // Build view.
        View view = getLayoutInflater().inflate(resource, null);
        
        TextView textView = (TextView)view.findViewById(R.id.IdgamesListText_Text);
        textView.setText(Html.fromHtml(text));
        
        mLayout.addView(view);
    }
    
    private void addReview(Review review) {
    	View view = getLayoutInflater().inflate(R.layout.idgames_details_listreview, null);
        
        TextView textView = (TextView)view.findViewById(R.id.IdgamesListReview_Text);
        TextView usernameView = (TextView)view.findViewById(R.id.IdgamesListReview_Username);
        RatingView ratingView = (RatingView)view.findViewById(R.id.IdgamesListReview_Rating);
        
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
        // Get the download mirror URL to use.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = sharedPrefs.getString("DownloadMirror", Config.IDGAMES_MIRROR_GREECE) + mFilePath + mFileName;
        
        // Use DownloadManager from ICS and upwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(mFileName);
            request.setDescription(mFileTitle);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileName);
            request.setAllowedOverRoaming(false);
            
            DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
            
        // Use a simpler, custom downloader for older or badly supported Android versions.
        } else {
            String downloadPath = "/sdcard/Downloads";
            
            // Build the download path.
            java.io.File path = new java.io.File(downloadPath);
            
            // Attempt to create the destination directory, to ensure it does exists.
            path.mkdirs();
            
            // Verify if the download path now exists.
            if (!path.exists()) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Cannot access SD card")
                    .setMessage("Insert or mount an SD card to be able to store downloaded Idgames files.")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("OK", null).create();
                alertDialog.show();
                
                return;
            }
            
            // Start the downloader task.
            DownloadTask task = new DownloadTask(this);
            task.setTitle("Doom Idgames Archive download");
            task.setDestinationPath(downloadPath);
            
            // Attach an intent that views the file's details.
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("fileId", mFileId);
            task.setIntent(PendingIntent.getActivity(this, 0, intent, 0));
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                task.execute(url);
            } else {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            }
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.idgames_details, menu);
        
        // Hide action bar menu items if no details have been loaded yet.
        if (mState != STATE_READY) {
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
        if (mFileId == -1) {
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
