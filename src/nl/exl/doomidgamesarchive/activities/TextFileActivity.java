package nl.exl.doomidgamesarchive.activities;

import nl.exl.doomidgamesarchive.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Displays the contents of an IdgamesApi file's text file.
 */
public class TextFileActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idgames_textfile);
        
        // Set text file data.
        TextView text = (TextView)findViewById(R.id.IdgamesText_Text);
        String textData = this.getIntent().getExtras().getString("textfile");
        if (textData != null) {
            text.setText(textData);
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        this.finish();
    }
}
