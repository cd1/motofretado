package com.gmail.cristiandeives.motofretado.http

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.gmail.cristiandeives.motofretado.http.jsonapi.CONTENT_TYPE
import org.json.JSONObject

internal sealed class BaseRequest<in L>(method: Int, url: String, listener: L) : JsonObjectRequest(
        method,
        "$SERVER_URL$url",
        null,
        listener,
        listener
) where L : Response.Listener<JSONObject>,
        L : Response.ErrorListener {
    companion object {
        private const val SERVER_URL = "https://pumpkin-crisp-26207.herokuapp.com"
    }

    protected val TAG: String = javaClass.simpleName

    init {
        Log.d(TAG, "creating request: $method $url")
    }

    override fun getHeaders(): Map<String, String> {
        Log.v(TAG, "> getHeaders()")

        val headers = mapOf(
                "Accept" to CONTENT_TYPE,
                "Content-Type" to CONTENT_TYPE
        )

        Log.v(TAG, "< getHeaders(): $headers")
        return headers
    }
}

internal class GetBusesRequest<in L>(listener: L) : BaseRequest<L>(Request.Method.GET, "/bus", listener)
where L : Response.Listener<JSONObject>,
      L : Response.ErrorListener

internal class GetBusRequest<in L>(busID: String, listener: L) : BaseRequest<L>(Request.Method.GET, "/bus/$busID", listener)
where L : Response.Listener<JSONObject>,
      L : Response.ErrorListener

internal class PatchBusRequest<in L>(private val mBus: Bus, listener: L) : BaseRequest<L>(Request.Method.PATCH, "/bus/${mBus.id}", listener)
where L : Response.Listener<JSONObject>,
      L : Response.ErrorListener {
    override fun getBody(): ByteArray {
        Log.v(TAG, "> getBody()")

        val body = mBus.toHTTPBody()

        Log.v(TAG, "< getBody(): $body")
        return body.toByteArray()
    }
}

internal class PostBusRequest<in L>(private val mBus: Bus, listener: L) : BaseRequest<L>(Request.Method.POST, "/bus", listener)
where L : Response.Listener<JSONObject>,
      L : Response.ErrorListener {
    override fun getBody(): ByteArray {
        Log.v(TAG, "> getBody()")

        val body = mBus.toHTTPBody()

        Log.v(TAG, "< getBody(): $body")
        return body.toByteArray()
    }
}