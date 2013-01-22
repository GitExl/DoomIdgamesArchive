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

package nl.exl.doomidgamesarchive.idgamesapi;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * IdgamesApi directory entry.
 */
public class DirectoryEntry extends Entry {
	// The database ID of this directory entry.
	private int mId = -1;
	
	// The name of this directory.
	private String mName;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toJSON(JSONObject obj) throws JSONException {
		obj.put("id", mId);
		obj.put("name", mName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromJSON(JSONObject obj) throws JSONException {
		mId = obj.getInt("id");
		mName = obj.getString("name");
	}
	
	public void setId(int id) {
		mId = id;
	}

	public void addName(String name) {
		if (mName == null) {
			mName = name;
		} else {
			mName += name;
		}
	}
	
	public int getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public String toString() {
		String[] paths = mName.split(Pattern.quote(File.separator));
		return paths[paths.length - 1];
	}
}
