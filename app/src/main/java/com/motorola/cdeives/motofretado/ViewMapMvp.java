package com.motorola.cdeives.motofretado;

import android.support.annotation.UiThread;

import com.motorola.cdeives.motofretado.http.Bus;

interface ViewMapMvp {
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
        String getBusId();
        @UiThread
        void setMapMarker(String title, double latitude, double longitude);
        @UiThread
        void enableBusIdInput();
        @UiThread
        void disableBusIdInput();
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
