package com.gmail.cristiandeives.motofretado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Messenger
import android.support.annotation.MainThread
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

@MainThread
internal class ActivityDetectionResultReceiver : BroadcastReceiver() {
    companion object {
        internal val RESULT_ACTION = "${ActivityDetectionResultReceiver::class.java.`package`.name}.RESULT_ACTION"
        internal const val EXTRA_MESSENGER = "messenger"

        private val TAG = ActivityDetectionResultReceiver::class.java.simpleName
        private const val MINIMUM_CONFIDENCE = 75 // %
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "> onReceive(context=$context, intent=$intent)")

        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity = result.mostProbableActivity
            Log.d(TAG, mostProbableActivity.toString())

            if (mostProbableActivity.confidence >= MINIMUM_CONFIDENCE) {
                val messenger = intent.extras?.get(EXTRA_MESSENGER) as Messenger

                when (mostProbableActivity.type) {
                    DetectedActivity.ON_FOOT -> messenger.sendMessage(TrackBusPresenter.MyHandler.MSG_USER_IS_ON_FOOT)
                    DetectedActivity.IN_VEHICLE -> messenger.sendMessage(TrackBusPresenter.MyHandler.MSG_USER_IS_IN_VEHICLE)
                    else -> Log.d(TAG, "user is in an irrelevant activity type; ignoring")
                }
            } else {
                Log.d(TAG, "most probable activity doesn't meet minimum confidence level")
            }
        } else {
            Log.wtf(TAG, "Intent doesn't have activity recognition result; ignoring")
        }

        Log.v(TAG, "< onReceive(context=$context, intent=$intent)")
    }
}