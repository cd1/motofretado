package com.motorola.cdeives.motofretado.http.jsonapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.motorola.cdeives.motofretado.http.Bus;

public class BusesDocument {
    @Nullable JSONAPI jsonapi;
    @Nullable BusData[] data;
    @Nullable Links links;

    public @NonNull Bus[] toBuses() throws IllegalStateException {
        if (jsonapi != null && jsonapi.version != null && !jsonapi.version.equals(JSONAPI.CURRENT_VERSION)) {
            throw new IllegalStateException("unsupported JSONAPI version: " + jsonapi.version);
        }

        if (data == null) {
            throw new IllegalStateException("missing JSONAPI data");
        }

        Bus[] buses = new Bus[data.length];

        for (int i = 0; i < buses.length; i++) {
            buses[i] = data[i].toBus();
        }

        return buses;
    }
}
