package com.motorola.cdeives.motofretado.http;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class GetBusResponseListener implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = GetBusResponseListener.class.getSimpleName();

    private final ModelListener<Bus> mListener;

    public GetBusResponseListener(ModelListener<Bus> listener) {
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

        mListener.onError(error);

        Log.v(TAG, "< onErrorResponse(error=" + error + ")");
    }
}