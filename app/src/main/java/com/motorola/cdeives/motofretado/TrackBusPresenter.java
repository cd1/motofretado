package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

class TrackBusPresenter implements TrackBusMvp.Presenter {
    private static final String TAG = TrackBusPresenter.class.getSimpleName();

    private final @NonNull Context mContext;
    private @Nullable String mBusId;
    private @Nullable TrackBusMvp.View mView;

    @UiThread
    TrackBusPresenter(@NonNull Context context) {
        mContext = context;
    }

    private boolean isServiceRunning() {
        return (mBusId != null);
    }

    @Override
    @UiThread
    public void onAttach(@NonNull TrackBusMvp.View view) {
        mView = view;
        if (isServiceRunning()) {
            mView.disableBusId();
        }
    }

    @Override
    @UiThread
    public void onDetach() {
        mView = null;
    }

    @Override
    @UiThread
    public void startLocationUpdate() {
        Messenger messenger = new Messenger(new MyHandler());

        Intent serviceIntent = new Intent(mContext, UpdateLocationService.class);
        serviceIntent.putExtra(UpdateLocationService.EXTRA_BUS_ID, mView.getBusId());
        serviceIntent.putExtra(UpdateLocationService.EXTRA_MESSENGER, messenger);
        if (mView != null) {
            Log.d(TAG, "starting service " + mUpdateLocationServiceIntent.getComponent());
            mContext.startService(mUpdateLocationServiceIntent);
        } else {
            Log.w(TAG, "view is null; cannot read bus ID needed to start the location service");
        }
    }

    @Override
    @UiThread
    public void stopLocationUpdate() {
        if (isServiceRunning()) {
            Intent serviceIntent = new Intent(mContext, UpdateLocationService.class);
            mContext.stopService(serviceIntent);
            mBusId = null;
        }
    }

    class MyHandler extends Handler {
        static final int MSG_DISPLAY_TOAST = 0;
        static final int MSG_SERVICE_CONNECTED = 1;
        static final int MSG_SERVICE_DISCONNECTED = 2;

        @Override
        @UiThread
        public void handleMessage(Message msg) {
            Log.v(TAG, "> handleMessage(msg=" + msg + ")");

            if (mView != null) {
                switch (msg.what) {
                    case MSG_DISPLAY_TOAST:
                        mView.displayMessage(mContext.getString(msg.arg1));
                        break;
                    case MSG_SERVICE_DISCONNECTED:
                        mBusId = null;
                        mView.enableBusId();
                        break;
                    case MSG_SERVICE_CONNECTED:
                        if (msg.obj instanceof String) {
                            mBusId = (String) msg.obj;
                        } else {
                            Log.wtf(TAG, "unexpected bus ID object type: " + msg.obj.getClass());
                        }
                        mView.disableBusId();
                        break;
                    default:
                        Log.wtf(TAG, "unexpected message code: " + msg.what);
                }
            } else {
                Log.d(TAG, "ignoring message because mView is null");
            }

            Log.v(TAG, "< handleMessage(msg=" + msg + ")");
        }
    }
}
