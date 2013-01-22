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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Returns an Idgames web API response object, based on a request object.
 */
public class ResponseTask extends AsyncTask<Request, Void, Response> {
	// The static response cache.
	private static ResponseCache mCache;

	
	public ResponseTask(Context context) {
		// Instantiate the response cache if it does not yet exist.
		if (mCache == null) {
			mCache = new ResponseCache(context);
		}
	}
	
	@Override
    protected Response doInBackground(Request... requests) {
		InputStream content = null;
		Request request = requests[0];
		String failure = null;
		
		// If the response exists in the cache, use that one.
		Response response = null;
		try {
			response = mCache.get(request, request.getMaxAge());
		} catch (IOException e) {
			Log.e("ResponseTask", "Cannot get response from cache: " + e.toString());
		}
		
		// Fetch a response from the Idgames web API.
		if (response == null) {
			long startTime = System.currentTimeMillis();
			
	        try {
	        	// Attempt a connection.
				URL url = new URL(request.getURL());
				HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
				content = new BufferedInputStream(urlConnection.getInputStream());
			} catch (MalformedURLException e) {
				Log.w("ResponseTask", "Malformed URL: " + e.toString());
				failure = e.toString();
			} catch (IOException e) {
				Log.w("ResponseTask", "IO exception while retrieving data: " + e.toString());
				failure = e.toString();
			}
	     
	        // Return an error message in the response object if anything went wrong.
	        if (failure != null) {
		     	response = new Response();
				response.setErrorType("Exception");
				response.setErrorMessage(failure);
				
		    	return response;
			}
	        
	        Log.i("ResponseTask", "Download took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
	        startTime = System.currentTimeMillis();
	
	        // Attempt to parse the response XML into a response object.
			ResponseParser responseParser = new ResponseParser();
			if (request.getAction() == Request.GET_FILE) {
				responseParser.setContainsSingleFile(true);
			}
			responseParser.parse(content);
			response = responseParser.getResponse();
			
			Log.i("ResponseTask", "Parsing took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
			
			// If this task was cancelled, abort here so that any cancelled response does not end up in the cach.e
			if (isCancelled()) {
				response = new Response();
				response.setErrorType("Cancelled");
				response.setErrorMessage("ResponseTask was cancelled.");

		    	return response;
			}
			
			// Put the response object in the cache for future use.
			try {
				mCache.put(request, response);
			} catch (IOException e) {
				Log.e("ResponseTask", "Cannot put response in mCache: " + e.toString());
			}
		}
		
		return response;
    }
}