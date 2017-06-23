package com.gmail.cristiandeives.motofretado;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class UpdateLocationBroadcastReceiver extends BroadcastReceiver {
    public static final String STOP_SERVICE_ACTION =
            UpdateLocationBroadcastReceiver.class.getPackage().getName()
                    + ".STOP_LOCATION_SERVICE_ACTION";

    private static final String TAG = UpdateLocationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "> onReceive([Context], [Intent])");

        if (TextUtils.equals(intent.getAction(), STOP_SERVICE_ACTION)) {
            Intent serviceIntent = new Intent(context, UpdateLocationService.class);
            context.stopService(serviceIntent);
        } else {
            Log.w(TAG, "I don't know what to do with the action " + intent.getAction());
        }

        Log.v(TAG, "< onReceive([Context], [Intent])");
    }
}
