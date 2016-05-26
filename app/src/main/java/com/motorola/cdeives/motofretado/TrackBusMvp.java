package com.motorola.cdeives.motofretado;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

public interface TrackBusMvp {
    interface Model {

    }

    interface View {
        @UiThread
        @NonNull
        String getBusId();
        @UiThread
        void displayMessage(@Nullable String text);
        @UiThread
        void enableBusId();
        @UiThread
        void disableBusId();
    }

    interface Presenter {
        @UiThread
        void onAttach(View view);
        @UiThread
        void onDetach();
        @UiThread
        void startLocationUpdate();
        @UiThread
        void stopLocationUpdate();
        @UiThread
        String getBusId();
    }
}
