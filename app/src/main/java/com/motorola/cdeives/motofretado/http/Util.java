package com.motorola.cdeives.motofretado.http;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
    public static final String SERVER_URL = "https://pumpkin-crisp-26207.herokuapp.com";

    private static final String TAG = Util.class.getSimpleName();
    private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    private static Gson sGson;

    public static Gson getGsonInstance() {
        if (sGson == null) {
            Log.d(TAG, "initializing GSON instance");
            sGson = new GsonBuilder()
                    .setDateFormat(ISO8601_DATE_FORMAT)
                    .create();
        }

        return sGson;
    }
}
