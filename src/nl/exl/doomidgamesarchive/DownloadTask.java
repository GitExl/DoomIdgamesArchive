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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * ResponseTask to download files in a separate thread. Makes use of notifications to update
 * the user of progress and success\failure.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {
    // Notification manager id.
    private static final int NOTIFICATION_ID = 0x57ABBED;
    
    // Size of the download buffer, in bytes.
    // Larger values use less processing time, but more memory.
    private static final int BUFFER_SIZE = 1024 * 256;

    // A context reference.
    private Context mContext;
    
    // The intent that will be triggered when this download's notification is selected.
    private PendingIntent mIntent;
    
    // The title to display in notifications.
    private String mTitle;
    
    // The filename of the file currently being downloaded.
    private String mFileName;
    
    // The path to download files to.
    private String mDestinationPath;
    
    // Notification management objects.
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder; 
    
    
    public DownloadTask(Context context) {
        mContext = context;
    }
    
    @Override
    protected void onPostExecute(String result) {
        // Display a notification with the result of the download.
        mNotifyBuilder.setProgress(0, 0, false);
        mNotifyBuilder.setContentText(result);
        
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    };
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.av_download_large);
        
        // Build the initial notification style.
        mNotifyBuilder = new NotificationCompat.Builder(mContext);
        mNotifyBuilder.setSmallIcon(R.drawable.av_download_small);
        mNotifyBuilder.setLargeIcon(largeIcon);
        mNotifyBuilder.setContentTitle(mTitle);
        mNotifyBuilder.setContentText("Starting download...");
        mNotifyBuilder.setContentIntent(mIntent);
        mNotifyBuilder.setProgress(100, 0, false);
        
        mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }
    
    @Override
    protected String doInBackground(String... params) {
        try {
            // Attempt to connect to the supplied URL.
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            
            // Make sure the connection's response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                Log.e("DownloadTask", "Could not connect: HTTP error code " + connection.getResponseCode());
                return "Download failed, could not connect.";
            }
            
            // Check for valid content length.
            int size = connection.getContentLength();
            if (size < 1) {
                Log.e("DownloadTask", "Could not connect: invalid content length.");
                return "Download failed, could not connect.";
            }
            
            // Get just the filename from the URL. 
            mFileName = new File(url.getFile()).getName();
            
            int downloaded = 0;
            float progress = 0;
            int read;
            byte buffer[];
            
            OutputStream output = new FileOutputStream(mDestinationPath + "/" + mFileName);
            InputStream stream = connection.getInputStream();
           
            // Read data into a buffer and write it to disk until there is no more data to be read.
            while (downloaded < size) {
                // Resize buffer to match the last bytes in the input stream.
                if (size - downloaded > BUFFER_SIZE) {
                    buffer = new byte[BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                
                // Read from input stream.
                read = stream.read(buffer);
                if (read == -1)
                    break;
                
                // Write buffer to disk.
                output.write(buffer, 0, read);
                downloaded += read;
                
                // Update progress of this task.
                progress = ((float)downloaded / (float)size) * 100;
                publishProgress((int)progress);
            }
            
            output.close();
            stream.close();
            
        } catch (FileNotFoundException e) {
            Log.e("DownloadTask", "Could not create file.");
            return "Download failed, could not creae file.";
            
        } catch (MalformedURLException e) {
            Log.e("DownloadTask", "Malformed URL " + params[0] + ": " + e.toString());
            return "Download failed, URL is malformed.";
            
        } catch (IOException e) {
            Log.e("DownloadTask", "IO exception: " + e.toString());
            return "Download failed, I/O exception.";
        }        
        
        return "Download of " + mFileName + " complete.";
    }
    
    @Override
    protected void onProgressUpdate(Integer... progress) {
        // Update the notification's progress bar.
        mNotifyBuilder.setProgress(100, progress[0], false);
        mNotifyBuilder.setContentText("Downloading " + mFileName + " - " + progress[0] + "%");
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    };
    
    public void setDestinationPath(String path) {
        mDestinationPath = path;
    }
    
    public void setTitle(String title) {
        mTitle = title;
    }
    
    public void setIntent(PendingIntent intent) {
        mIntent = intent;
    }
}