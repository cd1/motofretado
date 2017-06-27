package com.gmail.cristiandeives.motofretado;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.MainThread;
import android.support.annotation.UiThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.gmail.cristiandeives.motofretado.http.Bus;
import com.gmail.cristiandeives.motofretado.http.ModelListener;
import com.gmail.cristiandeives.motofretado.http.PatchBusRequest;
import com.gmail.cristiandeives.motofretado.http.PatchBusResponseListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class UpdateLocationService extends Service {
    public static final String EXTRA_MESSENGER = "messenger";
    public static final String EXTRA_BUS_ID = "bus_id";

    private static final String TAG = UpdateLocationService.class.getSimpleName();
    private static final int UPDATE_INTERVAL = 5000; // ms
    private static final int FASTEST_UPDATE_INTERVAL = 1000; // ms
    private static final int NOTIFICATION_ID = 1;
    private static final int MAIN_ACTIVITY_INTENT_CODE = 1;
    private static final int STOP_ACTION_INTENT_CODE = 2;

    private Messenger mMessenger;
    private String mBusId;
    private RequestQueue mRequestQueue;
    private BroadcastReceiver mReceiver;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Override
    @MainThread
    public void onCreate() {
        Log.v(TAG, "> onCreate()");

        int permissionStatus = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationRequest = LocationRequest.create()
                    .setInterval(UPDATE_INTERVAL)
                    .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationCallback = new UpdateLocationCallback();

            mRequestQueue = Volley.newRequestQueue(this);
            mReceiver = new UpdateLocationBroadcastReceiver();
        } else {
            Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST,
                    R.string.fine_location_permission_not_granted, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to display toast", ex);
            }
        }

        Log.v(TAG, "< onCreate()");
    }

    @Override
    @MainThread
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "> onStartCommand(intent=" + intent + ", flags=" + flags + ", startId=" + startId + ")");

        Bundle extras = intent.getExtras();
        mMessenger = (Messenger) extras.get(EXTRA_MESSENGER);
        Log.d(TAG, "extra \"" + EXTRA_MESSENGER + "\": " + mMessenger);
        mBusId = extras.getString(EXTRA_BUS_ID);
        Log.d(TAG, "extra \"" + EXTRA_BUS_ID + "\": " + mBusId);

        Log.d(TAG, "requesting location updates");
        int permissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());

            Message msg = Message.obtain(null,
                    TrackBusPresenter.MyHandler.MSG_UPDATE_LOCATION_SERVICE_CONNECTED, mBusId);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to connect service", ex);
            }

            Intent activityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingContent = PendingIntent.getActivity(this, MAIN_ACTIVITY_INTENT_CODE,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intentStop = new Intent(UpdateLocationBroadcastReceiver.STOP_SERVICE_ACTION);
            PendingIntent pendingStop = PendingIntent.getBroadcast(this, STOP_ACTION_INTENT_CODE,
                    intentStop, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notify_bus)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(mBusId)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentIntent(pendingContent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.notification_stop_title), pendingStop)
                    .build();
            Log.d(TAG, "marking the service as foreground / adding persistent notification");
            startForeground(NOTIFICATION_ID, notification);

            Log.d(TAG, "registering BroadcastReceiver");
            registerReceiver(mReceiver, new IntentFilter(UpdateLocationBroadcastReceiver.STOP_SERVICE_ACTION));
        } else {
            Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST,
                    R.string.fine_location_permission_not_granted, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to display toast", ex);
            }
        }

        Log.v(TAG, "< onStartCommand(intent=" + intent + ", flags=" + flags + ", startId=" + startId + ")");

        return START_NOT_STICKY;
    }

    @Override
    @MainThread
    public IBinder onBind(Intent intent) {
        // we're not a bound service, so return null here
        Log.v(TAG, "onBind(intent=" + intent + ")");

        return null;
    }

    @Override
    @MainThread
    public void onDestroy() {
        Log.v(TAG, "> onDestroy()");

        Log.d(TAG, "unsubscribing from location updates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        Log.d(TAG, "unregistering BroadcastReceiver");
        unregisterReceiver(mReceiver);

        Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED);
        try {
            mMessenger.send(msg);
        } catch (RemoteException ex) {
            Log.e(TAG, "error sending message to disconnect service", ex);
        }

        Log.v(TAG, "< onDestroy()");
    }

    @UiThread
    private class UpdateLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult result) {
            Log.v(TAG, "> onLocationResult(result=" + result + ")");

            Location lastLocation = result.getLastLocation();

            Bus bus = new Bus();
            bus.id = mBusId;
            bus.latitude = lastLocation.getLatitude();
            bus.longitude = lastLocation.getLongitude();

            Request<JSONObject> request = new PatchBusRequest(bus,
                    new PatchBusResponseListener(new PatchBusListener()));
            mRequestQueue.add(request);

            Log.v(TAG, "< onLocationResult(result=" + result + ")");
        }
    }

    private class PatchBusListener implements ModelListener<Bus> {
        private final String TAG = getClass().getSimpleName();

        @Override
        public void onSuccess(Bus bus) {
            Log.d(TAG, "bus location updated successfully");
        }

        @Override
        public void onError(Exception error) {
            Message msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST, R.string.update_network_error, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException ex) {
                Log.e(TAG, "error sending message to display toast", ex);
            }
        }
    }
}
