package com.motorola.cdeives.motofretado;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewMapFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ViewMapMvp.Presenter>, ViewMapMvp.View {
    private static final String TAG = ViewMapFragment.class.getSimpleName();
    private static final int MAP_ZOOM_LEVEL = 15; // streets level
    private static final int VIEW_MAP_LOADER_ID = 0;

    private EditText mEditBusID;
    private Button mButtonViewMap;
    private GoogleMap mMap;
    private MapView mMapView;
    private ViewMapMvp.Presenter mPresenter;

    @UiThread
    private void buttonViewMapClick() {
        String busID = mEditBusID.getText().toString();

        if (TextUtils.isEmpty(busID)) {
            Log.d(TAG, "empty bus ID; cannot trigger location updates");
            Toast.makeText(getActivity(), R.string.empty_bus_id_message, Toast.LENGTH_SHORT).show();
            return;
        }

        mPresenter.startViewingBusLocation(busID);
    }

    @Override
    @MainThread
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onCreateView([LayoutInflater, ViewGroup, Bundle])");

        View rootView = inflater.inflate(R.layout.fragment_view_map, container, false);

        mEditBusID = (EditText) rootView.findViewById(R.id.editBusID);

        mButtonViewMap = (Button) rootView.findViewById(R.id.buttonViewMap);
        mButtonViewMap.setOnClickListener(view -> buttonViewMapClick());

        mMapView = (MapView) rootView.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(map -> {
            mMap = map;
            mEditBusID.setEnabled(true);
            mButtonViewMap.setEnabled(true);
        });

        Log.v(TAG, "< onCreateView([LayoutInflater, ViewGroup, Bundle])");

        return rootView;
    }

    @Override
    @MainThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=" + savedInstanceState + ")");

        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(VIEW_MAP_LOADER_ID, null, this);

        Log.v(TAG, "< onActivityCreated(savedInstanceState=" + savedInstanceState + ")");
    }

    @Override
    @MainThread
    public void onResume() {
        Log.v(TAG, "> onResume()");

        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

        Log.v(TAG, "< onResume()");
    }

    @Override
    @MainThread
    public void onPause() {
        Log.v(TAG, "> onPause()");

        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }

        Log.v(TAG, "< onPause()");
    }

    @Override
    @MainThread
    public void onStop() {
        Log.v(TAG, "> onStop()");

        super.onStop();
        mPresenter.onStop();

        Log.v(TAG, "< onStop()");
    }

    @Override
    @MainThread
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "> onSaveInstanceState(outState=" + outState + ")");

        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }

        Log.v(TAG, "< onSaveInstanceState(outState=" + outState + ")");
    }

    @Override
    @MainThread
    public void onLowMemory() {
        Log.v(TAG, "> onLowMemory()");

        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }

        Log.v(TAG, "< onLowMemory()");
    }

    @Override
    @MainThread
    public void onDestroy() {
        Log.v(TAG, "> onDestroy()");

        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDetach();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }

        Log.v(TAG, "< onDestroy()");
    }

    @Override
    public Loader<ViewMapMvp.Presenter> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "> onCreateLoader(id=" + id + ", args=" + args + ")");

        Loader<ViewMapMvp.Presenter> presenterLoader = null;

        switch (id) {
            case VIEW_MAP_LOADER_ID:
                presenterLoader = new ViewMapPresenterLoader(getContext().getApplicationContext());
                break;
            default:
                Log.wtf(TAG, "I don't know how to handle this loader ID: " + id);
        }

        Log.v(TAG, "< onCreateLoader(id=" + id + ", args=" + args + ")");

        return presenterLoader;
    }

    @Override
    public void onLoadFinished(Loader<ViewMapMvp.Presenter> loader, ViewMapMvp.Presenter data) {
        Log.v(TAG, "> onLoadFinished([Loader<Presenter>], [Presenter])");

        mPresenter = data;
        mPresenter.onAttach(this);

        Log.v(TAG, "< onLoadFinished([Loader<Presenter>], [Presenter])");
    }

    @Override
    public void onLoaderReset(Loader<ViewMapMvp.Presenter> loader) {
        Log.v(TAG, "> onLoaderReset([Loader<Presenter>]");

        mPresenter = null;

        Log.v(TAG, "< onLoaderReset([Loader<Presenter>]");
    }

    @Override
    @UiThread
    public void setMapMarker(String title, double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title));
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(latLng, MAP_ZOOM_LEVEL));
    }

    @Override
    @UiThread
    public void enableBusId() {
        mEditBusID.setEnabled(true);
    }

    @Override
    @UiThread
    public void disableBusId() {
        mEditBusID.setEnabled(false);
    }
}
