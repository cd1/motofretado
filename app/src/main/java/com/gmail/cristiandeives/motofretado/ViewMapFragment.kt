package com.gmail.cristiandeives.motofretado

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gmail.cristiandeives.motofretado.http.Bus
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_view_map.*
import java.util.Arrays
import java.util.Date

@MainThread
internal class ViewMapFragment : Fragment(),
        LoaderManager.LoaderCallbacks<ViewMapMvp.Presenter>,
        OnMapReadyCallback,
        ViewMapMvp.View
{
    companion object {
        private val TAG = ViewMapFragment::class.java.simpleName
        private const val MAP_ZOOM_LEVEL = 15f // streets level
        private const val VIEW_MAP_LOADER_ID = 0
        private const val REQUEST_PERMISSION_INITIAL_LOCATION = 0
    }

    private lateinit var mSpinnerAdapter: BusSpinnerAdapter
    private var mMap: GoogleMap? = null
    private var mPresenter: ViewMapMvp.Presenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val rootView = inflater.inflate(R.layout.fragment_view_map, container, false)

        mSpinnerAdapter = BusSpinnerAdapter(context)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $rootView")
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        spinnerBusID.adapter = mSpinnerAdapter
        buttonViewMap.setOnClickListener { buttonViewMapClick() }

        map_view.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@ViewMapFragment)
        }

        disableBusIdInput()

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

        loaderManager.initLoader(VIEW_MAP_LOADER_ID, null, this)

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onResume() {
        Log.v(TAG, "> onResume()")
        super.onResume()

        map_view?.onResume()

        Log.v(TAG, "< onResume()")
    }

    override fun onPause() {
        Log.v(TAG, "> onPause()")
        super.onPause()

        map_view?.onPause()

        Log.v(TAG, "< onPause()")
    }

    override fun onStop() {
        Log.v(TAG, "> onStop()")
        super.onStop()

        if (!activity.isChangingConfigurations) {
            mPresenter?.stopViewingBusLocation()
        }

        Log.v(TAG, "< onStop()")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.v(TAG, "> onSaveInstanceState(outState=$outState)")
        super.onSaveInstanceState(outState)

        map_view?.onSaveInstanceState(outState)

        Log.v(TAG, "< onSaveInstanceState(outState=$outState)")
    }

    override fun onLowMemory() {
        Log.v(TAG, "> onLowMemory()")
        super.onLowMemory()

        map_view?.onLowMemory()

        Log.v(TAG, "< onLowMemory()")
    }

    override fun onDestroy() {
        Log.v(TAG, "> onDestroy()")
        super.onDestroy()

        map_view?.onDestroy()

        Log.v(TAG, "< onDestroy()")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "> onRequestPermissionsResult(requestCode=$requestCode, permissions=${Arrays.toString(permissions)}, grantResults=${Arrays.toString(grantResults)})")
        }

        when (requestCode) {
            REQUEST_PERMISSION_INITIAL_LOCATION -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    initializeMapCurrentLocation()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "< onRequestPermissionsResult(requestCode=$requestCode, permissions=${Arrays.toString(permissions)}, grantResults=${Arrays.toString(grantResults)})")
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ViewMapMvp.Presenter> {
        Log.v(TAG, "> onCreateLoader(id=$id, args=$args)")

        val loader = ViewMapPresenterLoader(context.applicationContext)

        Log.v(TAG, "< onCreateLoader(id=$id, args=$args): $loader")
        return loader
    }

    override fun onLoadFinished(loader: Loader<ViewMapMvp.Presenter>, data: ViewMapMvp.Presenter) {
        Log.v(TAG, "> onLoadFinished(loader=$loader, data=$data)")

        mPresenter = data
        data.onAttach(this)

        Log.v(TAG, "< onLoadFinished(loader=$loader, data=$data)")
    }

    override fun onLoaderReset(loader: Loader<ViewMapMvp.Presenter>) {
        Log.v(TAG, "> onLoaderReset(loader=$loader)")

        mPresenter?.let { presenter ->
            presenter.onDetach()
            mPresenter = null
        }

        Log.v(TAG, "< onLoaderReset(loader=$loader)")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // if the user has denied the location permission and rotates the screen,
        // we don't want to prompt for the permission again
        if (!map_view.isDirty) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                initializeMapCurrentLocation()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_INITIAL_LOCATION)
            }
        }
    }

    @UiThread
    private fun buttonViewMapClick() {
        val busID = getBusId()

        if (busID.isNullOrEmpty()) {
            Log.d(TAG, "empty bus ID; cannot trigger location updates")
            displayMessage(R.string.empty_bus_id_message)
            return
        }

        mPresenter?.startViewingBusLocation()
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
    override fun setMapMarker(busId: String, latitude: Double, longitude: Double, timestamp: Date?) {
        mMap?.let { map ->
            val title = if (timestamp != null) {
                val timeDiffMs = Date().time - timestamp.time
                val minutes = timeDiffMs / 60_000
                val seconds = (timeDiffMs - minutes * 60_000) / 1_000
                when {
                    // show "just now" when it's less than 5s
                    timeDiffMs < 5_000 -> getString(R.string.bus_marker_just_now, busId)
                    // show e.g. "10s" when it's less than 1m
                    timeDiffMs < 60_000 -> getString(R.string.bus_marker_seconds, busId, seconds)
                    // show e.g. "10m"
                    else -> getString(R.string.bus_marker_minutes, busId, minutes)
                }
            } else {
                busId
            }

            val latLng = LatLng(latitude, longitude)
            map.clear()
            map.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(title))
            map.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
        }
    }

    @UiThread
    override fun enableBusIdInput() {
        spinnerBusID.isEnabled = true
        buttonViewMap.isEnabled = true
    }

    @UiThread
    override fun disableBusIdInput() {
        spinnerBusID.isEnabled = false
        buttonViewMap.isEnabled = false
    }

    @UiThread
    override fun displayMessage(@StringRes messageId: Int) {
        context.toast(getString(messageId))
    }

    @UiThread
    override fun setAvailableBuses(buses: List<Bus>, defaultBusId: String?) {
        mSpinnerAdapter.clear()
        mSpinnerAdapter.addAll(buses)

        if (!defaultBusId.isNullOrEmpty()) {
            val defaultBusIdIndex = buses.indexOfFirst { it.id == defaultBusId }
            spinnerBusID.setSelection(defaultBusIdIndex)
        }
    }

    @UiThread
    override fun setBusError(errorMessage: String) {
        mSpinnerAdapter.setErrorMessage(errorMessage)
        spinnerBusID.adapter = mSpinnerAdapter
        mSpinnerAdapter.notifyDataSetChanged()
        disableBusIdInput()
    }

    private fun initializeMapCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap?.let { googleMap ->
                LocationServices.getFusedLocationProviderClient(activity).lastLocation
                        .addOnSuccessListener { location: Location? ->
                            location?.let { loc ->
                                val latLng = LatLng(loc.latitude, loc.longitude)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
                            }
                        }
                        .addOnFailureListener { ex ->
                            Log.e(TAG, "failed to get current location; using default map view", ex)
                        }
            }
        } else {
            Log.w(TAG, "cannot initialize map to current location because the user hasn't granted permission")
        }
    }
}