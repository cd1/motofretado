package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;

import com.motorola.cdeives.motofretado.http.Bus;

class ViewMapPresenter implements ViewMapMvp.Presenter {
    private static final String TAG = ViewMapPresenter.class.getSimpleName();
    private static final int MSG_VIEW_BUS_LOCATION = 0;
    private static final int REPEAT_DELAY = 2500; // ms

    private Handler mHandler;
    private String mBusId;
    private ViewMapMvp.Model mModel;
    private ViewMapMvp.View mView;

    ViewMapPresenter(Context context) {
        mModel = new ViewMapModel(context.getApplicationContext());
    }

    private boolean isViewingBusLocation() {
        return !TextUtils.isEmpty(mBusId);
    }

    @Override
    @UiThread
    public void onAttach(ViewMapMvp.View view) {
        mView = view;
    }

    @Override
    @UiThread
    public void onDetach() {
        mView = null;
    }

    @Override
    @UiThread
    public void onStop() {
        mModel.cancelAllRequests();

        if (mHandler != null) {
            mHandler.removeMessages(MSG_VIEW_BUS_LOCATION);
        }

        mBusId = null;
        mView.enableBusId();
    }

    @Override
    public void startViewingBusLocation(@NonNull String busId) {
        if (isViewingBusLocation()) {
            Log.d(TAG, "the user is already viewing the location of bus " + mBusId);
            return;
        }

        if (mHandler == null) {
            mHandler = new MyHandler();
        }

        mBusId = busId;
        mView.disableBusId();
        mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION).sendToTarget();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "> onHandleMessage(msg=" + msg + ")");

            switch (msg.what) {
                case MSG_VIEW_BUS_LOCATION:
                    mModel.readBus(mBusId, new ViewMapMvp.Model.Listener<Bus>() {
                        @Override
                        public void onSuccess(Bus data) {
                            mView.setMapMarker(mBusId, data.latitude, data.longitude);
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.e(TAG, "could not read bus " + mBusId, ex);
                        }
                    });

                    Message newMsg = mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION);
                    mHandler.sendMessageDelayed(newMsg, REPEAT_DELAY);
                    break;
                default:
                    Log.wtf(TAG, "unexpected message code: " + msg.what);
            }

            Log.v(TAG, "< onHandleMessage(msg=" + msg + ")");
        }
    }
}
