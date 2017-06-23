package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.GetBusRequest;
import com.motorola.cdeives.motofretado.http.GetBusResponseListener;
import com.motorola.cdeives.motofretado.http.GetBusesRequest;
import com.motorola.cdeives.motofretado.http.GetBusesResponseListener;
import com.motorola.cdeives.motofretado.http.ModelListener;

import org.json.JSONObject;

class ViewMapModel implements ViewMapMvp.Model {
    private final @NonNull RequestQueue mQueue;

    ViewMapModel(@NonNull Context context) {
        mQueue = Volley.newRequestQueue(context);
    }

    @Override
    @UiThread
    public void readBus(@NonNull String busId, @NonNull ModelListener<Bus> listener) {
        Request<JSONObject> getBusRequest = new GetBusRequest(busId, new GetBusResponseListener(listener));
        mQueue.add(getBusRequest);
    }

    @Override
    @UiThread
    public void readAllBuses(@NonNull ModelListener<Bus[]> listener) {
        Request<JSONObject> request = new GetBusesRequest(new GetBusesResponseListener(listener));
        mQueue.add(request);
    }

    @Override
    public void cancelAllRequests() {
        mQueue.cancelAll(req -> true);
    }
}
