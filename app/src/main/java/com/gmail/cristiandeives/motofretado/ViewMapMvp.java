package com.gmail.cristiandeives.motofretado;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;

import com.gmail.cristiandeives.motofretado.http.Bus;
import com.gmail.cristiandeives.motofretado.http.ModelListener;

import java.util.List;

interface ViewMapMvp {
    interface Model {
        @UiThread
        void readAllBuses(@NonNull ModelListener<Bus[]> listener);
        @UiThread
        void readBus(@NonNull String busId, @NonNull ModelListener<Bus> listener);
        @UiThread
        void cancelAllRequests();
    }

    interface View {
        @UiThread
        String getBusId();
        @UiThread
        void setMapMarker(String title, double latitude, double longitude);
        @UiThread
        void enableBusIdInput();
        @UiThread
        void disableBusIdInput();
        @UiThread
        void displayMessage(@StringRes int messageId);
        @UiThread
        void setAvailableBuses(@NonNull List<Bus> buses, @Nullable String defaultBusId);
    }

    interface Presenter {
        @UiThread
        void onAttach(View view);
        @UiThread
        void onDetach();
        @UiThread
        void startViewingBusLocation();
        @UiThread
        void stopViewingBusLocation();
    }
}
