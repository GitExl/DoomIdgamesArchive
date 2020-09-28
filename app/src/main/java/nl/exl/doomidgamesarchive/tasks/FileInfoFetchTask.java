package nl.exl.doomidgamesarchive.tasks;

import java.lang.ref.WeakReference;

import nl.exl.doomidgamesarchive.activities.DetailsActivity;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;

/**
 * Task for fetching information about a single file.
 */
public class FileInfoFetchTask extends ResponseTask {
    private WeakReference<DetailsActivity> activityReference;

    public FileInfoFetchTask(DetailsActivity context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(Response response) {
        DetailsActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        FileEntry responseFile = null;
        if (response.getErrorMessage() == null) {
            if (response.getEntries().size() > 0) {
                responseFile = (FileEntry) response.getEntries().get(0);
            }
        }

        activity.setFile(responseFile);
    }
}
