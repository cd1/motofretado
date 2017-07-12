package com.gmail.cristiandeives.motofretado

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.support.annotation.MainThread
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.BusResponseListener
import com.gmail.cristiandeives.motofretado.http.PatchBusRequest
import com.gmail.cristiandeives.motofretado.http.EmptyModelListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

@MainThread
internal class UpdateLocationService : Service() {
    companion object {
        internal const val EXTRA_MESSENGER = "messenger"
        internal const val EXTRA_BUS_ID = "bus_id"

        private val TAG = UpdateLocationService::class.java.simpleName
        private const val UPDATE_INTERVAL = 5000L // ms
        private const val FASTEST_UPDATE_INTERVAL = 1000L // ms
        private const val NOTIFICATION_ID = 1
        private const val MAIN_ACTIVITY_INTENT_CODE = 1
        private const val STOP_ACTION_INTENT_CODE = 2
    }

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var mBusId: String
    private lateinit var mMessenger: Messenger
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate() {
        Log.v(TAG, "> onCreate()")

        mLocationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationCallback = UpdateLocationCallback()

        mRequestQueue = Volley.newRequestQueue(this)
        mReceiver = UpdateLocationBroadcastReceiver()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Log.v(TAG, "< onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "> onStartCommand(intent=$intent, flags=$flags, startId=$startId)")

        val extras = intent.extras
        mMessenger = extras.get(EXTRA_MESSENGER) as Messenger
        Log.d(TAG, "extra \"$EXTRA_MESSENGER\": $mMessenger")
        mBusId = extras.getString(EXTRA_BUS_ID)
        Log.d(TAG, "extra \"$EXTRA_BUS_ID\": $mBusId")

        Log.d(TAG, "requesting location updates")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())

            val msg = Message.obtain(null,
                    TrackBusPresenter.MyHandler.MSG_UPDATE_LOCATION_SERVICE_CONNECTED, mBusId)
            try {
                mMessenger.send(msg)
            } catch (e: RemoteException) {
                Log.e(TAG, "error sending message to connect service", e)
            }

            val activityIntent = Intent(this, MainActivity::class.java)
            val pendingContent = PendingIntent.getActivity(
                    this,
                    MAIN_ACTIVITY_INTENT_CODE,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val intentStop = Intent(UpdateLocationBroadcastReceiver.STOP_SERVICE_ACTION)
            val pendingStop = PendingIntent.getBroadcast(
                    this,
                    STOP_ACTION_INTENT_CODE,
                    intentStop,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notify_bus)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(mBusId)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentIntent(pendingContent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.notification_stop_title), pendingStop)
                    .build()
            Log.d(TAG, "marking the service as foreground / adding persistent notification")
            startForeground(NOTIFICATION_ID, notification)

            Log.d(TAG, "registering BroadcastReceiver")
            registerReceiver(mReceiver, IntentFilter(UpdateLocationBroadcastReceiver.STOP_SERVICE_ACTION))
        } else {
            val msg = Message.obtain(null, TrackBusPresenter.MyHandler.MSG_DISPLAY_TOAST,
                    R.string.fine_location_permission_not_granted, 0)
            try {
                mMessenger.send(msg)
            } catch (e: RemoteException) {
                Log.e(TAG, "error sending message to display toast", e)
            }
        }

        val ret = Service.START_NOT_STICKY
        Log.v(TAG, "< onStartCommand(intent=$intent, flags=$flags, startId=$startId): $ret")

        return ret
    }

    override fun onBind(intent: Intent): IBinder? {
        // we're not a bound service, so return null here
        Log.v(TAG, "onBind(intent=$intent)")
        return null
    }

    override fun onDestroy() {
        Log.v(TAG, "> onDestroy()")

        Log.d(TAG, "unsubscribing from location updates")
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)

        Log.d(TAG, "unregistering BroadcastReceiver")
        unregisterReceiver(mReceiver)

        mMessenger.sendMessage(TrackBusPresenter.MyHandler.MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED)

        Log.v(TAG, "< onDestroy()")
    }

    private inner class UpdateLocationCallback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Log.v(TAG, "> onLocationResult(result=$result)")

            val lastLocation = result.lastLocation
            val bus = Bus(
                    id = mBusId,
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude
            )

            val request = PatchBusRequest(bus, BusResponseListener(EmptyModelListener))
            mRequestQueue.add(request)

            Log.v(TAG, "< onLocationResult(result=$result)")
        }
    }
}