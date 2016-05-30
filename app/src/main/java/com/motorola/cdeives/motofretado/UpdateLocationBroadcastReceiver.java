package com.motorola.cdeives.motofretado;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateLocationBroadcastReceiver extends BroadcastReceiver {
    public static final String STOP_SERVICE_ACTION =
            "com.motorola.cdeives.motofretado.STOP_LOCATION_SERVICE_ACTION";

    private static final String TAG = UpdateLocationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "> onReceive([Context], [Intent])");

        switch (intent.getAction()) {
            case STOP_SERVICE_ACTION:
                Intent serviceIntent = new Intent(context, UpdateLocationService.class);
                context.stopService(serviceIntent);
                break;
            default:
                Log.w(TAG, "I don't know what to do with the action " + intent.getAction());
        }

        Log.v(TAG, "< onReceive([Context], [Intent])");
    }
}
