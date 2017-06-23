package com.gmail.cristiandeives.motofretado;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.gmail.cristiandeives.motofretado.http.Bus;
import com.gmail.cristiandeives.motofretado.http.ModelListener;

import java.util.List;

interface TrackBusMvp {
    interface Model {
        @UiThread
        void readAllBuses(@NonNull ModelListener<Bus[]> listener);
        @UiThread
        void createBus(@NonNull Bus bus, ModelListener<Bus> listener);
        @UiThread
        void cancelAllRequests();
    }

    interface View {
        @UiThread
        @Nullable String getBusId();
        @UiThread
        void displayMessage(@Nullable String text);
        @UiThread
        void enableBusId();
        @UiThread
        void disableBusId();
        @UiThread
        void uncheckSwitchDetectAutomatically();
        @UiThread
        void setAvailableBuses(@NonNull List<Bus> buses, @Nullable String selectedBusId);
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
        void startActivityDetection();
        @UiThread
        void stopActivityDetection();
        @UiThread
        void createBus(@NonNull Bus bus);
    }
}
