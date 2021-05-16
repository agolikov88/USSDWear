package com.agolikov.wearbalance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.agolikov.wearbalance.utils.BalanceCheckUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

public class BalanceCheckActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private static final long BALANCE_TIMEOUT_MILLISECONDS = 10000; //10 seconds

    private BalanceCheckReceiver mBalanceCheckReceiver;
    private IntentFilter mIntentFilter;

    private GoogleApiClient mGoogleApiClient;
    private Node mDeviceNode;

    private Timer mBalanceTimeoutTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = getNewGoogleApiClient();
        mGoogleApiClient.connect();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        BalanceCheckUtils.checkBalance(this,
                getSharedPreferences(BalanceCheckUtils.SHARED_PREFS_NAME,
                        Context.MODE_PRIVATE).getString(BalanceCheckUtils.USSD_NUMBER_PREFERENCE, BalanceCheckUtils.DEFAULT_USSD_REQUEST));

        mBalanceCheckReceiver = new BalanceCheckReceiver();
        mIntentFilter = new IntentFilter(BalanceCheckUtils.BALANCE_CHECK_INTENT_ACTION);
        mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(mBalanceCheckReceiver, mIntentFilter);

        startBalanceTimeoutTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
        BalanceCheckUtils.restoreScreenOffTimeoutToPrevious(BalanceCheckActivity.this);
        unregisterReceiver(mBalanceCheckReceiver);
    }

    private void startBalanceTimeoutTimer() {
        TimerTask balanceTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BalanceCheckUtils.setScreenOffTimeoutToMinimum(BalanceCheckActivity.this);
                    }
                });
            }
        };
        mBalanceTimeoutTimer.schedule(balanceTimeoutTask, BALANCE_TIMEOUT_MILLISECONDS);
    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                if (nodes.getNodes().size() > 0) {
                    mDeviceNode = nodes.getNodes().get(0);    //assume only one device can be connected so take the first node
                }
            }
        });
    }

    private void sendMessage(Node node, String message) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                    BalanceCheckUtils.BALANCE_CHECKER_WEAR_PATH, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            BalanceCheckUtils.setScreenOffTimeoutToMinimum(BalanceCheckActivity.this);
                        }
                    }
            );
        } else {
            BalanceCheckUtils.setScreenOffTimeoutToMinimum(BalanceCheckActivity.this);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private GoogleApiClient getNewGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
    }

    public class BalanceCheckReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == BalanceCheckUtils.BALANCE_CHECK_INTENT_ACTION) {
                mBalanceTimeoutTimer.cancel();
                //send message to wearable
                if(mDeviceNode != null) {
                    sendMessage(mDeviceNode, intent.getStringExtra(BalanceCheckUtils.BALANCE_STRING_INTENT_EXTRA));
                } else {
                    //lock screen back
                    //set screen timeout to 1 second to turn it off then restore previous value in broadcast receiver
                    BalanceCheckUtils.setScreenOffTimeoutToMinimum(BalanceCheckActivity.this);
                }
            } else if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                BalanceCheckActivity.this.finish();
            }
        }

    }

}
