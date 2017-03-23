package nl.exl.doomidgamesarchive.activities;

import nl.exl.doomidgamesarchive.R;
import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_about);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        this.finish();
    }
}
