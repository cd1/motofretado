package com.gmail.cristiandeives.motofretado;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class ActivityDetectionService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final String EXTRA_MESSENGER = "messenger";

    private static final String TAG = ActivityDetectionService.class.getSimpleName();
    private static final int ACTIVITY_CHANGED_REQUEST_CODE = 1;
    private static final int DETECTION_INTERVAL = 5000; // ms

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mReceiverPendingIntent;
    private BroadcastReceiver mResultReceiver;
    private IntentFilter mReceiverIntentFilter;
    private Messenger mMessenger;

    @UiThread
    private void requestActivityUpdates() {
        Log.d(TAG, "registering BroadcastReceiver");
        registerReceiver(mResultReceiver, mReceiverIntentFilter);
        Log.d(TAG, "subscribing to activity updates");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                DETECTION_INTERVAL, mReceiverPendingIntent);
    }

    @UiThread
    private void removeActivityUpdates() {
        Log.d(TAG, "unsubscribing from activity updates");
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,
                mReceiverPendingIntent);
        Log.d(TAG, "unregistering BroadcastReceiver");
        unregisterReceiver(mResultReceiver);
    }

    @UiThread
    private void sendMessage(int what) {
        Message msg = Message.obtain(null, what);
        try {
            mMessenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message to Handler", ex);
        }
    }

    @Override
    @MainThread
    public void onCreate() {
        Log.v(TAG, "> onCreate()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();

        mResultReceiver = new ActivityDetectionResultReceiver();
        mReceiverIntentFilter = new IntentFilter(ActivityDetectionResultReceiver.RESULT_ACTION);

        Log.v(TAG, "< onCreate()");
    }

    @Override
    @MainThread
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        Log.v(TAG, "> onStartCommand(intent=" + intent + ", flags=" + flags + ", startId=" + startId + ")");

        Bundle extras = intent.getExtras();
        mMessenger = (Messenger) extras.get(EXTRA_MESSENGER);

        Intent receiverIntent = new Intent(ActivityDetectionResultReceiver.RESULT_ACTION);
        receiverIntent.putExtra(ActivityDetectionResultReceiver.EXTRA_MESSENGER, mMessenger);
        mReceiverPendingIntent = PendingIntent.getBroadcast(this, ACTIVITY_CHANGED_REQUEST_CODE,
                receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "connecting to Google Play Services");
        mGoogleApiClient.connect();

        int ret = START_REDELIVER_INTENT;

        Log.v(TAG, "< onStartCommand(intent=" + intent + ", flags=" + flags + ", startId=" + startId + "): " + ret);
        return ret;
    }

    @Override
    @MainThread
    public void onDestroy() {
        Log.v(TAG, "> onDestroy()");

        removeActivityUpdates();

        Log.d(TAG, "disconnecting from Google Play Services");
        mGoogleApiClient.disconnect();
        sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED);

        Log.v(TAG, "< onDestroy()");
    }

    @Override
    @MainThread
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind(intent=" + intent + "): null");
        return null;
    }

    @Override
    @MainThread
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.v(TAG, "> onConnected(connectionHint=" + connectionHint + ")");

        requestActivityUpdates();
        sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED);

        Log.v(TAG, "< onConnected(connectionHint=" + connectionHint + ")");
    }

    @Override
    @MainThread
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "> onConnectionSuspended(cause=" + cause + ")");

        removeActivityUpdates();
        sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED);

        Log.v(TAG, "< onConnectionSuspended(cause=" + cause + ")");
    }

    @Override
    @MainThread
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.v(TAG, "> onConnectionFailed(result=" + result + ")");

        Log.e(TAG, "Google Play Services connection failed: " + result.getErrorMessage());
        sendMessage(TrackBusPresenter.MyHandler.MSG_GMS_CONNECTION_FAILED);
        stopSelf();

        Log.v(TAG, "< onConnectionFailed(result=" + result + ")");
    }
}
