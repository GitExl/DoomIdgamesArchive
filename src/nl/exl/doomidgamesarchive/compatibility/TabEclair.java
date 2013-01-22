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
package nl.exl.doomidgamesarchive.compatibility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * A tab object for Eclair SDKs up to Gingerbread SDKs.
 */
public class TabEclair extends Tab {
	private FragmentActivity mActivity;
	private Tab.TabListener mListener;
	private Fragment mFragment;
	private String mTag;
	private String mText;
	
	
	protected TabEclair(FragmentActivity activity, Fragment fragment, String tag) {
		mActivity = activity;
		mFragment = fragment;
		mTag = tag;
	}
	
	@Override
	public void setText(int textResource) {
		mText = mActivity.getResources().getString(textResource);
	}
	
	@Override
	public String getText() {
		return mText;
	}

	@Override
	public void setListener(TabListener listener) {
		mListener = listener;
	}
	
	@Override
	public TabListener getListener() {
		return mListener;
	}

	@Override
	public Object getTab() {
		return null;
	}

	@Override
	public String getTag() {
		return mTag;
	}

	@Override
	public Fragment getFragment() {
		return mFragment;
	}
	
	@Override
	public void setFragment(Fragment fragment) {
		mFragment = fragment;
	}
}
