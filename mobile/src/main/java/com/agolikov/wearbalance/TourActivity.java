package com.agolikov.wearbalance;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.agolikov.wearbalance.utils.BalanceCheckUtils;

public class TourActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BalanceCheckUtils.setFirstLaunchFlag(this, false);
    }

    public void startUsingApp(View view) {
        finish();
    }

}
