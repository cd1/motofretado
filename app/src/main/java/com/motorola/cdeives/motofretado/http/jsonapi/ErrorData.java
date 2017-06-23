package com.motorola.cdeives.motofretado.http.jsonapi;

import android.support.annotation.Nullable;

class ErrorData {
    @Nullable String status;
    @Nullable ErrorSource source;
    @Nullable String title;
    @Nullable String detail;
}
