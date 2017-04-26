package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.ModelListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class ViewMapPresenter implements ViewMapMvp.Presenter {
    private static final String TAG = ViewMapPresenter.class.getSimpleName();
    private static final int MSG_VIEW_BUS_LOCATION = 0;
    private static final int REPEAT_DELAY = 2500; // ms
    private static final int RECENT_LOCATION_THRESHOLD = 10; // min
    private static final String MOST_RECENT_VIEW_BUS_ID_PREF = "most_recent_view_bus_id";

    private final @NonNull Context mContext;
    private Handler mHandler;
    private final @NonNull ViewMapMvp.Model mModel;
    private @Nullable ViewMapMvp.View mView;
    private boolean mIsViewingBusLocation;
    private @Nullable String mSelectedBusId;
    private @Nullable List<Bus> mAvailableBuses;

    ViewMapPresenter(@NonNull Context context) {
        mContext = context;
        mModel = new ViewMapModel(context.getApplicationContext());
        mModel.readAllBuses(new ReadAllBusesListener());
    }

    @Override
    @UiThread
    public void onAttach(@NonNull ViewMapMvp.View view) {
        mView = view;

        if (mAvailableBuses != null) {
            mView.setAvailableBuses(mAvailableBuses, mSelectedBusId);
            if (!mAvailableBuses.isEmpty()) {
                mView.enableBusIdInput();
            }
        }

        if (mIsViewingBusLocation) {
            mView.disableBusIdInput();
        }
    }

    @Override
    @UiThread
    public void onDetach() {
        if (mView != null) {
            mSelectedBusId = mView.getBusId();
        }

        mView = null;
    }

    @Override
    @UiThread
    public void stopViewingBusLocation() {
        if (!mIsViewingBusLocation) {
            Log.d(TAG, "the user is not viewing the bus location");
            return;
        }

        mModel.cancelAllRequests();

        if (mHandler != null) {
            mHandler.removeMessages(MSG_VIEW_BUS_LOCATION);
        }

        if (mView != null) {
            mView.enableBusIdInput();
        } else {
            Log.w(TAG, "view is null; cannot enable bus ID input");
        }

        mIsViewingBusLocation = false;
    }

    @Override
    @UiThread
    public void startViewingBusLocation() {
        if (mIsViewingBusLocation) {
            Log.d(TAG, "the user is already viewing the bus location");
            return;
        }

        if (mHandler == null) {
            mHandler = new MyHandler();
        }

        if (mView != null) {
            mView.disableBusIdInput();
        } else {
            Log.w(TAG, "view is null; cannot disable bus ID input");
        }

        mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION).sendToTarget();
        mIsViewingBusLocation = true;

        String busId = mView.getBusId();
        Log.d(TAG, "writing preference: " + MOST_RECENT_VIEW_BUS_ID_PREF + " => " + busId);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(MOST_RECENT_VIEW_BUS_ID_PREF, busId);
        prefsEditor.apply();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "> onHandleMessage(msg=" + msg + ")");

            if (mView != null) {
                String busId = mView.getBusId();
                switch (msg.what) {
                    case MSG_VIEW_BUS_LOCATION:
                        mModel.readBus(busId, new ModelListener<Bus>() {
                            @Override
                            public void onSuccess(Bus data) {
                                Calendar oldestAcceptableTime = Calendar.getInstance();
                                oldestAcceptableTime.add(Calendar.MINUTE,
                                        -RECENT_LOCATION_THRESHOLD);

                                if (data.updatedAt.after(oldestAcceptableTime.getTime())) {
                                    mView.setMapMarker(busId, data.latitude, data.longitude);
                                } else {
                                    mView.displayMessage(R.string.view_bus_not_recent_message);
                                    stopViewingBusLocation();
                                }
                            }

                            @Override
                            public void onError(Exception ex) {
                                Log.e(TAG, "could not read bus " + busId, ex);
                            }
                        });

                        Message newMsg = mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION);
                        mHandler.sendMessageDelayed(newMsg, REPEAT_DELAY);
                        break;
                    default:
                        Log.wtf(TAG, "unexpected message code: " + msg.what);
                }
            } else {
                Log.w(TAG, "view is null; cannot get selected bus ID");
            }

            Log.v(TAG, "< onHandleMessage(msg=" + msg + ")");
        }
    }

    @UiThread
    private class ReadAllBusesListener implements ModelListener<Bus[]> {
        @Override
        public void onSuccess(Bus[] buses) {
            List<Bus> busesList = Arrays.asList(buses);

            if (mView != null) {
                String mostRecentBusId;

                if (!busesList.isEmpty()) {
                    mostRecentBusId = PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString(MOST_RECENT_VIEW_BUS_ID_PREF, null);
                    Log.d(TAG, "preference read: "
                            + MOST_RECENT_VIEW_BUS_ID_PREF + " => " + mostRecentBusId);
                    mView.enableBusIdInput();
                } else {
                    mostRecentBusId = null;
                }

                mView.setAvailableBuses(busesList, mostRecentBusId);
            } else {
                Log.w(TAG, "view is null; cannot update the available bus numbers");
            }

            mAvailableBuses = busesList;
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, "could not read buses", ex);
            if (mView != null) {
                mView.displayMessage(R.string.read_buses_failed);
            } else {
                Log.w(TAG, "view is null; cannot display error message");
            }
        }
    }
}
