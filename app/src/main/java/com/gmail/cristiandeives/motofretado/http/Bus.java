package com.gmail.cristiandeives.motofretado.http;

import android.support.annotation.NonNull;

import com.gmail.cristiandeives.motofretado.http.jsonapi.BusDocument;
import com.gmail.cristiandeives.motofretado.http.jsonapi.BusesDocument;

import java.util.Date;

public class Bus {
    public String id;
    public double latitude;
    public double longitude;
    public Date updatedAt;
    public Date createdAt;

    public static @NonNull Bus createFromHTTPBody(@NonNull String body) {
        BusDocument doc = Util.getGsonInstance().fromJson(body, BusDocument.class);
        return doc.toBus();
    }

    public static @NonNull Bus[] createBusesFromHTTPBody(@NonNull String body) {
        BusesDocument doc = Util.getGsonInstance().fromJson(body, BusesDocument.class);
        return doc.toBuses();
    }

    @Override
    public String toString() {
        return id;
    }

    public @NonNull String toHTTPBody() {
        BusDocument doc = BusDocument.parse(this);
        return Util.getGsonInstance().toJson(doc, BusDocument.class);
    }
}
