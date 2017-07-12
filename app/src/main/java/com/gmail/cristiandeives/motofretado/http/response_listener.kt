package com.gmail.cristiandeives.motofretado.http

import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import org.json.JSONObject

internal sealed class BaseResponseListener<in T>(val mListener: ModelListener<T>) : Response.Listener<JSONObject>, Response.ErrorListener {
    protected val TAG: String = javaClass.simpleName

    override fun onErrorResponse(error: VolleyError) {
        Log.v(TAG, "> onErrorResponse(error=$error)")

        Log.e(TAG, "unexpected error", error)
        error.networkResponse?.let { resp ->
            val httpError = Error.createFromHTTPBody(String(resp.data))
            Log.e(TAG, "JSONAPI details: $httpError")
        }

        mListener.onError(error)

        Log.v(TAG, "< onErrorResponse(error=$error)")
    }
}

internal class BusResponseListener(listener: ModelListener<Bus>) : BaseResponseListener<Bus>(listener) {
    override fun onResponse(response: JSONObject) {
        Log.v(TAG, "> onResponse(response=$response)")

        val bus = Bus.createFromHTTPBody(response.toString())
        mListener.onSuccess(bus)

        Log.v(TAG, "< onResponse(response=$response)")
    }
}

internal class BusesResponseListener(listener: ModelListener<List<Bus>>) : BaseResponseListener<List<Bus>>(listener) {
    override fun onResponse(response: JSONObject) {
        Log.v(TAG, "> onResponse(response=$response)")

        val buses = Bus.createBusesFromHTTPBody(response.toString())
        mListener.onSuccess(buses)

        Log.v(TAG, "< onResponse(response=$response)")
    }
}