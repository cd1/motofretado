package com.motorola.cdeives.motofretado;

import android.support.annotation.UiThread;

public interface MainPresenter {
    @UiThread
    void setUp();
    @UiThread
    void tearDown();
    @UiThread
    void startLocationUpdate();
    @UiThread
    void stopLocationUpdate();
    @UiThread
    String getBusId();
    @UiThread
    boolean isUpdateLocationServiceRunning();
}
