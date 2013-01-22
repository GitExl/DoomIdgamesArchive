/**
 * Copyright (c) 2012, Dennis Meuwissen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package nl.exl.doomidgamesarchive;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import nl.exl.doomidgamesarchive.idgamesapi.DirectoryEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Entry;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Request;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;
import nl.exl.doomidgamesarchive.idgamesapi.VoteEntry;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Provides list item Views from IdgamesApi entries for a ListView.
 */
public class IdgamesListAdapter extends ArrayAdapter<Entry> {
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
	public static class ViewHolder {
		TextView title;
		TextView subtitle;
		RatingView rating;
	}
	
	
	public IdgamesListAdapter(Context context) {
		super(context, R.layout.idgames_listitem, R.id.IdgamesListItem_Title);
		
		mInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		// Create a new listitem View.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.idgames_listitem, null);
			
			// Store holder class with references to child views.
			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.IdgamesListItem_Title);
			holder.subtitle = (TextView)convertView.findViewById(R.id.IdgamesListItem_Subtitle);
			holder.rating = (RatingView)convertView.findViewById(R.id.IdgamesListItem_Rating);
			convertView.setTag(holder);
		
		// Reuse an existing listitem View.
		} else {
			holder = (ViewHolder)convertView.getTag();
		}

		// Get the IdgamesApi entry for this list position.
		Entry entry = this.getItem(position);
		if (entry == null) {
			return convertView;
		}
		
		// Set the View's title.
		String title = null;
		if (mAddListIndex == true) {
			title = Integer.toString(position + 1) + ". " + entry.toString();
		} else {
			title = entry.toString();
		}
		holder.title.setText(title);
		
		// Fill view with directory info.
		if (entry instanceof DirectoryEntry) {
			holder.subtitle.setText("Directory");
			holder.subtitle.setMaxLines(1);
			
			holder.rating.setVisibility(View.GONE);

		// Fill view with file info.
		} else if (entry instanceof FileEntry) {
			FileEntry fileEntry = (FileEntry)entry;
			
			String subText = fileEntry.getAuthor();
			
			// Add date.
			String date = fileEntry.getLocaleDate();
			if (date != null && date.length() > 0) {
				subText += " - " + date;
			}
			
			// Add file size.
			if (fileEntry.getFileSize() > 0) {
				subText += " - " + fileEntry.getFileSizeString();
			}

			holder.subtitle.setText(subText);
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
	public void fixVotes() {
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
					
					responseTask = new ResponseTask(getContext()) {
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
	 * @param fileEntry
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
	public void sort() {
		this.sort(new EntryComparator());
	}
	
	public void setAddListIndex(boolean addListIndex) {
		this.mAddListIndex = addListIndex;
	}
}
