package com.agolikov.wearbalance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.agolikov.wearbalance.R;
import com.agolikov.wearbalance.utils.BalanceCheckUtils;

public class ConfigureUssdServiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_ussd_service);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(BalanceCheckUtils.ifUssdServiceRunning(this)) {
            if(BalanceCheckUtils.ifFirstLaunch(this)) {
                startActivity(new Intent(this, TourActivity.class));
            }
            finish();
        }
    }

    public void openAccessibilitySettings(View view) {
        Intent openSettingsIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(openSettingsIntent);
    }

}
