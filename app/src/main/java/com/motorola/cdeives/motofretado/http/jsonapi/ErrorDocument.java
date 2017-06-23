package com.motorola.cdeives.motofretado.http.jsonapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.motorola.cdeives.motofretado.http.Error;

public class ErrorDocument {
    @Nullable ErrorData[] errors;

    public @NonNull Error toError() throws IllegalStateException {
        if (errors == null || errors.length == 0) {
            throw new IllegalStateException("no error found in document");
        }

        Error error = new Error();

        error.status = errors[0].status;
        error.title = errors[0].title;
        error.details = errors[0].detail;

        return error;
    }
}
