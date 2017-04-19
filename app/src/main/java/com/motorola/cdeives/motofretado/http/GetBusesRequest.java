package com.motorola.cdeives.motofretado.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class GetBusesRequest extends JsonArrayRequest {
    private static final String TAG = GetBusesRequest.class.getSimpleName();

    public <L extends Response.Listener<JSONArray> & Response.ErrorListener> GetBusesRequest(@NonNull L listener) {
        super(Method.GET, Util.SERVER_URL + "/bus", null, listener, listener);
        Log.d(TAG, "creating request: GET /bus");
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        return headers;
    }
}
