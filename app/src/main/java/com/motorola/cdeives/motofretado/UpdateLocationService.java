package com.motorola.cdeives.motofretado;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.Error;
import com.motorola.cdeives.motofretado.http.PatchBusRequest;
import com.motorola.cdeives.motofretado.http.PostBusRequest;
import com.motorola.cdeives.motofretado.http.Util;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Calendar;

public class UpdateLocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String EXTRA_MESSENGER = "messenger";
    public static final String EXTRA_BUS_ID = "bus_id";

    private static final String TAG = UpdateLocationService.class.getSimpleName();
    private static final int UPDATE_INTERVAL = 5000; // ms
    private static final int FASTEST_UPDATE_INTERVAL = 1000; // ms
    private static final int NOTIFICATION_ID = 1;
    private static final int MAIN_ACTIVITY_INTENT_CODE = 1;

    private Messenger mMessenger;
    private String mBusId;
    private GoogleApiClient mGoogleApiClient;
    private RequestQueue mRequestQueue;

    @Override
    @MainThread
    public void onCreate() {
        Log.v(TAG, "> onCreate()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mRequestQueue = Volley.newRequestQueue(this);

        Log.v(TAG, "< onCreate()");
    }

    @Override
    @MainThread
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "> onStartCommand(" + intent + ", " + flags + ", " + startId + ")");

        Bundle extras = intent.getExtras();
        mMessenger = (Messenger) extras.get(EXTRA_MESSENGER);
        Log.d(TAG, "extra \"" + EXTRA_MESSENGER + "\": " + mMessenger);
        mBusId = extras.getString(EXTRA_BUS_ID);
        Log.d(TAG, "extra \"" + EXTRA_BUS_ID + "\": " + mBusId);

        Log.d(TAG, "opening connection to Google Play Services");
        mGoogleApiClient.connect();

        Log.v(TAG, "< onStartCommand(" + intent + ", " + flags + ", " + startId + ")");

        return START_NOT_STICKY;
    }

    @Override
    @MainThread
    public IBinder onBind(Intent intent) {
        // we're not a bound service, so return null here
        Log.v(TAG, "onBind(" + intent + ")");

        return null;
    }

    @Override
    @MainThread
    public void onDestroy() {
        Log.v(TAG, "> onDestroy()");

        Log.d(TAG, "unsubscribing from location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "closing connection to Google Play Services");
        mGoogleApiClient.disconnect();

        Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_ENABLE_BUS_ID);
        try {
            mMessenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message to enable bus ID", ex);
        }

        Log.v(TAG, "< onDestroy()");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "> onConnected(" + bundle + ")");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.d(TAG, "subscribing for location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    locationRequest, this);

            Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISABLE_BUS_ID);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to disable bus ID", ex);
            }

            Intent activityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingContent = PendingIntent.getActivity(this, MAIN_ACTIVITY_INTENT_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notify_bus)
                    .setContentTitle(getString(R.string.notification_title, mBusId))
                    .setContentIntent(pendingContent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.notification_stop_title), null)
                    .build();
            Log.d(TAG, "marking the service as foreground / adding persistent notification");
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST, R.string.fine_location_permission_not_granted, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to display toast", ex);
            }
        }

        Log.v(TAG, "< onConnected(" + bundle + ")");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "> onConnectionSuspended(" + i + ")");

        Log.w(TAG, "Google Play Services connection was suspended!");
        Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_ENABLE_BUS_ID);
        try {
            mMessenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message to enable bus ID", ex);
        }

        Log.v(TAG, "< onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "> onConnectionFailed(" + connectionResult + ")");

        Log.w(TAG, "Google Play Services connection failed!");
        Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_ENABLE_BUS_ID);
        try {
            mMessenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message to enable bus ID", ex);
        }

        Log.v(TAG, "< onConnectionFailed(" + connectionResult + ")");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "> onLocationChanged(" + location + ")");

        Bus bus = new Bus();
        bus.id = mBusId;
        bus.latitude = location.getLatitude();
        bus.longitude = location.getLongitude();
        bus.updatedAt = Calendar.getInstance().getTime();

        JsonObjectRequest request = new PatchBusRequest(bus, new PatchBusResponseListener(bus));
        Log.d(TAG, "PATCH /bus/" + mBusId);
        mRequestQueue.add(request);

        Log.v(TAG, "< onLocationChanged(" + location + ")");
    }

    private class PatchBusResponseListener
            implements Response.Listener<JSONObject>, Response.ErrorListener {
        private final String TAG = getClass().getSimpleName();

        private @NonNull Bus mBus;

        public PatchBusResponseListener(@NonNull Bus bus) {
            mBus = bus;
        }

        @Override
        public void onResponse(JSONObject response) {
            Log.v(TAG, "> onResponse([JSONObject]");

            Log.d(TAG, "bus location updated successfully");

            Log.v(TAG, "< onResponse([JSONObject]");
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v(TAG, "> onErrorResponse(" + error + ")");

            switch (error.networkResponse.statusCode) {
                case HttpURLConnection.HTTP_NOT_FOUND: // 404 Not Found
                    JsonObjectRequest request = new PostBusRequest(mBus, new PostBusResponseListener());
                    Log.d(TAG, "POST /bus");
                    mRequestQueue.add(request);
                    break;
                default:
                    Error httpError = Util.getGsonInstance().fromJson(new String(error.networkResponse.data), Error.class);
                    Log.e(TAG, "unexpected error PATCHing bus: " + httpError.details + "(" + httpError.status + ")", error);

                    Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST, R.string.update_network_error, 0);
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException ex) {
                        Log.e(TAG, "error sending message to display toast", ex);
                    }
            }

            Log.v(TAG, "< onErrorResponse(" + error + ")");
        }
    }

    private class PostBusResponseListener
            implements Response.Listener<JSONObject>, Response.ErrorListener {
        @Override
        public void onResponse(JSONObject response) {
            Log.v(TAG, "> onResponse([JSONObject]");

            Log.d(TAG, "bus created successfully");

            Log.v(TAG, "< onResponse([JSONObject]");
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v(TAG, "> onErrorResponse(" + error + ")");

            switch (error.networkResponse.statusCode) {
                case HttpURLConnection.HTTP_CONFLICT: // 409 Conflict
                    // when a PATCH request is sent to a non-existing bus, a POST request is sent
                    // in order to create it. however, another PATCH request may be sent before the
                    // POST doesn't finish, so another POST will be sent (for that second PATCH
                    // request which failed). then that second POST request will return 409 Conflict.
                    Log.w(TAG, "I received 409 Conflict when POSTing a new bus; it's probably safe to ignore it");
                    break;
                default:
                    Error httpError = Util.getGsonInstance().fromJson(new String(error.networkResponse.data), Error.class);
                    Log.e(TAG, "unexpected error POSTing bus: " + httpError.details + "(" + httpError.status + ")", error);

                    Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST, R.string.update_network_error, 0);
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException ex) {
                        Log.e(TAG, "error sending message to display toast", ex);
                    }
            }

            Log.v(TAG, "< onErrorResponse(" + error + ")");
        }
    }
}
