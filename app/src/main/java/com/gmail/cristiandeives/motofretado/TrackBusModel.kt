package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.support.annotation.UiThread
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.BusResponseListener
import com.gmail.cristiandeives.motofretado.http.BusesResponseListener
import com.gmail.cristiandeives.motofretado.http.GetBusesRequest
import com.gmail.cristiandeives.motofretado.http.ModelListener
import com.gmail.cristiandeives.motofretado.http.PostBusRequest

@UiThread
internal class TrackBusModel(context: Context) : TrackBusMvp.Model {
    private val mQueue: RequestQueue = Volley.newRequestQueue(context)

    override fun readAllBuses(listener: ModelListener<List<Bus>>) {
        val req = GetBusesRequest(BusesResponseListener(listener))
        mQueue.add(req)
    }

    override fun createBus(bus: Bus, listener: ModelListener<Bus>) {
        val req = PostBusRequest(bus, BusResponseListener(listener))
        mQueue.add(req)
    }

    override fun cancelAllRequests() {
        mQueue.cancelAll { true }
    }
}