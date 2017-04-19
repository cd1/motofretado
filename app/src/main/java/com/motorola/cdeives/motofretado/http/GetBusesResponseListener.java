package com.motorola.cdeives.motofretado.http;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.util.Arrays;

public class GetBusesResponseListener
        implements Response.Listener<JSONArray>, Response.ErrorListener {
    private static final String TAG = GetBusesResponseListener.class.getSimpleName();

    private final ModelListener<Bus[]> mListener;

    public GetBusesResponseListener(ModelListener<Bus[]> listener) {
        mListener = listener;
    }

    @Override
    public void onResponse(JSONArray response) {
        Log.v(TAG, "> onResponse([JSONObject])");

        Bus[] buses = Util.getGsonInstance().fromJson(response.toString(), Bus[].class);
        Arrays.sort(buses);
        mListener.onSuccess(buses);

        Log.v(TAG, "< onResponse([JSONObject])");
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.v(TAG, "> onErrorResponse([VolleyError])");

        if (error.networkResponse != null) {
            Error httpError = Util.getGsonInstance().fromJson(
                    new String(error.networkResponse.data), Error.class);
            Log.e(TAG, String.format("unexpected error GETting bus: %s (%d)",
                    httpError.details, httpError.status), error);

            mListener.onError(error);
        }

        Log.v(TAG, "< onErrorResponse([VolleyError])");
    }
}
