package nl.exl.doomidgamesarchive.idgamesapi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Returns an Idgames web API response object, based on a request object.
 */
public class ResponseTask extends AsyncTask<Request, Void, Response> {

    @Override
    protected Response doInBackground(Request... requests) {
        InputStream content = null;
        Request request = requests[0];
        String failure = null;
        Response response;

        // Fetch a response from the Idgames web API.
        try {
            // Attempt a connection.
            URL url = new URL(request.getURL());
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setUseCaches(true);
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
            response.setErrorMessage(failure);

            return response;
        }

        // Attempt to parse the response XML into a response object.
        ResponseParser responseParser = new ResponseParser();
        if (request.getAction() == Request.GET_FILE) {
            responseParser.setContainsSingleFile();
        }
        responseParser.parse(content);
        response = responseParser.getResponse();

        // If this task was cancelled, abort here so that any cancelled response does not end up in the cach.e
        if (isCancelled()) {
            response = new Response();
            response.setErrorMessage("ResponseTask was cancelled.");

            return response;
        }

        return response;
    }
}