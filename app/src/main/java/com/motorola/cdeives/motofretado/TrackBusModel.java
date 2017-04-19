package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.motorola.cdeives.motofretado.http.Bus;
import com.motorola.cdeives.motofretado.http.GetBusesRequest;
import com.motorola.cdeives.motofretado.http.GetBusesResponseListener;
import com.motorola.cdeives.motofretado.http.ModelListener;
import com.motorola.cdeives.motofretado.http.PostBusRequest;
import com.motorola.cdeives.motofretado.http.PostBusResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

@UiThread
class TrackBusModel implements TrackBusMvp.Model {
    private final @NonNull RequestQueue mQueue;

    TrackBusModel(@NonNull Context context) {
        mQueue = Volley.newRequestQueue(context);
    }

    @Override
    public void readAllBuses(@NonNull ModelListener<Bus[]> listener) {
        Request<JSONArray> req = new GetBusesRequest(new GetBusesResponseListener(listener));
        mQueue.add(req);
    }

    @Override
    public void createBus(@NonNull Bus bus, ModelListener<Bus> listener) {
        Request<JSONObject> req = new PostBusRequest(bus, new PostBusResponseListener(listener));
        mQueue.add(req);
    }

    @Override
    public void cancelAllRequests() {
        mQueue.cancelAll(req -> true);
    }
}

