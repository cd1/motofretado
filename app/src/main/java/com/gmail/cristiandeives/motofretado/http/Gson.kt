package com.gmail.cristiandeives.motofretado.http

import android.util.Log
import com.google.gson.GsonBuilder

internal object Gson {
    private const val ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val TAG = Gson::class.java.simpleName

    internal val gson: com.google.gson.Gson by lazy {
        Log.d(TAG, "initializing Gson")
        GsonBuilder()
            .setDateFormat(ISO8601_DATE_FORMAT)
            .create()
    }
}