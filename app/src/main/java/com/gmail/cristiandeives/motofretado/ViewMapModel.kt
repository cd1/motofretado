package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.support.annotation.UiThread
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.BusResponseListener
import com.gmail.cristiandeives.motofretado.http.BusesResponseListener
import com.gmail.cristiandeives.motofretado.http.GetBusRequest
import com.gmail.cristiandeives.motofretado.http.GetBusesRequest
import com.gmail.cristiandeives.motofretado.http.ModelListener

@UiThread
internal class ViewMapModel(context: Context) : ViewMapMvp.Model {
    private val mQueue: RequestQueue = Volley.newRequestQueue(context)

    override fun readBus(busId: String, listener: ModelListener<Bus>) {
        val req = GetBusRequest(busId, BusResponseListener(listener))
        mQueue.add(req)
    }

    override fun readAllBuses(listener: ModelListener<List<Bus>>) {
        val req = GetBusesRequest(BusesResponseListener(listener))
        mQueue.add(req)
    }

    override fun cancelAllRequests() {
        mQueue.cancelAll { true }
    }
}