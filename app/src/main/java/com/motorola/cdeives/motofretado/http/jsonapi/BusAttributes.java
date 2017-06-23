package com.motorola.cdeives.motofretado.http.jsonapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.motorola.cdeives.motofretado.http.Bus;

import java.util.Date;

class BusAttributes {
    double latitude;
    double longitude;
    @SerializedName("created_at")
    @Nullable Date createdAt;
    @SerializedName("updated_at")
    @Nullable Date updatedAt;

    static @NonNull BusAttributes parse(@NonNull Bus bus) {
        BusAttributes attributes = new BusAttributes();

        attributes.createdAt = bus.createdAt;
        attributes.updatedAt = bus.updatedAt;
        attributes.latitude = bus.latitude;
        attributes.longitude = bus.longitude;

        return attributes;
    }
}
