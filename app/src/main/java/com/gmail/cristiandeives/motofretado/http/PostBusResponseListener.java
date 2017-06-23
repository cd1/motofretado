package com.gmail.cristiandeives.motofretado.http;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class PostBusResponseListener implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = PostBusResponseListener.class.getSimpleName();

    private final ModelListener<Bus> mListener;

    public PostBusResponseListener(ModelListener<Bus> listener) {
        mListener = listener;
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.v(TAG, "> onResponse(response=" + response + ")");

        Bus bus = Bus.createFromHTTPBody(response.toString());
        mListener.onSuccess(bus);

        Log.v(TAG, "< onResponse(response=" + response + ")");
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.v(TAG, "> onErrorResponse(error=" + error + ")");

        if (error.networkResponse != null) {
            Error httpError = Error.createFromHTTPBody(new String(error.networkResponse.data));
            if (!httpError.status.isEmpty()) {
                Log.e(TAG, String.format("unexpected error POSTting bus: %s - %s [%s]",
                        httpError.status, httpError.title, httpError.details), error);
            } else {
                Log.e(TAG, "unexpected error POSTting bus [no additional detail]");
            }

            mListener.onError(error);
        }

        Log.v(TAG, "< onErrorResponse(error=" + error + ")");
    }
}
