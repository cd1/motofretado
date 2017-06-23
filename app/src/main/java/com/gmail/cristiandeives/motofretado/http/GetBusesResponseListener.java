package com.gmail.cristiandeives.motofretado.http;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class GetBusesResponseListener
        implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = GetBusesResponseListener.class.getSimpleName();

    private final ModelListener<Bus[]> mListener;

    public GetBusesResponseListener(ModelListener<Bus[]> listener) {
        mListener = listener;
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.v(TAG, "> onResponse(response=" + response + ")");

        Bus[] buses = Bus.createBusesFromHTTPBody(response.toString());
        mListener.onSuccess(buses);

        Log.v(TAG, "< onResponse(response=" + response + ")");
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.v(TAG, "> onErrorResponse(error=" + error + ")");

        if (error.networkResponse != null) {
            Error httpError = Error.createFromHTTPBody(new String(error.networkResponse.data));
            if (!httpError.status.isEmpty()) {
                Log.e(TAG, String.format("unexpected error GETting bus: %s - %s [%s]",
                        httpError.status, httpError.title, httpError.details), error);
                mListener.onError(error);
            } else {
                Log.e(TAG, "unexpected error GETting bus [no additional detail]");
            }
        }

        Log.v(TAG, "< onErrorResponse(error=" + error + ")");
    }
}
