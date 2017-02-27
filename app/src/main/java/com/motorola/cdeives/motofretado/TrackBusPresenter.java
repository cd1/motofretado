package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

class TrackBusPresenter implements TrackBusMvp.Presenter {
    private static final String TAG = TrackBusPresenter.class.getSimpleName();

    private final @NonNull Context mContext;
    private String mBusId;
    private TrackBusMvp.View mView;

    TrackBusPresenter(@NonNull Context context) {
        mContext = context;
    }

    private boolean isServiceRunning() {
        return (mBusId != null);
    }

    @Override
    @UiThread
    public void onAttach(TrackBusMvp.View view) {
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
        Log.d(TAG, "starting service " + serviceIntent.getComponent());
        mContext.startService(serviceIntent);

                /* LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case GeofenceStatusCodes.SUCCESS:
                        Log.d(TAG, "we're allowed to start location updates!");
                        break;
                    case GeofenceStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult((Activity) mContext, 0);
                        } catch (IntentSender.SendIntentException ex) {
                            Log.e(TAG, "error trying to start resolution", ex);
                        }
                        break;
                    default:
                        mView.displayToast("Can't we use your GPS??? :(");
                        Log.w(TAG, status.toString());
                }

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mView.disableBusID();
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, TrackBusPresenterImpl.this);
                } else {
                    mView.displayToast("Location permission wasn't granted");
                }
            }
        }); */
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
