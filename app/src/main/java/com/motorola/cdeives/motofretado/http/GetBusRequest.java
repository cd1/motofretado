package com.motorola.cdeives.motofretado.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.motorola.cdeives.motofretado.http.jsonapi.JSONAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetBusRequest extends JsonObjectRequest {
    private static final String TAG = GetBusRequest.class.getSimpleName();

    public <L extends Response.Listener<JSONObject> & Response.ErrorListener> GetBusRequest(@NonNull String busID, @NonNull L listener) {
        super(Method.GET, Util.SERVER_URL + "/bus/" + busID, null, listener, listener);
        Log.d(TAG, "creating request: GET /bus/" + busID);
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
