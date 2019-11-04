package nl.exl.doomidgamesarchive.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import nl.exl.doomidgamesarchive.Config;
import nl.exl.doomidgamesarchive.R;
import nl.exl.doomidgamesarchive.RatingView;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;
import nl.exl.doomidgamesarchive.idgamesapi.Review;
import nl.exl.doomidgamesarchive.idgamesdb.Image;
import nl.exl.doomidgamesarchive.tasks.FileImageTask;
import nl.exl.doomidgamesarchive.tasks.FileInfoFetchTask;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Displays details from an IdgamesApi file.
 * Builds a number of views to form a custom mLayout.
 */
public class DetailsActivity extends AppCompatActivity {

    // Activity states for UI choices.
    public static final int STATE_INVALID = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2;

    // Invalid file.
    private static final int FILE_ID_INVALID = -1;

    // Permission requests.
    private static final int PERMISSION_CALLBACK_DOWNLOAD = 1;

    private static final String META_BASE_URL = "https://f000.backblazeb2.com/file/idgames-meta/";
    
    // Layout references.
    private LinearLayout mLayoutInfo;
    private LinearLayout mLayoutReviews;
    private NestedScrollView mScroller;
    private ImageView mProgress;
    private ImageView mHeaderImage;
    private TextView mTitleView;
    private RatingView mRatingView;
    private CoordinatorLayout mHeader;
    private TextView mVoteCount;
    private RelativeLayout mTitleLayout;
    private CollapsingToolbarLayout mToolbarLayout;
    private RelativeLayout mToolbarLayoutBackground;

    private FileEntry mFile;
    private Image mIdgamesImage;

    private int mState = STATE_INVALID;

    private boolean mFileCompleted;
    private boolean mImageCompleted;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_idgames_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get mLayout references.
        mLayoutInfo = findViewById(R.id.IdgamesDetails_LayoutInfo);
        mLayoutReviews = findViewById(R.id.IdgamesDetails_LayoutReviews);
        mScroller = findViewById(R.id.IdgamesDetails_Scroller);
        mTitleView = findViewById(R.id.IdgamesDetails_Title);
        mRatingView = findViewById(R.id.IdgamesDetails_Rating);
        mHeader = findViewById(R.id.IdgamesDetail_Header);
        mVoteCount = findViewById(R.id.IdgamesDetails_VoteCount);
        mTitleLayout = findViewById(R.id.IdgamesDetails_TitleLayout);
        mHeaderImage = findViewById(R.id.IdgamesDetails_Image);
        mToolbarLayout = findViewById(R.id.IdgamesDetails_ToolbarLayout);
        mToolbarLayoutBackground = findViewById(R.id.IdgamesDetails_ToolbarBackground);
        
        mProgress = findViewById(R.id.IdgamesDetails_Progress);
        mProgress.setBackgroundResource(R.drawable.cacodemon);

        setState(DetailsActivity.STATE_LOADING);
        int fileId = getFileIdParameter();
        getFileInfo(fileId);

        // Set the minimum height of the toolbar layout so that it doesn't collapse to less than
        // the title's layout height.
        final ViewTreeObserver observer = mTitleLayout.getViewTreeObserver();
        final ViewTreeObserver.OnGlobalLayoutListener titleLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int currentMinHeight = mToolbarLayout.getMinimumHeight();
                int targetMinHeight = mTitleLayout.getHeight();
                if (currentMinHeight != targetMinHeight) {
                    mToolbarLayout.setMinimumHeight(targetMinHeight);
                }
            }
        };
        observer.addOnGlobalLayoutListener(titleLayoutListener);
    }

    private int getFileIdParameter() {

        // Test for idgames:// protocol link.
        Uri data = getIntent().getData();
        if (data != null && data.getScheme().equals("idgames")) {
            String host = data.getHost();
            if (host == null) {
                return FILE_ID_INVALID;
            } else {
                try {
                    return Integer.parseInt(host);
                } catch (NumberFormatException e) {
                    return FILE_ID_INVALID;
                }
            }
        }

        // Get the file ID to display from the intent.
        return getIntent().getIntExtra("fileId", FILE_ID_INVALID);
    }

    private void getFileInfo(int fileId) {
        mFileCompleted = false;
        mImageCompleted = false;

        if (fileId == FILE_ID_INVALID) {
            buildInvalidView();
            invalidateOptionsMenu();
            return;
        }

        // Build a request for the file ID's info.
        Request request = new Request();
        request.setAction(Request.GET_FILE);
        request.setFileId(fileId);
        request.setMaxAge(Config.MAXAGE_DETAILS);

        // Run task to fetch file info.
        ResponseTask responseTask = new FileInfoFetchTask(this);
        responseTask.execute(request);
    }

    public void setImage(Image idgamesImage) {
        mIdgamesImage = idgamesImage;

        if (idgamesImage != null) {
            mToolbarLayoutBackground.setBackground(new ColorDrawable(0xFF000000 | idgamesImage.color));

            // Scale height to correct aspect ratio to 4:3, but only for 8:5 aspect ratio images.
            int width = idgamesImage.width;
            int height = idgamesImage.height;
            if ((double)width / (double)height == 8.0 / 5.0) {
                height = (int)Math.ceil(idgamesImage.height * 1.2);
            }

            // Generate a temporary bitmap as placeholder.
            // TODO: Using a ColorDrawable and setBounds did not work. Other more efficient options?
            Bitmap bitmap =  Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
            Drawable placeholder = new BitmapDrawable(getResources(), bitmap);

            // TODO: when loading a cached version, the override seems to not take effect.
            Glide.with(this)
                .load(META_BASE_URL + idgamesImage.path)
                .override(width, height)
                .placeholder(placeholder)
                .transition(withCrossFade())
                .into(mHeaderImage);
        }

        mImageCompleted = true;
        updateCompletion();
    }

    public void setFile(FileEntry file) {
        mFile = file;
        if (file != null) {
            buildDetailView();
        } else {
            buildInvalidView();
        }

        // Fetch additional info for the file.
        FileImageTask imageTask = new FileImageTask(this);
        imageTask.execute(mFile);

        mFileCompleted = true;
        updateCompletion();
    }

    private void updateCompletion() {
        if (mFileCompleted && mImageCompleted) {
            setState(STATE_READY);
        }
    }

    /**
     * Builds the view so as to display an invalid file.
     */
    private void buildInvalidView() {
        mRatingView.setRating(0);
    }

    /**
     * Builds the views to display an IdgamesApi file's information.
     */
    public void buildDetailView() {
        mTitleView.setText(mFile.toString());
        mRatingView.setRating((float)mFile.getRating());

        // Set number of votes.
        if (mFile.getVoteCount() == 0) {
            mVoteCount.setText(R.string.IdgamesDetails_NoVotes);
        } else if (mFile.getVoteCount() == 1) {
            mVoteCount.setText(R.string.IdgamesDetails_SingleVote);
        } else {
            String votes = getString(R.string.IdgamesDetails_PluralVotes, mFile.getVoteCount());
            mVoteCount.setText(votes);
        }
        
        // Create individual sections.
        createSection("Description", true, mFile.getDescription());
        createSection("Author", true, mFile.getAuthor(), mFile.getEmail());
        createSection("File", false, mFile.getFileName(), mFile.getLocaleDate(), mFile.getFileSizeString());
        createSection("Credits", true, mFile.getCredits());
        createSection("Based on", false, mFile.getBase());
        createSection("Build time", false, mFile.getBuildTime());
        createSection("Editors used", false, mFile.getEditorsUsed());
        createSection("Bugs", false, mFile.getBugs());
        
        List<Review> reviews = mFile.getReviews();
        if (reviews.size() > 0) {
            Review review;
            for (int i = 0; i < reviews.size(); i++) {
                review = reviews.get(i);
                addReview(review);
            }
        }
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
        addText(total.substring(2), parseLinks);
    }
    
    /**
     * Adds a header to the mLayout view.
     * 
     * @param title The title to give this header.
     */
    private void addHeader(String title) {

        // Do not create empty headers at all.
        if (title == null || title.length() == 0) {
            return;
        }
        
        // Get resource ID to inflate.
        int resource = R.layout.idgames_details_listheader;

        // Build view.
        View view = getLayoutInflater().inflate(resource, mLayoutInfo, false);
        ((TextView)view.findViewById(R.id.IdgamesListHeader_Title)).setText(title);
        mLayoutInfo.addView(view);
    }
    
    /**
     * Adds a TextView to the mLayout.
     * 
     * @param text The text to add.
     */
    private void addText(String text, boolean parseLinks) {
        text = text.trim();
        if (text.length() == 0) {
            return;
        }

        // Inflate and configure the text view that gets added to the info layout.
        View view = getLayoutInflater().inflate(R.layout.idgames_details_listtext, mLayoutInfo, false);
        TextView textView = view.findViewById(R.id.IdgamesListText_Text);
        if (parseLinks) {
            textView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        } else {
            textView.setAutoLinkMask(0);
        }
        textView.setText(Html.fromHtml(text));
        
        mLayoutInfo.addView(view);
    }

    /**
     * Adds a review layout.
     *
     * @param review Review object to add a layout for.
     */
    private void addReview(Review review) {
        View view = getLayoutInflater().inflate(R.layout.idgames_details_listreview, mLayoutReviews, false);
        
        TextView textView = view.findViewById(R.id.IdgamesListReview_Text);
        TextView usernameView = view.findViewById(R.id.IdgamesListReview_Username);
        RatingView ratingView = view.findViewById(R.id.IdgamesListReview_Rating);
        
        textView.setText(Html.fromHtml(review.getText()));
        usernameView.setText(review.getUsername());
        ratingView.setRating(review.getRating());
        
        mLayoutReviews.addView(view);
    }
    
    /**
     * Displays the mProgressBar indicator and hides the rest of the activity.
     */
    private void showProgressIndicator() {
        mHeader.setVisibility(View.GONE);
        mScroller.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.start();
    }
    
    /**
     * Hides the mProgressBar indicator and displays the rest of the activity.
     */
    private void hideProgressIndicator() {
        mHeader.setVisibility(View.VISIBLE);
        mScroller.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.stop();
    }
    
    /**
     * Downloads the current IdgamesFile to the public external download directory.
     * Will use DownloadManager on Ice Cream Sandwich and up, a custom DownloadTask on older versions.
     */
    private void downloadFile() {

        // Ask for permission if needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_CALLBACK_DOWNLOAD);
                return;
            }
        }

        // Get the download mirror URL to use.
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String url = sharedPrefs.getString("DownloadMirror", Config.IDGAMES_MIRROR_DEFAULT) + mFile.getFilePath() + mFile.getFileName();

        // Let the DownloadManager handle the download.
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(mFile.getFileName());
        request.setDescription(mFile.getTitle());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);

        try {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFile.getFileName());
        } catch (IllegalStateException e) {
            Toast.makeText(this, this.getString(R.string.IdgamesDetails_ToastNoDirectory), Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) {
            Toast.makeText(this, this.getString(R.string.IdgamesDetails_ToastNoDownloadManager), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            manager.enqueue(request);
            Toast.makeText(this, this.getString(R.string.IdgamesDetails_ToastDownloadStarted), Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            Toast.makeText(this, this.getString(R.string.IdgamesDetails_ToastNoDownloadEnqueue), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CALLBACK_DOWNLOAD &&
            grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadFile();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.idgames_details, menu);
        
        // Hide action bar menu items if no details have been loaded yet.
        if (mState != STATE_READY || mFile == null) {
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

    /**
     * {@inheritDoc}
     */
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mFile == null) {
            return true;
        }
        
        switch (item.getItemId()) {
            case R.id.MenuDetails_ViewText:
                Intent intent = new Intent(this, TextFileActivity.class);
                intent.putExtra("textfile", mFile.getTextFileContents());
                startActivity(intent);
                return true;
                
            case R.id.MenuDetails_Download:
                downloadFile();
                return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set the current UI state.
     *
     * @param state The new state of this activity.
     */
    public void setState(int state) {
        mState = state;
        if (state == STATE_LOADING) {
            showProgressIndicator();
        } else {
            hideProgressIndicator();
        }
        invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
