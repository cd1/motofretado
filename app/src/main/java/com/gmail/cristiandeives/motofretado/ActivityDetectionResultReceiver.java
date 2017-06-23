package com.gmail.cristiandeives.motofretado;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.MainThread;
import android.support.annotation.UiThread;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityDetectionResultReceiver extends BroadcastReceiver {
    static final String RESULT_ACTION = ActivityDetectionResultReceiver.class.getPackage().getName() + ".RESULT_ACTION";
    static final String EXTRA_MESSENGER = "messenger";

    private static final String TAG = ActivityDetectionResultReceiver.class.getSimpleName();
    private static final int MINIMUM_CONFIDENCE = 75; // %

    @UiThread
    private static void fetchMessengerAndSendMessage(Intent intent, int what) {
        Bundle extras = intent.getExtras();
        Messenger messenger = (Messenger) extras.get(EXTRA_MESSENGER);
        if (messenger == null) {
            Log.w(TAG, "could not find a Messenger in the Intent");
            return;
        }

        Message msg = Message.obtain(null, what);
        try {
            messenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message", ex);
        }
    }

    @Override
    @MainThread
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "> onReceive(context=" + context + ", intent=" + intent + ")");

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            Log.d(TAG, mostProbableActivity.toString());

            if (mostProbableActivity.getConfidence() >= MINIMUM_CONFIDENCE) {
                switch (mostProbableActivity.getType()) {
                    case DetectedActivity.ON_FOOT:
                        fetchMessengerAndSendMessage(intent,
                                TrackBusPresenter.MyHandler.MSG_USER_IS_ON_FOOT);
                        break;
                    case DetectedActivity.IN_VEHICLE:
                        fetchMessengerAndSendMessage(intent,
                                TrackBusPresenter.MyHandler.MSG_USER_IS_IN_VEHICLE);
                        break;
                    default:
                        Log.d(TAG, "user is in an irrelevant activity type; ignoring");
                }

            } else {
                Log.d(TAG, "most probable activity doesn't meet minimum confidence level");
            }
        } else {
            Log.wtf(TAG, "Intent doesn't have activity recognition result; ignoring");
        }

        Log.v(TAG, "< onReceive(context=" + context + ", intent=" + intent + ")");
    }
}
