package com.agolikov.wearbalance.services;

import android.content.Intent;
import android.net.Uri;

import com.agolikov.wearbalance.BalanceCheckActivity;
import com.agolikov.wearbalance.WearBalanceMainActivity;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by agolikov on 24/09/14.
 */
public class WearListenerService extends WearableListenerService {

    private static final String BALANCE_CHECKER_WEAR_PATH = "/balance-checker-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(BALANCE_CHECKER_WEAR_PATH)) {
            Intent balanceCheckActivityIntent = new Intent(this, BalanceCheckActivity.class);
            balanceCheckActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(balanceCheckActivityIntent);
        }

    }

}
