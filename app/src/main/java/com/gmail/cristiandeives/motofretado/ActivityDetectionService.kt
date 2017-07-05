package com.gmail.cristiandeives.motofretado

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.support.annotation.MainThread
import android.support.annotation.UiThread
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition

@MainThread
internal class ActivityDetectionService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    companion object {
        internal const val EXTRA_MESSENGER = "messenger"

        private val TAG = ActivityDetectionService::class.java.simpleName
        private const val ACTIVITY_CHANGED_REQUEST_CODE = 1
        private const val DETECTION_INTERVAL = 5000L // ms
    }

    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mResultReceiver: BroadcastReceiver
    private lateinit var mReceiverIntentFilter: IntentFilter
    private lateinit var mMessenger: Messenger
    private var mReceiverPendingIntent: PendingIntent? = null

    override fun onCreate() {
        Log.v(TAG, "> onCreate()")

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build()

        mResultReceiver = ActivityDetectionResultReceiver()
        mReceiverIntentFilter = IntentFilter(ActivityDetectionResultReceiver.RESULT_ACTION)

        Log.v(TAG, "< onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "> onStartCommand(intent=$intent, flags=$flags, startId=$startId)")

        val messenger = intent.extras?.get(EXTRA_MESSENGER)
        if (messenger is Messenger) {
            mMessenger = messenger
            val receiverIntent = Intent(ActivityDetectionResultReceiver.RESULT_ACTION)
            receiverIntent.putExtra(ActivityDetectionResultReceiver.EXTRA_MESSENGER, mMessenger)
            mReceiverPendingIntent = PendingIntent.getBroadcast(
                    this,
                    ACTIVITY_CHANGED_REQUEST_CODE,
                    receiverIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            Log.d(TAG, "connecting to Google Play Services")
            mGoogleApiClient.connect()
        } else {
            Log.w(TAG, "could not find a Messenger in the Intent")
        }

        val ret = START_REDELIVER_INTENT

        Log.v(TAG, "< onStartCommand(intent=$intent, flags=$flags, startId=$startId): $ret")
        return ret
    }

    override fun onDestroy() {
        Log.v(TAG, "> onDestroy()")

        removeActivityUpdates()

        Log.d(TAG, "disconnecting from Google Play Services")
        mGoogleApiClient.disconnect()

        mMessenger.sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED)

        Log.v(TAG, "< onDestroy()")
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.v(TAG, "onBind(intent=$intent): null")
        return null
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.v(TAG, "> onConnected(connectionHint=$connectionHint)")

        requestActivityUpdates()
        mMessenger.sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED)

        Log.v(TAG, "< onConnected(connectionHint=$connectionHint)")
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.v(TAG, "> onConnectionSuspended(cause=$cause)")

        removeActivityUpdates()
        mMessenger.sendMessage(TrackBusPresenter.MyHandler.MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED)

        Log.v(TAG, "< onConnectionSuspended(cause=$cause)")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.v(TAG, "> onConnectionFailed(result=$result)")

        Log.e(TAG, "Google Play Services connection failed: ${result.errorMessage}")
        mMessenger.sendMessage(TrackBusPresenter.MyHandler.MSG_GMS_CONNECTION_FAILED)
        stopSelf()

        Log.v(TAG, "< onConnectionFailed(result=$result)")
    }

    @UiThread
    private fun requestActivityUpdates() {
        Log.d(TAG, "registering BroadcastReceiver")
        registerReceiver(mResultReceiver, mReceiverIntentFilter)

        Log.d(TAG, "subscribing to activity updates")
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                DETECTION_INTERVAL, mReceiverPendingIntent)
    }

    @UiThread
    private fun removeActivityUpdates() {
        Log.d(TAG, "unsubscribing from activity updates")
        if (mGoogleApiClient.isConnected) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,
                    mReceiverPendingIntent)

            Log.d(TAG, "unregistering BroadcastReceiver")
            unregisterReceiver(mResultReceiver)
        }
    }
}