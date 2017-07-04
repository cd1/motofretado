package com.gmail.cristiandeives.motofretado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.annotation.MainThread
import android.util.Log

@MainThread
internal class UpdateLocationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        internal val STOP_SERVICE_ACTION = "${UpdateLocationBroadcastReceiver::class.java.`package`.name}.STOP_LOCATION_SERVICE_ACTION"

        private val TAG = UpdateLocationBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "> onReceive(context=$context, intent=$intent)")

        if (intent.action == STOP_SERVICE_ACTION) {
            val serviceIntent = Intent(context, UpdateLocationService::class.java)
            context.stopService(serviceIntent)
        } else {
            Log.w(TAG, "I don't know what to do with the action ${intent.action}")
        }

        Log.v(TAG, "< onReceive(context=$context, intent=$intent)")
    }
}