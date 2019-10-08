package nl.exl.doomidgamesarchive.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import nl.exl.doomidgamesarchive.R;

public class AboutActivity extends AppCompatActivity {

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
