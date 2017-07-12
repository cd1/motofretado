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
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.DetectedActivityFence
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

@MainThread
internal class ActivityInVehicleFenceService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    companion object {
        internal const val EXTRA_MESSENGER = "messenger"
        internal const val FENCE_KEY = "FENCE_KEY"

        private val TAG = ActivityInVehicleFenceService::class.java.simpleName
        private const val ACTIVITY_CHANGED_REQUEST_CODE = 1
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
                .addApi(Awareness.API)
                .build()

        mResultReceiver = ActivityInVehicleFenceReceiver()
        mReceiverIntentFilter = IntentFilter(ActivityInVehicleFenceReceiver.RESULT_ACTION)

        Log.v(TAG, "< onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "> onStartCommand(intent=$intent, flags=$flags, startId=$startId)")

        val messenger = intent.extras?.get(EXTRA_MESSENGER)
        if (messenger is Messenger) {
            mMessenger = messenger
            val receiverIntent = Intent(ActivityInVehicleFenceReceiver.RESULT_ACTION)
            receiverIntent.putExtra(ActivityInVehicleFenceReceiver.EXTRA_MESSENGER, mMessenger)
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
        Log.d(TAG, "registering activity fence")
        val fence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE)
        val registerRequest = FenceUpdateRequest.Builder()
                .addFence(FENCE_KEY, fence, mReceiverPendingIntent)
                .build()
        Awareness.FenceApi.updateFences(mGoogleApiClient, registerRequest).setResultCallback { result ->
            if (result.isSuccess) {
                Log.v(TAG, "fence registered successfully")
            } else {
                Log.v(TAG, "fence failed to register: ${result.statusMessage}")
            }
        }

        Log.d(TAG, "registering BroadcastReceiver")
        registerReceiver(mResultReceiver, mReceiverIntentFilter)
    }

    @UiThread
    private fun removeActivityUpdates() {
        Log.d(TAG, "unregistering activity fence")
        if (mGoogleApiClient.isConnected) {
            val unregisterRequest = FenceUpdateRequest.Builder()
                    .removeFence(FENCE_KEY)
                    .build()
            Awareness.FenceApi.updateFences(mGoogleApiClient, unregisterRequest).setResultCallback { result ->
                if (result.isSuccess) {
                    Log.v(TAG, "fence unregistered successfully")
                } else {
                    Log.v(TAG, "fence failed to unregister: ${result.statusMessage}")
                }
            }

            Log.d(TAG, "unregistering BroadcastReceiver")
            unregisterReceiver(mResultReceiver)
        }
    }
}