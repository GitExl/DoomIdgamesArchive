package nl.exl.doomidgamesarchive;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.List;

import androidx.fragment.app.Fragment;
import nl.exl.doomidgamesarchive.idgamesapi.DirectoryEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Entry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;

/**
 * A fragment containing a list of IdGamesApi entries.
 * It will adapt what it displays based on the API mRequest type.
 */
public class IdgamesListFragment extends Fragment implements OnItemClickListener {
    // Sorts the list alphabetically if true.
    private boolean mSort = false;
    
    // Adds list index numbers before ListView titles if true.
    private boolean mAddListIndex = false;
    
    // Request associated with this fragment.
    private Request mRequest;
    
    // Tool view references.
    private RelativeLayout mBrowseTools;
    private RelativeLayout mSearchTools;
    
    // View references.
    private ImageView mProgress;
    private TextView mPathText;
    private TextInputEditText mSearchField;
    private Spinner mSearchSpinner;
    private RelativeLayout mMessageContainer;
    private TextView mMessage;

    // Search spinner adapter.
    private ArrayAdapter<CharSequence> mSearchSpinnerAdapter;
    
    // The currently selected search settings.
    private String mSearchQuery;
    private int mSearchCategory;
    
    // Entry list.
    private ListView mEntryListView;
    private IdgamesListAdapter mEntryAdapter;
    
    // List select mListener interface for the containing activity.
    private IdgamesListener mListener;
    
    // ResponseTask used to receive data.
    private ResponseTask mTask;
    
    
    /**
     * Interface for implementing events coming from this fragment.
     */
    public interface IdgamesListener {
        void onEntrySelected(IdgamesListFragment fragment, Entry entry);
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle arguments = getArguments();
        
        // Restore the mRequest state from saved instance or from new arguments.
        mRequest = new Request();
        if (savedInstanceState == null) {
            mRequest.restoreFromBundle(arguments);
        } else {
            mRequest.restoreFromBundle(savedInstanceState);
        }

        // Set view arguments.
        mAddListIndex = arguments.getBoolean("addListIndex", false);
        mSort = arguments.getBoolean("sort", false);
    };
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mRequest.saveToBundle(outState);
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_idgames_list, container, false);
        
        mProgress = view.findViewById(R.id.IdgamesList_Progress);
        mProgress.setBackgroundResource(R.drawable.cacodemon);
        
        mMessageContainer = view.findViewById(R.id.IdgamesList_MessageContainer);
        mMessage = view.findViewById(R.id.IdgamesList_Message);

        mPathText = view.findViewById(R.id.IdgamesList_Path);
        mBrowseTools = view.findViewById(R.id.IdgamesList_BrowseTools);
        
        mSearchField = view.findViewById(R.id.IdgamesList_SearchField);
        mSearchSpinner = view.findViewById(R.id.IdgamesList_SearchSpinner);
        mSearchTools = view.findViewById(R.id.IdgamesList_SearchTools);
        
        // Set up search field actions.
        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSearchQuery = v.getText().toString();
                    executeSearch();
                }
                return false;
            }
        });
        mSearchField.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    mSearchQuery = ((TextView)v).getText().toString();
                    executeSearch();
                }
                return false;
            }
        });
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mSearchQuery = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Set up search spinner.
        mSearchSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.search_types, android.R.layout.simple_spinner_item);
        mSearchSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSearchSpinner.setAdapter(mSearchSpinnerAdapter);
        
        mSearchSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
                // Convert the position straight to a category id. Not pretty, but as long
                // as the ordering in the source array is the same, not a problem.
                mSearchCategory = position;
                executeSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        
        // Set up entry ListView and adapter.
        mEntryAdapter = new IdgamesListAdapter(this.getActivity());
        mEntryListView = view.findViewById(R.id.IdgamesList_List);
        mEntryListView.setAdapter(mEntryAdapter);
        mEntryListView.setOnItemClickListener(this);
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (mTask != null) {
            mTask.cancel(true);
        }
    };
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (IdgamesListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IdgamesListener");
        }
    }
    
    /**
     * Sets the maximum amount of entries to display in this list. Modified the mRequest to do so.
     * 
     * @param limit The maximum number of entries to display.
     */
    public void setLimit(int limit) {
        if (this.mRequest != null) {
            this.mRequest.setLimit(limit);
        } else {
            Log.w("IdgamesListFragment", "Request is null, cannot set limit.");
        }
    }
    
    private void executeSearch() {
        if (mSearchQuery == null || mSearchQuery.length() == 0) {
            return;
        }
        
        if (mSearchQuery.length() <= 2) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Query too short")
                .setMessage("Enter a search query of at least 3 characters.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", null)
                .create();
            alertDialog.show();
        
            return;
        }
        
        mRequest.setQuery(mSearchQuery);
        mRequest.setCategory(mSearchCategory);
        updateList();
        
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchField.getWindowToken(), 0);
    }
    
    /**
     * Update the list with data from a new IdgamesApi response.
     */
    public void updateList() {
        if (mRequest == null) {
            Log.w("IdgamesListFragment", "No Request defined. IdgamesListFragment will not display anything.");
            return;
        }
        
        if (mRequest.getAction() == Request.SEARCH) {
            if (mSearchQuery == null || mSearchQuery.length() == 0) {
                showRelevantTools();
                return;
            }
        }
        
        mEntryAdapter.clear();
        
        // Construct a new IdgamesApi mTask.
        mTask = new ResponseTask() {
            @Override
            protected void onPostExecute(Response response) {
                String warning = response.getWarningType();
                if (warning != null && warning.equals("Limit Warning")) {
                    Toast.makeText(getActivity(), "The search returned too many results. Not all of them are displayed.", Toast.LENGTH_LONG).show();
                }
                
                mEntryAdapter.setAddListIndex(mAddListIndex);
                
                // Add all entries to list adapter.
                mEntryAdapter.clear();
                List<Entry> entries = response.getEntries();
                for (int i = 0; i < entries.size(); i++) {
                    mEntryAdapter.add(entries.get(i));
                }
                
                if (mSort) {
                    mEntryAdapter.sort();
                } else {
                    mEntryAdapter.notifyDataSetChanged();
                }
                
                if (entries.size() == 0) {
                    hideProgressIndicator("No results.");
                } else {
                    hideProgressIndicator(null);
                }
                
                // Fix entry names for votes without any.
                if (mRequest.getAction() == Request.GET_LATESTVOTES)
                    mEntryAdapter.fixVotes();
            }

            @Override
            protected void onPreExecute() {
                showProgressIndicator();
                showRelevantTools();
            }
        };
        
        mTask.execute(mRequest);
    }
    
    /**
     * Displays the relevant views for the current Idgames action.
     */
    private void showRelevantTools() {
        int action = mRequest.getAction();
        
        // Set view visibility based on the request's action.
        // Search.
        if (action == Request.SEARCH) {
            mBrowseTools.setVisibility(View.GONE);
            mSearchTools.setVisibility(View.VISIBLE);
            
        // Browse.
        } else if (action == Request.GET_CONTENTS) {
            mBrowseTools.setVisibility(View.VISIBLE);
            mSearchTools.setVisibility(View.GONE);
            
            String path = " / " + mRequest.getDirectoryName().replace("/", " / ");
            mPathText.setText(path);

        // Others.
        } else {
            mBrowseTools.setVisibility(View.GONE);
            mSearchTools.setVisibility(View.GONE);
        }
    }
    
    /**
     * Changes this fragment's API browse mRequest to it's parent directory.
     * 
     * @return True if there was a parent directory that was changed to, false if not.
     */
    public boolean enterParentDirectory() {
        // If this is not a directory list, we cannot go back.
        if (mRequest.getDirectoryName() == null)
            return false;
        
        String dirName = mRequest.getDirectoryName();
        
        // If this is the root directory, we cannot go back.
        if (dirName.equals(""))
            return false;

        // Get the parent directory's name.
        File dir = new File(dirName);
        dirName = dir.getParent();
        
        // Make sure the directory name is valid for the Idgames API.
        if (dirName == null) {
            dirName = "";
        } else {
            dirName += "/";
        }
        
        // Cancel any potentially running IdgamesApi mTask.
        if (mTask != null) {
            mTask.cancel(true);
        }
        
        // Update list with the new mRequest data.
        mRequest.setDirectoryName(dirName);
        updateList();
        mEntryListView.scrollTo(0, 0);
        
        return true;
    }
    
    /**
     * Changes this fragment's API browse mRequest to enter a specific directory.
     * 
     * @param dir The directory to enter.
     */
    public void enterDirectory(DirectoryEntry dir) {
        mRequest.setDirectoryName(dir.getName());
        updateList();
        mEntryListView.scrollTo(0, 0);
    }
    
    /**
     * Displays the mProgress indicator view and hides the entry list.
     */
    private void showProgressIndicator() {
        mEntryListView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        mMessageContainer.setVisibility(View.GONE);
        
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.start();
    }
    
    /**
     * Hides the mProgress indicator view and shows the entry list.
     */
    private void hideProgressIndicator(String message) {
        mProgress.setVisibility(View.GONE);
        AnimationDrawable progressAnim = (AnimationDrawable)mProgress.getBackground();
        progressAnim.stop();
        
        if (message != null) {
            mEntryListView.setVisibility(View.GONE);
            mMessage.setText(message);
            mMessageContainer.setVisibility(View.VISIBLE);
        } else {
            mEntryListView.setVisibility(View.VISIBLE);
            mMessageContainer.setVisibility(View.GONE);
        }
    }

    /**
     * List item click events are sent through to this fragment's IdgamesListener.  
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Entry entry = mEntryAdapter.getItem(position);
        mListener.onEntrySelected(this, entry);
    }
}
