package nl.exl.doomidgamesarchive.tasks;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import nl.exl.doomidgamesarchive.activities.DetailsActivity;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesdb.File;
import nl.exl.doomidgamesarchive.idgamesdb.IdgamesDb;
import nl.exl.doomidgamesarchive.idgamesdb.Image;

public class FileImageTask extends AsyncTask<FileEntry, Void, Image> {

    private IdgamesDb mDb;

    private WeakReference<DetailsActivity> mDetailsActivity;

    public FileImageTask(DetailsActivity context) {
        mDetailsActivity = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        DetailsActivity activity = mDetailsActivity.get();
        mDb = IdgamesDb.getInstance(activity);
    }

    @Override
    protected Image doInBackground(FileEntry... entries) {
        FileEntry entry = entries[0];
        if (entry == null) {
            return null;
        }

        File idgamesFile = mDb.files().findByPath(entry.getFilePath() + entry.getFileName());
        if (idgamesFile == null) {
            return null;
        }

        return mDb.images().findByFile(idgamesFile.id);
    }

    @Override
    protected void onPostExecute(Image image) {
        DetailsActivity activity = mDetailsActivity.get();
        activity.setImage(image);
    }
}
