package nl.exl.doomidgamesarchive.tasks;

import java.lang.ref.WeakReference;

import nl.exl.doomidgamesarchive.IdgamesListFragment;
import nl.exl.doomidgamesarchive.idgamesapi.Response;
import nl.exl.doomidgamesarchive.idgamesapi.ResponseTask;

public class ListTask extends ResponseTask {

    private WeakReference<IdgamesListFragment> listFragmentReference;

    public ListTask(IdgamesListFragment context) {
        listFragmentReference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(Response response) {
        IdgamesListFragment listFragment = listFragmentReference.get();
        listFragment.setResponse(response);
    }

}
