package com.gmail.cristiandeives.motofretado.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gmail.cristiandeives.motofretado.http.jsonapi.JSONAPI;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostBusRequest extends JsonObjectRequest {
    private static final String TAG = PostBusRequest.class.getSimpleName();

    private final Bus mBus;

    public <L extends Response.Listener<JSONObject> & Response.ErrorListener> PostBusRequest(@NonNull Bus bus, @NonNull L listener) {
        super(Method.POST, Util.SERVER_URL + "/bus", null, listener, listener);
        Log.d(TAG, "creating request: POST /bus/");
        mBus = bus;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Log.v(TAG, "> getHeaders()");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", JSONAPI.CONTENT_TYPE);
        headers.put("Content-Type", JSONAPI.CONTENT_TYPE);

        Log.v(TAG, "< getHeaders(): " + headers);
        return headers;
    }

    @Override
    public byte[] getBody() {
        Log.v(TAG, "> getBody()");

        byte[] body = mBus.toHTTPBody().getBytes();

        Log.v(TAG, "< getBody(): " + Arrays.toString(body));
        return body;
    }
}
