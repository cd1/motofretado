package com.gmail.cristiandeives.motofretado

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.MainThread
import android.support.annotation.UiThread
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.gmail.cristiandeives.motofretado.http.Bus
import kotlinx.android.synthetic.main.fragment_track_bus.*
import java.util.Arrays

@MainThread
internal class TrackBusFragment : Fragment(), LoaderManager.LoaderCallbacks<TrackBusMvp.Presenter>, TrackBusMvp.View, AddBusDialogFragment.OnClickListener {
    companion object {
        private val TAG = TrackBusFragment::class.java.simpleName
        private const val REQUEST_PERMISSION_LOCATION_UPDATE = 0
        private const val REQUEST_PERMISSION_ACTIVITY_DETECTION = 1
        private const val TRACK_BUS_LOADER_ID = 0
    }

    private lateinit var mSpinnerAdapter: BusSpinnerAdapter
    private var mPresenter: TrackBusMvp.Presenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val rootView = inflater.inflate(R.layout.fragment_track_bus, container, false)

        mSpinnerAdapter = BusSpinnerAdapter(context)
        mPresenter = null;

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $rootView")
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        spinnerBusID.adapter = mSpinnerAdapter

        buttonAddBus.setOnClickListener {
            val fragment = AddBusDialogFragment()
            fragment.setTargetFragment(this@TrackBusFragment, 0)
            fragment.show(fragmentManager, AddBusDialogFragment::class.java.name)
        }
        buttonEnterBus.setOnClickListener(this::buttonEnterBusClick)
        buttonLeaveBus.setOnClickListener { mPresenter?.stopLocationUpdate() }
        switchDetectAutomatically.setOnCheckedChangeListener(this::switchDetectAutomaticallyChange)

        disableBusId()

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onDestroyView() {
        Log.v(TAG, "> onDestroyView()")
        super.onDestroyView()

        mPresenter?.onDetach()

        Log.v(TAG, "< onDestroyView()")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        loaderManager.initLoader(TRACK_BUS_LOADER_ID, null, this)

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "> onRequestPermissionsResult(requestCode=$requestCode, permissions=${Arrays.toString(permissions)}, grantResults=${Arrays.toString(grantResults)})")
        }

        when (requestCode) {
            REQUEST_PERMISSION_LOCATION_UPDATE, REQUEST_PERMISSION_ACTIVITY_DETECTION -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "user granted permission")
                    mPresenter?.let { presenter ->
                        when (requestCode) {
                            REQUEST_PERMISSION_LOCATION_UPDATE -> presenter.startLocationUpdate()
                            REQUEST_PERMISSION_ACTIVITY_DETECTION -> presenter.startActivityDetection()
                        }
                    }
                } else {
                    uncheckSwitchDetectAutomatically()
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "user did NOT grant permission")
                        displayMessage(getString(R.string.fine_location_permission_rationale))
                    } else {
                        view?.let { rootView ->
                            val snackIntent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.parse("package:${activity.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            Snackbar.make(rootView, R.string.fine_location_permission_rationale, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.snackbar_action_settings) { startActivity(snackIntent) }
                                    .show()
                        }
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "> onRequestPermissionsResult(requestCode=$requestCode, permissions=${Arrays.toString(permissions)}, grantResults=${Arrays.toString(grantResults)})")
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<TrackBusMvp.Presenter> {
        Log.v(TAG, "> onCreateLoader(id=$id, args=$args)")

        val loader = TrackBusPresenterLoader(context.applicationContext)

        Log.v(TAG, "< onCreateLoader(id=$id, args=$args): $loader")
        return loader
    }

    override fun onLoaderReset(loader: Loader<TrackBusMvp.Presenter>) {
        Log.v(TAG, "> onLoaderReset(loader=$loader)")

        mPresenter?.let { presenter ->
            presenter.onDetach()
            mPresenter = null
        }

        Log.v(TAG, "< onLoaderReset(loader=$loader)")
    }

    override fun onLoadFinished(loader: Loader<TrackBusMvp.Presenter>, data: TrackBusMvp.Presenter) {
        Log.v(TAG, "> onLoadFinished(loader=$loader, data=$data)")

        mPresenter = data
        data.onAttach(this)

        Log.v(TAG, "< onLoadFinished(loader=$loader, data=$data)")
    }

    @UiThread
    override fun getBusId(): String? {
        return if (mSpinnerAdapter.hasActualBusData()) {
            spinnerBusID.selectedItem.toString()
        } else {
            null
        }
    }

    @UiThread
    override fun displayMessage(text: String) {
        Log.d(TAG, "displaying message: $text")
        context.toast(text)
    }

    @UiThread
    override fun enableBusId() {
        spinnerBusID.isEnabled = true

        buttonAddBus.isEnabled = true
        buttonAddBus.setImageDrawable(context.getDrawable(R.drawable.ic_add))
    }

    @UiThread
    override fun disableBusId() {
        spinnerBusID.isEnabled = false

        buttonAddBus.isEnabled = false
        // change button icon to grayscale
        val drawable = context.getDrawable(R.drawable.ic_add).mutate()
        drawable.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN)
        buttonAddBus.setImageDrawable(drawable)
    }

    @UiThread
    override fun uncheckSwitchDetectAutomatically() {
        switchDetectAutomatically.isChecked = false
    }

    @UiThread
    override fun setAvailableBuses(buses: List<Bus>, selectedBusId: String?) {
        mSpinnerAdapter.clear()
        mSpinnerAdapter.addAll(buses)

        if (!selectedBusId.isNullOrEmpty()) {
            val selectedBusIndex = buses.indexOfFirst { it.id == selectedBusId }
            spinnerBusID.setSelection(selectedBusIndex)
        }
    }

    @UiThread
    override fun setBusError(errorMessage: String) {
        mSpinnerAdapter.setErrorMessage(errorMessage)
        spinnerBusID.adapter = mSpinnerAdapter
        mSpinnerAdapter.notifyDataSetChanged()
        disableBusId()
    }

    @UiThread
    override fun onPositiveButtonClick(text: String) {
        mPresenter?.createBus(Bus(id = text))
    }

    @UiThread
    private fun buttonEnterBusClick(view: View) {
        if (getBusId().isNullOrEmpty()) {
            Log.d(TAG, "empty bus ID; cannot trigger location updates")
            displayMessage(getString(R.string.empty_bus_id_message))
            return
        }

        mPresenter?.let { presenter ->
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                presenter.startLocationUpdate()
            } else {
                Log.d(TAG, "the app doesn't have location permission; requesting it to the user")
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION_UPDATE)
            }
        }
    }

    @UiThread
    private fun switchDetectAutomaticallyChange(buttonView: CompoundButton, isChecked: Boolean) {
        mPresenter?.let { presenter ->
            if (isChecked && getBusId().isNullOrEmpty()) {
                Log.d(TAG, "empty bus ID; cannot trigger activity detection")
                displayMessage(getString(R.string.empty_bus_id_message))
                uncheckSwitchDetectAutomatically()
                return
            }

            if (isChecked) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    presenter.startActivityDetection()
                } else {
                    Log.d(TAG, "the app doesn't have location permission; requesting it to the user")
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACTIVITY_DETECTION)
                }
            } else {
                presenter.stopActivityDetection()
            }
        }
    }
}