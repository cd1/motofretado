package com.gmail.cristiandeives.motofretado;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.gmail.cristiandeives.motofretado.http.Bus;

import java.util.Arrays;
import java.util.List;

public class TrackBusFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<TrackBusMvp.Presenter>, TrackBusMvp.View,
        AddBusDialogFragment.OnClickListener {
    private static final String TAG = TrackBusFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_LOCATION_UPDATE = 0;
    private static final int REQUEST_PERMISSION_ACTIVITY_DETECTION = 1;
    private static final int TRACK_BUS_LOADER_ID = 0;

    private @Nullable TrackBusMvp.Presenter mPresenter;
    private Spinner mSpinnerBusId;
    private Switch mSwitchDetectAutomatically;
    private BusSpinnerAdapter mSpinnerAdapter;

    @UiThread
    private void buttonEnterBusClick() {
        if (TextUtils.isEmpty(getBusId())) {
            Log.d(TAG, "empty bus ID; cannot trigger location updates");
            displayMessage(getString(R.string.empty_bus_id_message));
            return;
        }

        if (mPresenter == null) {
            Log.w(TAG, "presenter is null; cannot start update location service");
            return;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mPresenter.startLocationUpdate();
        } else {
            Log.d(TAG, "the app doesn't have location permission; requesting it to the user");
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION_UPDATE);
        }
    }

    @UiThread
    private void switchDetectAutomaticallyChange(boolean isChecked) {
        // ignore event if presenter isn't ready
        if (mPresenter == null) {
            Log.d(TAG, "presenter is null; cannot perform any action due to the switch change");
            return;
        }

        if (isChecked && TextUtils.isEmpty(getBusId())) {
            Log.d(TAG, "empty bus ID; cannot trigger activity detection");
            displayMessage(getString(R.string.empty_bus_id_message));
            uncheckSwitchDetectAutomatically();
            return;
        }

        if (isChecked) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                mPresenter.startActivityDetection();
            } else {
                Log.d(TAG, "the app doesn't have location permission; requesting it to the user");
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_ACTIVITY_DETECTION);
            }
        } else {
            mPresenter.stopActivityDetection();
        }
    }

    @Override
    @MainThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "> onCreateView([LayoutInflater, ViewGroup, Bundle])");

        View rootView = inflater.inflate(R.layout.fragment_track_bus, container, false);

        mSpinnerBusId = (Spinner) rootView.findViewById(R.id.spinnerBusID);
        mSpinnerAdapter = new BusSpinnerAdapter(getContext());
        mSpinnerBusId.setAdapter(mSpinnerAdapter);

        ImageButton buttonAddBus = (ImageButton) rootView.findViewById(R.id.buttonAddBus);
        buttonAddBus.setOnClickListener(view -> {
            DialogFragment fragment = new AddBusDialogFragment();
            fragment.setTargetFragment(TrackBusFragment.this, 0);
            fragment.show(getFragmentManager(), AddBusDialogFragment.class.getName());
        });

        Button buttonEnterBus = (Button) rootView.findViewById(R.id.buttonEnterBus);
        buttonEnterBus.setOnClickListener(view -> buttonEnterBusClick());

        Button buttonLeaveBus = (Button) rootView.findViewById(R.id.buttonLeaveBus);
        buttonLeaveBus.setOnClickListener(view -> {
            if (mPresenter != null) mPresenter.stopLocationUpdate();
        });

        mSwitchDetectAutomatically = (Switch) rootView.findViewById(R.id.switchDetectAutomatically);
        mSwitchDetectAutomatically.setOnCheckedChangeListener(
                (button, isChecked) -> switchDetectAutomaticallyChange(isChecked));

        disableBusId();

        Log.v(TAG, "< onCreateView([LayoutInflater, ViewGroup, Bundle])");

        return rootView;
    }

    @Override
    @MainThread
    public void onDestroyView() {
        Log.v(TAG, "> onDestroyView()");
        super.onDestroyView();

        if (mPresenter != null) {
            mPresenter.onDetach();
        }

        Log.v(TAG, "< onDestroyView()");
    }

    @Override
    @MainThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onActivityCreated(" + savedInstanceState + ")");

        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRACK_BUS_LOADER_ID, null, this);

        Log.v(TAG, "< onActivityCreated(" + savedInstanceState + ")");
    }

    @Override
    @MainThread
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "> onRequestPermissionsResult("
                + requestCode + ", "
                + Arrays.toString(permissions) + ", "
                + Arrays.toString(grantResults) + ")");

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION_UPDATE:
            case REQUEST_PERMISSION_ACTIVITY_DETECTION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "user granted permission");
                    if (mPresenter != null) {
                        switch (requestCode) {
                            case REQUEST_PERMISSION_LOCATION_UPDATE:
                                mPresenter.startLocationUpdate();
                            case REQUEST_PERMISSION_ACTIVITY_DETECTION:
                                mPresenter.startActivityDetection();
                        }
                    } else {
                        Log.w(TAG, "presenter is null; cannot start service");
                    }
                } else {
                    uncheckSwitchDetectAutomatically();
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "user did NOT grant permission");
                        displayMessage(getString(R.string.fine_location_permission_rationale));
                    } else {
                        View rootView = getView();
                        if (rootView != null) {
                            Snackbar.make(rootView, R.string.fine_location_permission_rationale, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.snackbar_action_settings, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.v(TAG, "> buttonEnterBusClick#onClick(" + getResources().getResourceEntryName(v.getId()) + ")");

                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.parse("package:" + getActivity().getPackageName()));
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                            Log.v(TAG, "< buttonEnterBusClick#onClick(" + getResources().getResourceEntryName(v.getId()) + ")");
                                        }
                                    }).show();
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        Log.v(TAG, "< onRequestPermissionsResult("
                + requestCode + ", "
                + Arrays.toString(permissions) + ", "
                + Arrays.toString(grantResults) + ")");
    }

    @Override
    @MainThread
    public Loader<TrackBusMvp.Presenter> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "> onCreateLoader(" + id + ", " + args + ")");

        Loader<TrackBusMvp.Presenter> presenterLoader = null;

        switch (id) {
            case TRACK_BUS_LOADER_ID:
                presenterLoader = new TrackBusPresenterLoader(getContext().getApplicationContext());
                break;
            default:
                Log.wtf(TAG, "I don't know how to handle this loader ID: " + id);
        }

        Log.v(TAG, "< onCreateLoader(" + id + ", " + args + ")");

        return presenterLoader;
    }

    @Override
    @MainThread
    public void onLoaderReset(Loader<TrackBusMvp.Presenter> loader) {
        Log.v(TAG, "> onLoaderReset([Loader<Presenter>])");

        if (mPresenter != null) {
            mPresenter.onDetach();
            mPresenter = null;
        }

        Log.v(TAG, "< onLoaderReset([Loader<Presenter>])");
    }

    @Override
    @MainThread
    public void onLoadFinished(Loader<TrackBusMvp.Presenter> loader, TrackBusMvp.Presenter data) {
        Log.v(TAG, "> onLoadFinished([Loader<Presenter>], [Presenter])");

        mPresenter = data;
        mPresenter.onAttach(this);

        Log.v(TAG, "< onLoadFinished([Loader<Presenter>], [Presenter])");
    }

    @Override
    @UiThread
    public @Nullable String getBusId() {
        return (mSpinnerAdapter != null && mSpinnerAdapter.hasActualBusData())
                ? mSpinnerBusId.getSelectedItem().toString()
                : null;
    }

    @Override
    @UiThread
    public void displayMessage(@Nullable String text) {
        Log.d(TAG, "displaying message: " + text);

        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    @UiThread
    public void enableBusId() {
        mSpinnerBusId.setEnabled(true);
    }

    @Override
    @UiThread
    public void disableBusId() {
        mSpinnerBusId.setEnabled(false);
    }

    @Override
    @UiThread
    public void uncheckSwitchDetectAutomatically() {
        mSwitchDetectAutomatically.setChecked(false);
    }

    @Override
    @UiThread
    public void setAvailableBuses(@NonNull List<Bus> buses, @Nullable String selectedBusId) {
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(buses);

        if (!TextUtils.isEmpty(selectedBusId)) {
            for (int i = 0; i < buses.size(); i++) {
                if (buses.get(i).id.equals(selectedBusId)) {
                    mSpinnerBusId.setSelection(i);
                }
            }
        }
    }

    @Override
    @UiThread
    public void setBusError(@NonNull String errorMessage) {
        mSpinnerAdapter.setError(errorMessage);
        mSpinnerBusId.setAdapter(mSpinnerAdapter);
        mSpinnerAdapter.notifyDataSetChanged();
        disableBusId();
    }

    @Override
    @UiThread
    public void onPositiveButtonClick(String text) {
        if (mPresenter != null) {
            Bus bus = new Bus();
            bus.id = text;

            mPresenter.createBus(bus);
        } else {
            Log.w(TAG, "presenter is null; cannot add new bus");
        }
    }
}
