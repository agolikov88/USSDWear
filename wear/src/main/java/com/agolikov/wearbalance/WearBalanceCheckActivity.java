package com.agolikov.wearbalance;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

public class WearBalanceCheckActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener,
        NodeStatusListener {

    private static final int NOTIFICATION_ID = 255;
    private static final long MESSAGE_TIMEOUT_MILLISECONDS = 10000; //10 seconds

    private GoogleApiClient mGoogleApiClient;

    private static final String BALANCE_CHECKER_WEAR_PATH = "/balance-checker-wear";

    private ProgressBar mProgressBar;
    private TextView mLoadingLabel;
    private ImageView mDisconnectedImage;

    private Timer mMessageTimeoutTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wear_balance_check);

        initUI();
    }

    private void initUI() {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                mLoadingLabel = (TextView) findViewById(R.id.loading_label);
                mDisconnectedImage = (ImageView) findViewById(R.id.disconnected_icon);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient = getNewGoogleApiClient();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private GoogleApiClient getNewGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
    }

    private void resolveNode(final NodeStatusListener nodeStatusListener) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                if (nodes.getNodes().size() > 0) {
                    Node node = nodes.getNodes().get(0);    //assume only one device can be connected so take the first node
                    if (node != null) {
                        nodeStatusListener.onNodeResolved(node);
                    } else {
                        nodeStatusListener.onNodeUnresolved();
                    }
                } else {
                    nodeStatusListener.onNodeUnresolved();
                }
            }
        });
    }

    private void sendMessage(Node node) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), BALANCE_CHECKER_WEAR_PATH, null).setResultCallback (
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()) {
                                startMessageTimeoutTimer();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showFailureScreen();
                                    }
                                });
                            }
                        }
                    }
            );
        } else {
            showFailureScreen();
        }
    }

    private void showFailureScreen() {
        if(mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mLoadingLabel.setText(R.string.disconnected_label);
            mDisconnectedImage.setVisibility(View.VISIBLE);
        }
    }

    private void startMessageTimeoutTimer() {
        TimerTask messageTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFailureScreen();
                    }
                });
            }
        };
        mMessageTimeoutTimer.schedule(messageTimeoutTask, MESSAGE_TIMEOUT_MILLISECONDS);
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(BALANCE_CHECKER_WEAR_PATH)) {
            //cancel timeout timer
            mMessageTimeoutTimer.cancel();
            //cancel previous notification
            ((NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);

            Intent restartIntent = new Intent(this, WearBalanceCheckActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, restartIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(WearBalanceCheckActivity.this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            getResources(), R.drawable.notification_background))
                    .setContentTitle(getString(R.string.app_name))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setContentIntent(contentIntent)
                    .setContentText(new String(messageEvent.getData()));
            ((NotificationManager) WearBalanceCheckActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        }
        WearBalanceCheckActivity.this.finish();
    }

    @Override
    public void onNodeResolved(Node node) {
        sendMessage(node);
    }

    @Override
    public void onNodeUnresolved() {
        showFailureScreen();
    }

}
