package com.agolikov.wearbalance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.agolikov.wearbalance.utils.BalanceCheckUtils;


public class WearBalanceMainActivity extends Activity {

    private EditText mUssdRequestField;
    private TextView mUssdResponseField;

    private BalanceCheckReceiver mBalanceCheckReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!BalanceCheckUtils.ifUssdServiceRunning(this)) {
            startActivity(new Intent(this, ConfigureUssdServiceActivity.class));
        }

        setContentView(R.layout.activity_wear_balance_main);

        mBalanceCheckReceiver = new BalanceCheckReceiver();
        mIntentFilter = new IntentFilter(BalanceCheckUtils.USSD_REQUEST_TEST_INTENT_ACTION);
        mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(mBalanceCheckReceiver, mIntentFilter);

        initUI();
    }

    private void initUI() {
        mUssdRequestField = (EditText) findViewById(R.id.ussd_request_field);
        mUssdRequestField.setText(getSharedPreferences(BalanceCheckUtils.SHARED_PREFS_NAME,
                Context.MODE_PRIVATE).getString(BalanceCheckUtils.USSD_NUMBER_PREFERENCE, BalanceCheckUtils.DEFAULT_USSD_REQUEST));

        mUssdResponseField = (TextView) findViewById(R.id.ussd_response_label);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBalanceCheckReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wear_balance_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkBalance(View view) {
        if(BalanceCheckUtils.ifUssdServiceRunning(this)) {
            SharedPreferences prefs = getSharedPreferences(BalanceCheckUtils.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(BalanceCheckUtils.USSD_NUMBER_PREFERENCE, mUssdRequestField.getText().toString());
            editor.commit();

            BalanceCheckUtils.checkBalance(this, mUssdRequestField.getText().toString());
        } else {
            startActivity(new Intent(this, ConfigureUssdServiceActivity.class));
        }
    }

    public class BalanceCheckReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == BalanceCheckUtils.USSD_REQUEST_TEST_INTENT_ACTION) {
                String ussdResponse = intent.getStringExtra(BalanceCheckUtils.BALANCE_STRING_INTENT_EXTRA);
                if(ussdResponse != null && !ussdResponse.isEmpty()) {
                    mUssdResponseField.setText(ussdResponse);
                } else {
                    mUssdResponseField.setText(R.string.ussd_response_error_label);
                }
            }
        }

    }

}
