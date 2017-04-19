package com.motorola.cdeives.motofretado.http;


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
        Log.v(TAG, "> onResponse([JSONObject])");

        Bus bus = Util.getGsonInstance().fromJson(response.toString(), Bus.class);
        mListener.onSuccess(bus);

        Log.v(TAG, "< onResponse([JSONObject])");
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.v(TAG, "> onErrorResponse(error=" + error + ")");

        if (error.networkResponse != null) {
            Error httpError = Util.getGsonInstance().fromJson(
                    new String(error.networkResponse.data), Error.class);
            Log.e(TAG, String.format("unexpected error POSTing bus: %s (%d)",
                    httpError.details, httpError.status), error);

            mListener.onError(error);
        }

        Log.v(TAG, "< onErrorResponse(error=" + error + ")");
    }
}
