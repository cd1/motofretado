package com.motorola.cdeives.motofretado.http.jsonapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.motorola.cdeives.motofretado.http.Bus;

public class BusDocument {
    static final String TYPE = "bus";

    @Nullable JSONAPI jsonapi;
    @Nullable BusData data;
    @Nullable Links links;

    public static @NonNull BusDocument parse(@NonNull Bus bus) {
        BusDocument doc = new BusDocument();

        if (doc.jsonapi != null) {
            doc.jsonapi.version = JSONAPI.CURRENT_VERSION;
        }
        doc.data = BusData.parse(bus);

        return doc;
    }

    public @NonNull Bus toBus() throws IllegalStateException {
        if (jsonapi != null && jsonapi.version != null && !jsonapi.version.equals(JSONAPI.CURRENT_VERSION)) {
            throw new IllegalStateException("unsupported JSONAPI version: " + jsonapi.version);
        }

        if (data == null) {
            throw new IllegalStateException("missing JSONAPI data");
        }

        return data.toBus();
    }
}
