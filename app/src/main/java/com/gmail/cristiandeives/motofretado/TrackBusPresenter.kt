package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.preference.PreferenceManager
import android.support.annotation.UiThread
import android.util.Log
import com.gmail.cristiandeives.motofretado.http.Bus
import com.gmail.cristiandeives.motofretado.http.ModelListener
import java.lang.ref.WeakReference

@UiThread
internal class TrackBusPresenter(private val mContext: Context) : TrackBusMvp.Presenter {
    companion object {
        private val TAG = TrackBusPresenter::class.java.simpleName
        private const val MOST_RECENT_TRACK_BUS_ID_PREF = "most_recent_track_bus_id"
    }

    private val mModel: TrackBusMvp.Model = TrackBusModel(mContext)
    private var mView: TrackBusMvp.View? = null
    private val mActivityDetectionServiceIntent: Intent
    private val mUpdateLocationServiceIntent: Intent
    private var mIsUpdateLocationServiceRunning: Boolean = false
    private var mIsActivityDetectionServiceRunning: Boolean = false
    private var mSelectedBusId: String? = null
    private var mAvailableBuses: List<Bus>? = null
    private var mBusErrorMessage: String? = null

    init {
        val messenger = Messenger(MyHandler(this))

        mActivityDetectionServiceIntent = Intent(mContext, ActivityDetectionService::class.java).apply {
            putExtra(ActivityDetectionService.EXTRA_MESSENGER, messenger)
        }

        mUpdateLocationServiceIntent = Intent(mContext, UpdateLocationService::class.java).apply {
            putExtra(UpdateLocationService.EXTRA_MESSENGER, messenger)
        }

        mModel.readAllBuses(ReadAllBusesListener(null, true))
    }

    override fun onAttach(view: TrackBusMvp.View) {
        mView = view

        val errorMessage = mBusErrorMessage
        if (errorMessage != null && errorMessage.isNotEmpty()) {
            view.setBusError(errorMessage)
        } else {
            mAvailableBuses?.let { buses ->
                view.apply {
                    setAvailableBuses(buses, mSelectedBusId)
                    enableBusId()
                }
            }
        }

        if (mIsUpdateLocationServiceRunning || mIsActivityDetectionServiceRunning) {
            view.disableBusId()
        }

        if (!mIsActivityDetectionServiceRunning) {
            view.uncheckSwitchDetectAutomatically()
        }
    }

    override fun onDetach() {
        mSelectedBusId = mView?.getBusId()
        mView = null
    }

    override fun startLocationUpdate() {
        mView?.let { view ->
            val busId = view.getBusId()

            Log.d(TAG, "starting service ${mUpdateLocationServiceIntent.component}")
            mUpdateLocationServiceIntent.putExtra(UpdateLocationService.EXTRA_BUS_ID, busId)
            mContext.startService(mUpdateLocationServiceIntent)

            Log.d(TAG, "writing preference: $MOST_RECENT_TRACK_BUS_ID_PREF => $busId")
            mContext.editSharedPreferences {
                putString(MOST_RECENT_TRACK_BUS_ID_PREF, busId)
            }
        }
    }

    override fun stopLocationUpdate() {
        if (mIsUpdateLocationServiceRunning) {
            Log.d(TAG, "stopping service ${mUpdateLocationServiceIntent.component}")
            mContext.stopService(mUpdateLocationServiceIntent)
        }
    }

    override fun startActivityDetection() {
        Log.d(TAG, "starting service ${mActivityDetectionServiceIntent.component}")

        mView?.disableBusId()
        mContext.startService(mActivityDetectionServiceIntent)
    }

    override fun stopActivityDetection() {
        Log.d(TAG, "stopping service ${mActivityDetectionServiceIntent.component}")
        mContext.stopService(mActivityDetectionServiceIntent)
    }

    override fun createBus(bus: Bus) {
        Log.d(TAG, "creating bus: $bus")
        mModel.createBus(bus, PostBusListener())
    }

    internal class MyHandler(presenter: TrackBusPresenter) : Handler() {
        companion object {
            const val MSG_DISPLAY_TOAST = 0
            const val MSG_GMS_CONNECTION_FAILED = 1
            const val MSG_UPDATE_LOCATION_SERVICE_CONNECTED = 2
            const val MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED = 3
            const val MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED = 4
            const val MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED = 5
            const val MSG_USER_IS_ON_FOOT = 6
            const val MSG_USER_IS_IN_VEHICLE = 7
        }

        private val mPresenterRef: WeakReference<TrackBusPresenter> = WeakReference(presenter)

        override fun handleMessage(msg: Message) {
            Log.v(TAG, "> handleMessage(msg=$msg)")

            mPresenterRef.get()?.let { presenter ->
                when (msg.what) {
                    MSG_DISPLAY_TOAST -> presenter.mView?.displayMessage(presenter.mContext.getString(msg.arg1))
                    MSG_GMS_CONNECTION_FAILED -> presenter.mView?.displayMessage(presenter.mContext.getString(R.string.gms_connection_failed))
                    MSG_UPDATE_LOCATION_SERVICE_DISCONNECTED -> {
                        presenter.apply {
                            mIsUpdateLocationServiceRunning = false
                            mIsActivityDetectionServiceRunning = false
                            mView?.let { view ->
                                view.enableBusId()
                                view.uncheckSwitchDetectAutomatically()
                            }
                            mModel.cancelAllRequests()
                        }
                    }
                    MSG_UPDATE_LOCATION_SERVICE_CONNECTED -> {
                        presenter.apply {
                            mIsUpdateLocationServiceRunning = true
                            mView?.disableBusId()
                        }
                    }
                    MSG_ACTIVITY_DETECTION_SERVICE_CONNECTED -> presenter.mIsActivityDetectionServiceRunning = true
                    MSG_ACTIVITY_DETECTION_SERVICE_DISCONNECTED -> presenter.apply {
                        mIsActivityDetectionServiceRunning = false
                        if (!mIsUpdateLocationServiceRunning) {
                            mView?.enableBusId()
                        }
                    }
                    MSG_USER_IS_ON_FOOT -> presenter.apply {
                        if (mIsUpdateLocationServiceRunning) {
                            stopLocationUpdate()
                            stopActivityDetection()
                        }
                    }
                    MSG_USER_IS_IN_VEHICLE -> presenter.apply {
                        if (!mIsUpdateLocationServiceRunning) {
                            startLocationUpdate()
                        }
                    }
                    else -> Log.wtf(TAG, "unexpected message code: ${msg.what}")
                }
            }

            Log.v(TAG, "< handleMessage(msg=$msg)")
        }
    }

    private inner class ReadAllBusesListener(private val mSelectedBusId: String?, private val mSelectDefaultFromPref: Boolean) : ModelListener<List<Bus>> {
        override fun onSuccess(data: List<Bus>) {
            mView?.let { view ->
                if (!data.isEmpty()) {
                    val selectedBusId = if (mSelectDefaultFromPref) {
                        val busIdPref = PreferenceManager.getDefaultSharedPreferences(mContext)
                                .getString(MOST_RECENT_TRACK_BUS_ID_PREF, null)
                        Log.d(TAG, "preference read: $MOST_RECENT_TRACK_BUS_ID_PREF => $busIdPref")
                        busIdPref
                    } else if (!mSelectedBusId.isNullOrEmpty()) {
                        mSelectedBusId
                    } else {
                        null
                    }

                    view.enableBusId()
                    view.setAvailableBuses(data, selectedBusId)
                    mBusErrorMessage = null
                    mAvailableBuses = data
                } else {
                    val errorMessage = mContext.getString(R.string.no_buses_available)
                    view.setBusError(errorMessage)
                    mBusErrorMessage = errorMessage
                }
            }
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "could not read buses", ex)
            val errorMessage = mContext.getString(R.string.read_buses_failed)
            mView?.setBusError(errorMessage)
            mBusErrorMessage = errorMessage
        }
    }

    private inner class PostBusListener : ModelListener<Bus> {
        override fun onSuccess(data: Bus) {
            Log.d(TAG, "bus created successfully: $data")
            mModel.readAllBuses(ReadAllBusesListener(data.id, false))
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "could not create bus", ex)
            mView?.displayMessage(mContext.getString(R.string.create_bus_failed))
        }
    }
}