package com.gmail.cristiandeives.motofretado.http;

import android.support.annotation.NonNull;

import com.gmail.cristiandeives.motofretado.http.jsonapi.ErrorDocument;

public class Error {
    public String status;
    public String title;
    public String details;

    public static @NonNull Error createFromHTTPBody(@NonNull String body) {
        ErrorDocument doc = Util.getGsonInstance().fromJson(body, ErrorDocument.class);
        return doc.toError();
    }
}
