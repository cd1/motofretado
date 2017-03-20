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
    private final @NonNull Intent mActivityDetectionServiceIntent;
    private final @NonNull Intent mUpdateLocationServiceIntent;
    private boolean mIsActivityDetectionServiceRunning;

    @UiThread
    TrackBusPresenter(@NonNull Context context) {
        mContext = context;

        Messenger messenger = new Messenger(new MyHandler());

        mActivityDetectionServiceIntent = new Intent(mContext, ActivityDetectionService.class);
        mActivityDetectionServiceIntent.putExtra(
                ActivityDetectionService.EXTRA_MESSENGER, messenger);

        mUpdateLocationServiceIntent = new Intent(mContext, UpdateLocationService.class);
        mUpdateLocationServiceIntent.putExtra(UpdateLocationService.EXTRA_MESSENGER, messenger);
    }

    private boolean isUpdateLocationServiceRunning() {
        return (mBusId != null);
    }

    @Override
    @UiThread
    public void onAttach(@NonNull TrackBusMvp.View view) {
        mView = view;
        if (isUpdateLocationServiceRunning() || mIsActivityDetectionServiceRunning) {
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
        if (mView != null) {
            Log.d(TAG, "starting service " + mUpdateLocationServiceIntent.getComponent());
            mUpdateLocationServiceIntent.putExtra(UpdateLocationService.EXTRA_BUS_ID, mView.getBusId());
            mContext.startService(mUpdateLocationServiceIntent);
        } else {
            Log.w(TAG, "view is null; cannot read bus ID needed to start the location service");
        }
    }

    @Override
    @UiThread
    public void stopLocationUpdate() {
        if (isUpdateLocationServiceRunning()) {
            Log.d(TAG, "stopping service " + mUpdateLocationServiceIntent.getComponent());
            mContext.stopService(mUpdateLocationServiceIntent);
            mBusId = null;
        } else {
            Log.d(TAG, "service is already stopped; there's no need to stop it");
        }
    }

    @Override
    @UiThread
    public void startActivityDetection() {
        Log.d(TAG, "starting service " + mActivityDetectionServiceIntent.getComponent());

        if (mView != null) {
            mView.disableBusId();
        } else {
            Log.w(TAG, "view is null; cannot disable the bus ID field");
        }

        mContext.startService(mActivityDetectionServiceIntent);
    }

    @Override
    @UiThread
    public void stopActivityDetection() {
        Log.d(TAG, "stopping service " + mActivityDetectionServiceIntent.getComponent());
        mContext.stopService(mActivityDetectionServiceIntent);
    }

    class MyHandler extends Handler {
        static final int MSG_DISPLAY_TOAST = 0;
        static final int MSG_GMS_CONNECTION_FAILED = 1;
        static final int MSG_UPDATE_LOCATION_SERVICE_CONNECTED = 2;
        static final int MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED = 3;
        static final int MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED = 4;
        static final int MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED = 5;
        static final int MSG_USER_IS_ON_FOOT = 6;
        static final int MSG_USER_IS_IN_VEHICLE = 7;

        @Override
        @UiThread
        public void handleMessage(Message msg) {
            Log.v(TAG, "> handleMessage(msg=" + msg + ")");

            if (mView != null) {
                switch (msg.what) {
                    case MSG_DISPLAY_TOAST:
                        mView.displayMessage(mContext.getString(msg.arg1));
                        break;
                    case MSG_GMS_CONNECTION_FAILED:
                        mView.displayMessage(mContext.getString(R.string.gms_connection_failed));
                        break;
                    case MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED:
                        mBusId = null;
                        mView.enableBusId();
                        mView.uncheckSwitchDetectAutomatically();
                        break;
                    case MSG_UPDATE_LOCATION_SERVICE_CONNECTED:
                        if (msg.obj instanceof String) {
                            mBusId = (String) msg.obj;
                        } else {
                            Log.wtf(TAG, "unexpected bus ID object type: " + msg.obj.getClass());
                        }
                        mView.disableBusId();
                        break;
                    case MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED:
                        mIsActivityDetectionServiceRunning = true;
                        break;
                    case MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED:
                        mIsActivityDetectionServiceRunning = false;
                        if (!isUpdateLocationServiceRunning()) {
                            mView.enableBusId();
                        }
                        break;
                    case MSG_USER_IS_ON_FOOT:
                        if (isUpdateLocationServiceRunning()) {
                            stopLocationUpdate();
                            stopActivityDetection();
                        }
                        break;
                    case MSG_USER_IS_IN_VEHICLE:
                        if (!isUpdateLocationServiceRunning()) {
                            startLocationUpdate();
                        }
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
