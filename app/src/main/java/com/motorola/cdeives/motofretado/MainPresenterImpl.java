package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

public class MainPresenterImpl implements MainPresenter {
    private static final String TAG = MainPresenterImpl.class.getSimpleName();

    /* package */ @NonNull Context mContext;
    /* package */ @NonNull MainPresenterView mView;

    public MainPresenterImpl(@NonNull Context context, @NonNull MainPresenterView view) {
        mContext = context;
        mView = view;
    }

    @Override
    @UiThread
    public void setUp() {
    }

    @Override
    @UiThread
    public void tearDown() {
    }

    @Override
    @UiThread
    public void startLocationUpdate() {
        Messenger messenger = new Messenger(new MainPresenterHandler(this));
        String busId = mView.getBusID();

        Intent serviceIntent = new Intent(mContext, UpdateLocationService.class);
        serviceIntent.putExtra(UpdateLocationService.EXTRA_BUS_ID, busId);
        serviceIntent.putExtra(UpdateLocationService.EXTRA_MESSENGER, messenger);
        Log.d(TAG, "starting service " + serviceIntent.getComponent());
        mContext.startService(serviceIntent);
        // END

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
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MainPresenterImpl.this);
                } else {
                    mView.displayToast("Location permission wasn't granted");
                }
            }
        }); */
    }

    @Override
    @UiThread
    public void stopLocationUpdate() {
        Intent serviceIntent = new Intent(mContext, UpdateLocationService.class);
        mContext.stopService(serviceIntent);
    }
}
