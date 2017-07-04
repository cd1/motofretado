package com.gmail.cristiandeives.motofretado

import android.support.annotation.UiThread
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.ModelListener

@UiThread
internal interface TrackBusMvp {
    interface Model {
        fun readAllBuses(listener: ModelListener<List<Bus>>)
        fun createBus(bus: Bus, listener: ModelListener<Bus>)
        fun cancelAllRequests()
    }

    interface View {
        fun getBusId(): String?
        fun displayMessage(text: String)
        fun enableBusId()
        fun disableBusId()
        fun uncheckSwitchDetectAutomatically()
        fun setAvailableBuses(buses: List<Bus>, selectedBusId: String?)
        fun setBusError(errorMessage: String)
   }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun startLocationUpdate()
        fun stopLocationUpdate()
        fun startActivityDetection()
        fun stopActivityDetection()
        fun createBus(bus: Bus)
    }
}