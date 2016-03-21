package com.motorola.cdeives.motofretado.http;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostBusRequest extends JsonObjectRequest {
    private Bus mBus;

    public <L extends Response.Listener<JSONObject> & Response.ErrorListener> PostBusRequest(@NonNull Bus bus, @NonNull L listener) {
        super(Method.POST, Util.SERVER_URL + "/bus", null, listener, listener);
        mBus = bus;
    }

    @Override
    public Map<String, String> getHeaders ()throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        return headers;
    }

    @Override
    public byte[] getBody() {
        String jsonString = Util.getGsonInstance().toJson(mBus, Bus.class);

        return jsonString.getBytes();
    }
}