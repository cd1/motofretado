package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;

import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.ModelListener;

import java.util.Arrays;
import java.util.List;

class TrackBusPresenter implements TrackBusMvp.Presenter {
    private static final String TAG = TrackBusPresenter.class.getSimpleName();
    private static final String MOST_RECENT_TRACK_BUS_ID_PREF = "most_recent_track_bus_id";

    private final @NonNull Context mContext;
    private final @NonNull TrackBusMvp.Model mModel;
    private @Nullable TrackBusMvp.View mView;
    private final @NonNull Intent mActivityDetectionServiceIntent;
    private final @NonNull Intent mUpdateLocationServiceIntent;
    private boolean mIsUpdateLocationServiceRunning;
    private boolean mIsActivityDetectionServiceRunning;
    private @Nullable String mSelectedBusId;
    private @Nullable List<Bus> mAvailableBuses;

    @UiThread
    TrackBusPresenter(@NonNull Context context) {
        mContext = context;

        mModel = new TrackBusModel(mContext);
        Messenger messenger = new Messenger(new MyHandler());

        mActivityDetectionServiceIntent = new Intent(mContext, ActivityDetectionService.class);
        mActivityDetectionServiceIntent.putExtra(
                ActivityDetectionService.EXTRA_MESSENGER, messenger);

        mUpdateLocationServiceIntent = new Intent(mContext, UpdateLocationService.class);
        mUpdateLocationServiceIntent.putExtra(UpdateLocationService.EXTRA_MESSENGER, messenger);

        mModel.readAllBuses(new ReadAllBusesListener(null,true));
    }

    @Override
    @UiThread
    public void onAttach(@NonNull TrackBusMvp.View view) {
        mView = view;

        if (mAvailableBuses != null) {
            mView.setAvailableBuses(mAvailableBuses, mSelectedBusId);
            mView.enableBusId();
        }

        if (mIsUpdateLocationServiceRunning || mIsActivityDetectionServiceRunning) {
            mView.disableBusId();
        }

        if (!mIsActivityDetectionServiceRunning) {
            mView.uncheckSwitchDetectAutomatically();
        }
    }

    @Override
    @UiThread
    public void onDetach() {
        if (mView != null) {
            mSelectedBusId = mView.getBusId();
        } else {
            Log.w(TAG, "view is null; cannot save current bus ID");
        }

        mView = null;
    }

    @Override
    @UiThread
    public void startLocationUpdate() {
        if (mView != null) {
            String busId = mView.getBusId();

            Log.d(TAG, "starting service " + mUpdateLocationServiceIntent.getComponent());
            mUpdateLocationServiceIntent.putExtra(UpdateLocationService.EXTRA_BUS_ID, busId);
            mContext.startService(mUpdateLocationServiceIntent);

            Log.d(TAG, "writing preference: " + MOST_RECENT_TRACK_BUS_ID_PREF + " => " + busId);
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
            prefsEditor.putString(MOST_RECENT_TRACK_BUS_ID_PREF, busId);
            prefsEditor.apply();
        } else {
            Log.w(TAG, "view is null; cannot read bus ID needed to start the location service");
        }
    }

    @Override
    @UiThread
    public void stopLocationUpdate() {
        if (mIsUpdateLocationServiceRunning) {
            Log.d(TAG, "stopping service " + mUpdateLocationServiceIntent.getComponent());
            mContext.stopService(mUpdateLocationServiceIntent);
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

    @Override
    @UiThread
    public void createBus(@NonNull Bus bus) {
        Log.d(TAG, "creating bus: " + bus);

        mModel.createBus(bus, new PostBusListener());
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

            switch (msg.what) {
                case MSG_DISPLAY_TOAST:
                    if (mView != null) {
                        mView.displayMessage(mContext.getString(msg.arg1));
                    } else {
                        Log.w(TAG, "view is null; cannot display message");
                    }
                    break;
                case MSG_GMS_CONNECTION_FAILED:
                    if (mView != null) {
                        mView.displayMessage(mContext.getString(R.string.gms_connection_failed));
                    } else {
                        Log.d(TAG, "view is null; cannot display message");
                    }
                    break;
                case MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED:
                    mIsUpdateLocationServiceRunning = false;
                    mIsActivityDetectionServiceRunning = false;
                    if (mView != null) {
                        mView.enableBusId();
                        mView.uncheckSwitchDetectAutomatically();
                    } else {
                        Log.d(TAG, "view is null; cannot update UI");
                    }
                    mModel.cancelAllRequests();
                    break;
                case MSG_UPDATE_LOCATION_SERVICE_CONNECTED:
                    mIsUpdateLocationServiceRunning = true;
                    if (mView != null) {
                        mView.disableBusId();
                    } else {
                        Log.d(TAG, "view is null; cannot update UI");
                    }
                    break;
                case MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED:
                    mIsActivityDetectionServiceRunning = true;
                    break;
                case MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED:
                    mIsActivityDetectionServiceRunning = false;
                    if (mView != null) {
                        if (!mIsUpdateLocationServiceRunning) {
                            mView.enableBusId();
                        }
                    } else {
                        Log.d(TAG, "view is null; cannot update UI");
                    }
                    break;
                case MSG_USER_IS_ON_FOOT:
                    if (mIsUpdateLocationServiceRunning) {
                        stopLocationUpdate();
                        stopActivityDetection();
                    }
                    break;
                case MSG_USER_IS_IN_VEHICLE:
                    if (!mIsUpdateLocationServiceRunning) {
                        startLocationUpdate();
                    }
                    break;
                default:
                    Log.wtf(TAG, "unexpected message code: " + msg.what);
            }

            Log.v(TAG, "< handleMessage(msg=" + msg + ")");
        }
    }

    @UiThread
    private class ReadAllBusesListener implements ModelListener<Bus[]> {
        private final @Nullable String mSelectedBusId;
        private final boolean mSelectDefaultFromPref;

        private ReadAllBusesListener(@Nullable String selectedBusId, boolean selectedBusFromPref) {
            mSelectedBusId = selectedBusId;
            mSelectDefaultFromPref = selectedBusFromPref;
        }

        @Override
        public void onSuccess(Bus[] buses) {
            List<Bus> busesList = Arrays.asList(buses);

            if (mView != null) {
                String selectedBusId;

                if (mSelectDefaultFromPref) {
                    selectedBusId = PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString(MOST_RECENT_TRACK_BUS_ID_PREF, null);
                    Log.d(TAG, "preference read: "
                            + MOST_RECENT_TRACK_BUS_ID_PREF + " => " + selectedBusId);
                } else if (!TextUtils.isEmpty(mSelectedBusId)) {
                    selectedBusId = mSelectedBusId;
                } else {
                    selectedBusId = null;
                }

                mView.setAvailableBuses(busesList, selectedBusId);
                mView.enableBusId();
            } else {
                Log.w(TAG, "view is null; cannot update the available bus numbers");
            }

            mAvailableBuses = busesList;
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, "could not read buses", ex);
            if (mView != null) {
                mView.displayMessage(mContext.getString(R.string.read_buses_failed));
            } else {
                Log.w(TAG, "view is null; cannot display error message");
            }
        }
    }

    @UiThread
    private class PostBusListener implements ModelListener<Bus> {
        @Override
        public void onSuccess(Bus bus) {
            Log.d(TAG, "bus created successfully: " + bus);
            mModel.readAllBuses(new ReadAllBusesListener(bus.id, false));
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, "could not create bus", ex);
            if (mView != null) {
                mView.displayMessage(mContext.getString(R.string.create_bus_failed));
            } else {
                Log.w(TAG, "view is null; cannot display error message");
            }
        }
    }
}
