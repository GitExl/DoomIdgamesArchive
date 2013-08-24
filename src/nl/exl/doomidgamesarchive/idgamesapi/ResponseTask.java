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
                content = new BufferedInputStream(urlConnection.getInputStream(), 8192);
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
                Log.e("ResponseTask", "Cannot put response in cache: " + e.toString());
            }
        }
        
        return response;
    }
}