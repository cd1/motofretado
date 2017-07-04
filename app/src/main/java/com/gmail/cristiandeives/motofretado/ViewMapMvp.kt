package com.gmail.cristiandeives.motofretado

import android.support.annotation.StringRes
import android.support.annotation.UiThread
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.ModelListener

@UiThread
internal interface ViewMapMvp {
    interface Model {
        fun readAllBuses(listener: ModelListener<List<Bus>>)
        fun readBus(busId: String, listener: ModelListener<Bus>)
        fun cancelAllRequests()
    }

    interface View {
        fun getBusId(): String?
        fun setMapMarker(title: String, latitude: Double, longitude: Double)
        fun enableBusIdInput()
        fun disableBusIdInput()
        fun displayMessage(@StringRes messageId: Int)
        fun setAvailableBuses(buses: List<Bus>, defaultBusId: String?)
        fun setBusError(errorMessage: String)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun startViewingBusLocation()
        fun stopViewingBusLocation()
    }
}