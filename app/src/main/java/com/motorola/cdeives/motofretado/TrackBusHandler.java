package com.motorola.cdeives.motofretado;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import java.lang.ref.WeakReference;

public class TrackBusHandler extends Handler {
    public static final int MSG_DISPLAY_TOAST = 0;
    public static final int MSG_ENABLE_BUS_ID = 1;
    public static final int MSG_DISABLE_BUS_ID = 2;

    private static final String TAG = TrackBusHandler.class.getSimpleName();

    private @NonNull WeakReference<TrackBusPresenterImpl> mTrackBusPresenterRef;

    public TrackBusHandler(@NonNull TrackBusPresenterImpl presenter) {
        mTrackBusPresenterRef = new WeakReference<>(presenter);
    }

    @Override
    @UiThread
    public void handleMessage(Message msg) {
        Log.v(TAG, "> handleMessage(" + msg + ")");

        TrackBusPresenterImpl presenter = mTrackBusPresenterRef.get();
        if (presenter != null) {
            switch (msg.what) {
                case MSG_DISPLAY_TOAST:
                    presenter.mView.displayToast(presenter.mContext.getResources().getString(msg.arg1));
                    break;
                case MSG_ENABLE_BUS_ID:
                    presenter.mView.enableBusID();
                    break;
                case MSG_DISABLE_BUS_ID:
                    presenter.mView.disableBusID();
                    break;
                default:
                    Log.wtf(TAG, "unexpected message code: " + msg.what);
            }
        } else {
            Log.w(TAG, "TrackBusPresenter ref is null; can't handle message");
        }

        Log.v(TAG, "< handleMessage(" + msg + ")");
    }
}
