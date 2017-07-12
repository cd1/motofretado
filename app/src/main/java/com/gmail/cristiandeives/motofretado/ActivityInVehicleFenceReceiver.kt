package com.gmail.cristiandeives.motofretado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Messenger
import android.support.annotation.MainThread
import android.util.Log
import com.google.android.gms.awareness.fence.FenceState

@MainThread
internal class ActivityInVehicleFenceReceiver : BroadcastReceiver() {
    companion object {
        internal val RESULT_ACTION = "${ActivityInVehicleFenceReceiver::class.java.`package`.name}.RESULT_ACTION"
        internal const val EXTRA_MESSENGER = "messenger"

        private val TAG = ActivityInVehicleFenceReceiver::class.java.simpleName
    }

    private val FenceState.currentStateString: String
        get() = when(currentState) {
            FenceState.TRUE -> "TRUE"
            FenceState.FALSE -> "FALSE"
            else -> "UNKNOWN"
        }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "> onReceive(context=$context, intent=$intent)")

        val fenceState = FenceState.extract(intent)
        val messenger = intent.extras?.get(EXTRA_MESSENGER) as Messenger

        if (fenceState.fenceKey == ActivityInVehicleFenceService.FENCE_KEY) {
            Log.d(TAG, "fence state: ${fenceState.currentStateString}")
            when (fenceState.currentState) {
                FenceState.TRUE -> messenger.sendMessage(TrackBusPresenter.MyHandler.MSG_USER_IS_IN_VEHICLE)
                FenceState.FALSE -> messenger.sendMessage(TrackBusPresenter.MyHandler.MSG_USER_IS_ON_FOOT)
            }
        } else {
            Log.d(TAG, "unexpected fence key: ${fenceState.fenceKey}")
        }

        Log.v(TAG, "< onReceive(context=$context, intent=$intent)")
    }
}