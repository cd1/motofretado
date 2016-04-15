package com.motorola.cdeives.motofretado.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PatchBusRequest extends JsonObjectRequest {
    private static final String TAG = PatchBusRequest.class.getSimpleName();

    private Bus mBus;

    public <L extends Response.Listener<JSONObject> & Response.ErrorListener> PatchBusRequest(@NonNull Bus bus, @NonNull L listener) {
        super(Method.PATCH, Util.SERVER_URL + "/bus/" + bus.id, null, listener, listener);
        Log.d(TAG, "creating request: PATCH /bus/" + bus.id);
        mBus = bus;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        return headers;
    }

    @Override
    public byte[] getBody() {
        Bus bus = mBus;

        if (!TextUtils.isEmpty(mBus.id)) {
            // PATCH /bus/<id> doesn't accept IDs, so let's create a new bus without it
            bus = new Bus();
            bus.latitude = mBus.latitude;
            bus.longitude = mBus.longitude;
        }

        String jsonString = Util.getGsonInstance().toJson(bus, Bus.class);

        return jsonString.getBytes();
    }
}
