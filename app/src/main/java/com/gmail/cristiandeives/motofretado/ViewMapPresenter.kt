package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.annotation.UiThread
import android.util.Log
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.ModelListener
import java.lang.ref.WeakReference
import java.util.Calendar

@UiThread
internal class ViewMapPresenter(private val mContext: Context) : ViewMapMvp.Presenter {
    companion object {
        private val TAG = ViewMapPresenter::class.java.simpleName
        private const val MSG_VIEW_BUS_LOCATION = 0
        private const val REPEAT_DELAY = 5_000L // ms
        private const val RECENT_LOCATION_THRESHOLD = 10 // min
        private const val MOST_RECENT_VIEW_BUS_ID_PREF = "most_recent_view_bus_id"
    }

    private val mHandler: Handler = MyHandler(this)
    private val mModel: ViewMapMvp.Model = ViewMapModel(mContext.applicationContext)
    private var mView: ViewMapMvp.View? = null
    private var mIsViewingBusLocation: Boolean = false
    private var mSelectedBusId: String? = null
    private var mAvailableBuses: List<Bus>? = null
    private var mBusErrorMessage: String? = null

    init {
        mModel.readAllBuses(ReadAllBusesListener())
    }

    override fun onAttach(view: ViewMapMvp.View) {
        mView = view

        val errorMessage = mBusErrorMessage
        if (errorMessage != null && errorMessage.isNotEmpty()) {
            view.setBusError(errorMessage)
        } else {
            mAvailableBuses?.let { buses ->
                view.apply {
                    setAvailableBuses(buses, mSelectedBusId)
                    enableBusIdInput()
                }
            }
        }

        if (mIsViewingBusLocation) {
            view.disableBusIdInput()
        }
    }

    override fun onDetach() {
        mSelectedBusId = mView?.getBusId()
        mView = null
    }

    override fun stopViewingBusLocation() {
        if (!mIsViewingBusLocation) {
            Log.d(TAG, "the user is not viewing the bus location")
            return
        }

        mModel.cancelAllRequests()
        mHandler.removeMessages(MSG_VIEW_BUS_LOCATION)
        mView?.enableBusIdInput()
        mIsViewingBusLocation = false
    }

    override fun startViewingBusLocation() {
        if (mIsViewingBusLocation) {
            Log.d(TAG, "the user is already viewing the bus location")
            return
        }

        mView?.disableBusIdInput()

        mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION).sendToTarget()
        mIsViewingBusLocation = true

        mView?.getBusId()?.let { busId ->
            Log.d(TAG, "writing preference: $MOST_RECENT_VIEW_BUS_ID_PREF => $busId")
            mContext.editSharedPreferences {
                putString(MOST_RECENT_VIEW_BUS_ID_PREF, busId)
            }
        }
    }

    private class MyHandler(presenter: ViewMapPresenter) : Handler() {
        private val mPresenterRef: WeakReference<ViewMapPresenter> = WeakReference(presenter)

        override fun handleMessage(msg: Message) {
            Log.v(TAG, "> onHandleMessage(msg=$msg)")

            mPresenterRef.get()?.let { presenter ->
                when (msg.what) {
                    MSG_VIEW_BUS_LOCATION -> {
                        presenter.mView?.let { view ->
                            view.getBusId()?.let { busId ->
                                presenter.mModel.readBus(busId, object : ModelListener<Bus> {
                                    override fun onSuccess(data: Bus) {
                                        val oldestAcceptableTime = Calendar.getInstance().apply {
                                            add(Calendar.MINUTE, -RECENT_LOCATION_THRESHOLD)
                                        }

                                        if (data.updatedAt?.after(oldestAcceptableTime.time) ?: false) {
                                            if (data.latitude != null && data.longitude != null) {
                                                view.setMapMarker(busId, data.latitude, data.longitude, data.updatedAt)
                                            }
                                        } else {
                                            view.displayMessage(R.string.view_bus_not_recent_message)
                                            presenter.stopViewingBusLocation()
                                        }
                                    }

                                    override fun onError(ex: Exception) {
                                        Log.e(TAG, "could not read bus $busId", ex)
                                        view.displayMessage(R.string.read_bus_failed)
                                        presenter.stopViewingBusLocation()
                                    }
                                })
                            }
                        }

                        val newMsg = presenter.mHandler.obtainMessage(MSG_VIEW_BUS_LOCATION)
                        presenter.mHandler.sendMessageDelayed(newMsg, REPEAT_DELAY)
                    }
                    else -> Log.wtf(TAG, "unexpected message code: ${msg.what}")
                }
            }

            Log.v(TAG, "< onHandleMessage(msg=$msg)")
        }
    }

    private inner class ReadAllBusesListener : ModelListener<List<Bus>> {
        override fun onSuccess(data: List<Bus>) {
            mView?.let { view ->
                if (!data.isEmpty()) {
                    val mostRecentBusId = PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString(MOST_RECENT_VIEW_BUS_ID_PREF, null)
                    Log.d(TAG, "preference read: $MOST_RECENT_VIEW_BUS_ID_PREF => $mostRecentBusId")
                    view.enableBusIdInput()
                    view.setAvailableBuses(data, mostRecentBusId)
                    mBusErrorMessage = null
                    mAvailableBuses = data
                } else {
                    mContext.getString(R.string.no_buses_available).let { msg ->
                        view.setBusError(msg)
                        mBusErrorMessage = msg
                    }
                }
            }
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "could not read buses", ex)
            mView?.let { view ->
                mContext.getString(R.string.read_buses_failed).let { msg ->
                    view.setBusError(msg)
                    mBusErrorMessage = msg
                }
            }
        }
    }
}