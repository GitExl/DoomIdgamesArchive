package nl.exl.doomidgamesarchive.tasks;

import java.lang.ref.WeakReference;

import nl.exl.doomidgamesarchive.IdgamesListAdapter;
import nl.exl.doomidgamesarchive.idgamesapi.FileEntry;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;

public class VoteUpdateTask extends ResponseTask {

    private WeakReference<IdgamesListAdapter> listAdapterReference;

    public VoteUpdateTask(IdgamesListAdapter context) {
        listAdapterReference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(Response response) {
        IdgamesListAdapter listAdapter = listAdapterReference.get();
        if (listAdapter == null) {
            return;
        }

        if (response.getEntries().size() == 0) {
            return;
        }

        FileEntry fileEntry = (FileEntry) response.getEntries().get(0);
        listAdapter.updateVote(fileEntry);
    }

}
