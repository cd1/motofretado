package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.GetBusRequest;
import com.motorola.cdeives.motofretado.http.Util;

import org.json.JSONObject;

class ViewMapModel implements ViewMapMvp.Model {
    private final @NonNull RequestQueue mQueue;

    ViewMapModel(@NonNull Context context) {
        mQueue = Volley.newRequestQueue(context);
    }

    @Override
    @UiThread
    public void readBus(String busId, Listener<Bus> listener) {
        JsonObjectRequest getBusRequest = new GetBusRequest(busId, new GetBusResponseListener(listener));
        mQueue.add(getBusRequest);
    }

    @Override
    public void cancelAllRequests() {
        mQueue.cancelAll(req -> true);
    }

    private class GetBusResponseListener implements Response.Listener<JSONObject>, Response.ErrorListener {
        private final String TAG = ViewMapModel.class.getSimpleName();

        private final Listener<Bus> mListener;

        private GetBusResponseListener(Listener<Bus> listener) {
            mListener = listener;
        }

        @Override
        public void onResponse(JSONObject response) {
            Log.v(TAG, "> onResponse([JSONObject])");

            mListener.onSuccess(Util.getGsonInstance().fromJson(response.toString(), Bus.class));

            Log.v(TAG, "< onResponse([JSONObject])");
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v(TAG, "> onErrorResponse([VolleyError])");

            mListener.onError(error);

            Log.v(TAG, "< onErrorResponse([VolleyError])");
        }
    }
}
