package com.gmail.cristiandeives.motofretado.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gmail.cristiandeives.motofretado.http.jsonapi.JSONAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetBusesRequest extends JsonObjectRequest {
    private static final String TAG = GetBusesRequest.class.getSimpleName();

    public <L extends Response.Listener<JSONObject> & Response.ErrorListener> GetBusesRequest(@NonNull L listener) {
        super(Method.GET, Util.SERVER_URL + "/bus", null, listener, listener);
        Log.d(TAG, "creating request: GET /bus");
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Log.v(TAG, "> getHeaders()");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", JSONAPI.CONTENT_TYPE);

        Log.v(TAG, "< getHeaders(): " + headers);
        return headers;
    }
}
