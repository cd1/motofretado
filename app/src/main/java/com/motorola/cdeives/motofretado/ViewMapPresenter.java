package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.motorola.cdeives.motofretado.http.Bus;

class ViewMapPresenter implements ViewMapMvp.Presenter {
    private static final String TAG = ViewMapPresenter.class.getSimpleName();
    private static final int MSG_VIEW_BUS_LOCATION = 0;
    private static final int REPEAT_DELAY = 2500; // ms

    private Handler mHandler;
    private final @NonNull ViewMapMvp.Model mModel;
    private @Nullable ViewMapMvp.View mView;
    private boolean mIsViewingBusLocation;

    ViewMapPresenter(Context context) {
        mModel = new ViewMapModel(context.getApplicationContext());
    }

    @Override
    @UiThread
    public void onAttach(@NonNull ViewMapMvp.View view) {
        mView = view;
        if (mIsViewingBusLocation) {
            mView.disableBusIdInput();
        }
    }

    @Override
    @UiThread
    public void onDetach() {
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
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "> onHandleMessage(msg=" + msg + ")");

            if (mView != null) {
                String busId = mView.getBusId();
                switch (msg.what) {
                    case MSG_VIEW_BUS_LOCATION:
                        mModel.readBus(busId, new ViewMapMvp.Model.Listener<Bus>() {
                            @Override
                            public void onSuccess(Bus data) {
                                mView.setMapMarker(busId, data.latitude, data.longitude);
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
}
