package com.agolikov.wearbalance.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.agolikov.wearbalance.utils.BalanceCheckUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by agolikov on 14/09/14.
 */
public class BalanceReaderService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();

        BalanceCheckUtils.setUssdReaderServiceRunningFlag(this, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BalanceCheckUtils.setUssdReaderServiceRunningFlag(this, false);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source == null) {
            return;
        }

        // Grab the parent of the view that fired the event.
        AccessibilityNodeInfo childNode = source.getChild(0);
        if (childNode == null) {
            return;
        }

        CharSequence resultText = childNode.getText();
        if(resultText != null) {
            //dismiss ussd dialog
            childNode = source.getChild(1);
            if(childNode != null && BalanceCheckUtils.ifCalledByApp(this)) {
                childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                BalanceCheckUtils.setBalanceCallerFlag(this, false);
            }
            //send broadcast to balance check activity
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(BalanceCheckUtils.BALANCE_CHECK_INTENT_ACTION);
            broadcastIntent.putExtra(BalanceCheckUtils.BALANCE_STRING_INTENT_EXTRA, resultText.toString());
            sendBroadcast(broadcastIntent);
            Intent broadcastTestIntent = new Intent();
            broadcastTestIntent.setAction(BalanceCheckUtils.USSD_REQUEST_TEST_INTENT_ACTION);
            broadcastTestIntent.putExtra(BalanceCheckUtils.BALANCE_STRING_INTENT_EXTRA, resultText.toString());
            sendBroadcast(broadcastTestIntent);
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

}
