package nl.exl.doomidgamesarchive;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import nl.exl.doomidgamesarchive.idgamesapi.DirectoryEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Entry;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;
import nl.exl.doomidgamesarchive.idgamesapi.VoteEntry;

/**
 * Provides list item Views from IdgamesApi entries for a ListView.
 */
class IdgamesListAdapter extends ArrayAdapter<Entry> {
    // Adds list indices in front of listitem titles if true.
    private boolean mAddListIndex = false;
    
    // Layout mInflater reference.
    private LayoutInflater mInflater = null;
    
    
    /**
     * Comparator for IdgamesApi entries.
     * Directories are sorted before files and votes 
     */
    private static class EntryComparator implements Comparator<Entry> {
        public int compare(Entry lhs, Entry rhs) {
            if (lhs instanceof DirectoryEntry && !(rhs instanceof DirectoryEntry)) {
                return -1;
            } else if (rhs instanceof DirectoryEntry && !(lhs instanceof DirectoryEntry)) {
                return 1;
            } else {
                return lhs.toString().compareToIgnoreCase(rhs.toString());
            }
        }
    }
    
    /**
     * Helper class to keep view references in a view, preventing repeated lookups.
     */
    private static class ViewHolder {
        TextView index;
        TextView title;
        TextView subtitle;
        TextView date;
        RatingView rating;
    }
    
    
    IdgamesListAdapter(Context context) {
        super(context, R.layout.idgames_listitem, R.id.IdgamesListItem_Title);
        
        mInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Locale locale = this.getContext().getResources().getConfiguration().locale;
        ViewHolder holder;
        
        // Create a new listitem View.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.idgames_listitem, parent, false);
            
            // Store holder class with references to child views.
            holder = new ViewHolder();
            holder.index = convertView.findViewById(R.id.IdgamesListItem_Index);
            holder.title = convertView.findViewById(R.id.IdgamesListItem_Title);
            holder.subtitle = convertView.findViewById(R.id.IdgamesListItem_Subtitle);
            holder.date = convertView.findViewById(R.id.IdgamesListItem_Date);
            holder.rating = convertView.findViewById(R.id.IdgamesListItem_Rating);
            convertView.setTag(holder);
            
        // Reuse an existing listitem View.
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        // Hide views that might or might not receive content, if they exist at all.
        if (holder.date != null) {
            holder.date.setVisibility(View.GONE);
        }
        if (holder.index != null) {
            holder.index.setVisibility(View.GONE);
        }
        if (holder.rating != null) {
            holder.rating.setVisibility(View.GONE);
        }

        // Get the IdgamesApi entry for this list position.
        Entry entry = this.getItem(position);
        if (entry == null) {
            return convertView;
        }
        
        // Set the View's title.
        if (mAddListIndex) {
            holder.index.setText(String.format(locale, "%1$d", position + 1));
            holder.index.setVisibility(View.VISIBLE);
        }
        holder.title.setText(entry.toString());
        
        // Fill view with directory info.
        if (entry instanceof DirectoryEntry) {
            holder.subtitle.setText(R.string.IdgamesList_Directory);
            holder.subtitle.setMaxLines(1);

        // Fill view with file info.
        } else if (entry instanceof FileEntry) {
            FileEntry fileEntry = (FileEntry)entry;
            StringBuilder subText = new StringBuilder();

            subText.append(fileEntry.getAuthor());

            // Add date.
            String date = fileEntry.getLocaleDate();
            if (date != null && date.length() > 0) {

                if (holder.date == null) {
                    subText.append(" - ");
                    subText.append(date);
                } else {
                    holder.date.setText(date);
                    holder.date.setVisibility(View.VISIBLE);
                }
            }
            
            // Add file size.
            if (fileEntry.getFileSize() > 0) {
                subText.append(" - ");
                subText.append(fileEntry.getFileSizeString());
            }

            holder.subtitle.setText(subText.toString());
            holder.subtitle.setMaxLines(1);
            
            holder.rating.setRating((float)fileEntry.getRating());
            holder.rating.setVisibility(View.VISIBLE);

        // Fill view with vote info.
        } else if (entry instanceof VoteEntry) {
            VoteEntry voteEntry = (VoteEntry)entry;
            
            String reviewText = voteEntry.getReviewText();
            if (reviewText != null && reviewText.trim().length() > 0) {
                holder.subtitle.setMaxLines(10);
                holder.subtitle.setText(reviewText);
                holder.subtitle.setVisibility(View.VISIBLE);
            } else {
                holder.subtitle.setVisibility(View.GONE);
            }
            
            holder.rating.setRating((float)voteEntry.getRating());
            holder.rating.setVisibility(View.VISIBLE);
        }
        
        return convertView;
    }

    /**
     * Loads additional entry information for vote entries that have no title.
     */
    void fixVotes() {
        Entry entry;
        VoteEntry voteEntry;
        ResponseTask responseTask;
        Request request;
        String title;
        
        // Remember entries that have been fixed already, to prevent multiple requests for votes with the same file id.
        Set<Entry> finishedEntries = new HashSet<Entry>();
        
        for (int i = 0; i < getCount(); i++) {
            entry = getItem(i);
            if (entry instanceof VoteEntry) {
                voteEntry = (VoteEntry)entry;
                
                // Only fix votes with an empty title.
                title = voteEntry.getTitle();
                if (!finishedEntries.contains(entry) && title == null || title.length() == 0) {
                    finishedEntries.add(entry);
                    
                    // Execute a new request for file details.
                    request = new Request();
                    request.setAction(Request.GET_FILE);
                    request.setMaxAge(Config.MAXAGE_NEWVOTES);
                    request.setFileId(voteEntry.getFileId());
                    
                    responseTask = new ResponseTask() {
                        @Override
                        protected void onPostExecute(Response response) {
                            // Update the corresponding vote with a new title.
                            if (response.getEntries().size() > 0) {
                                FileEntry fileEntry = (FileEntry)response.getEntries().get(0);
                                updateVote(fileEntry);
                            }
                        }
                    };
                    responseTask.execute(request);
                }
            }
        }
    }
    
    /**
     * Updates an individual IdgamesApi vote in this adapter's data.
     * 
     * @param fileEntry The file entry to update a vote list item with.
     */
    private void updateVote(FileEntry fileEntry) {
        Entry entry;
        VoteEntry voteEntry;
        
        // Search the entire adapter for votes matching the file's id.
        for (int i = 0; i < getCount(); i++) {
            entry = getItem(i);
            
            if (entry instanceof VoteEntry) {
                voteEntry = (VoteEntry)entry;
                
                // Set the vote's new title.
                // We do not break out of the loop because a file id might occur multiple times inside this adapter's data.
                if (voteEntry.getFileId() == fileEntry.getId()) {
                    voteEntry.setTitle(fileEntry.toString());
                }
            }   
        }
        
        this.notifyDataSetChanged();
    }
    
    /**
     * Sorts this adapter's data.
     */
    void sort() {
        this.sort(new EntryComparator());
    }
    
    void setAddListIndex(boolean addListIndex) {
        this.mAddListIndex = addListIndex;
    }
}
