package com.gmail.cristiandeives.motofretado.http.jsonapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.gmail.cristiandeives.motofretado.http.Bus;

class BusData {
    @Nullable String type;
    @Nullable String id;
    @Nullable BusAttributes attributes;

    static @NonNull BusData parse(@NonNull Bus bus) {
        BusData data = new BusData();

        data.id = bus.id;
        data.type = BusDocument.TYPE;
        data.attributes = BusAttributes.parse(bus);

        return data;
    }

    @NonNull Bus toBus() throws IllegalStateException {
        if (type == null) {
            throw new IllegalStateException("missing JSONAPI type");
        }

        if (!type.equals(BusDocument.TYPE)) {
            throw new IllegalStateException("invalid JSONAPI type: " + type);
        }

        if (TextUtils.isEmpty(id)) {
            throw new IllegalStateException("missing JSONAPI ID");
        }

        Bus bus = new Bus();

        bus.id = id;
        if (attributes != null) {
            bus.createdAt = attributes.createdAt;
            bus.updatedAt = attributes.updatedAt;
            bus.latitude = attributes.latitude;
            bus.longitude = attributes.longitude;
        }

        return bus;
    }
}
