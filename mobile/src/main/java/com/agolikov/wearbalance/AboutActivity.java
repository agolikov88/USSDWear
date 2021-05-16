package com.agolikov.wearbalance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.agolikov.wearbalance.R;

public class AboutActivity extends Activity {

    private static final String FEEDBACK_EMAIL_ADDRESS = "agolikov88@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void sendFeedback(View view) {
        /* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL_ADDRESS});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.feedback_email_subject);

        /* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_email_chooser_title)));
    }

    public void showTour(View view) {
        startActivity(new Intent(this, TourActivity.class));
    }

}
