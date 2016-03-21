package com.motorola.cdeives.motofretado;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

public interface MainPresenterView {
    @UiThread
    @NonNull String getBusID();
    @UiThread
    void displayToast(@Nullable String text);
    @UiThread
    void enableBusID();
    @UiThread
    void disableBusID();
}
