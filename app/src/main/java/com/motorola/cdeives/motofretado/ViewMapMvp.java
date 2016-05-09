package com.motorola.cdeives.motofretado;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.motorola.cdeives.motofretado.http.Bus;

public interface ViewMapMvp {
     interface Model {
        interface Listener<T> {
            void onSuccess(T data);
            void onError(Exception ex);
        }

        void readBus(String busId, Listener<Bus> listener);
        void cancelAllRequests();
    }

    interface View {
        @UiThread
        void setMapMarker(String title, double latitude, double longitude);
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
        void onStart();
        @UiThread
        void onStop();
        @UiThread
        void startViewingBusLocation(@NonNull String busID);
    }
}
