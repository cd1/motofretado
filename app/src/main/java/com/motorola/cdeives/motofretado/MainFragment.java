package com.motorola.cdeives.motofretado;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class MainFragment extends Fragment implements View.OnClickListener, MainPresenterView {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 0;

    private MainPresenter mPresenter;
    private EditText mEditBusNumber;

    @UiThread
    private void buttonEnterBusClick() {
        if (TextUtils.isEmpty(getBusID())) {
            Log.d(TAG, "empty bus ID; cannot trigger location updates");
            displayToast(getResources().getString(R.string.empty_bus_id_message));
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mPresenter.startLocationUpdate();
        } else {
            Log.d(TAG, "the app doesn't have location permission");
            Log.d(TAG, "requesting location permission to the user");
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION);

        }
    }

    @UiThread
    private void buttonLeaveBusClick() {
        mPresenter.stopLocationUpdate();
    }

    @Override
    @MainThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate([Bundle])");

        super.onCreate(savedInstanceState);

        mPresenter = new MainPresenterImpl(getActivity(), this);

        Log.v(TAG, "< onCreate([Bundle])");
    }

    @Override
    @MainThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "> onCreateView([LayoutInflater, ViewGroup, Bundle])");

        View rootView = inflater.inflate(R.layout.fragment_main, container, true);

        mEditBusNumber = (EditText) rootView.findViewById(R.id.editBusNumber);

        Button buttonEnterBus = (Button) rootView.findViewById(R.id.buttonEnterBus);
        buttonEnterBus.setOnClickListener(this);

        Button buttonLeaveBus = (Button) rootView.findViewById(R.id.buttonLeaveBus);
        buttonLeaveBus.setOnClickListener(this);

        Log.v(TAG, "< onCreateView([LayoutInflater, ViewGroup, Bundle])");

        return rootView;
    }

    @Override
    @MainThread
    public void onStart() {
        Log.v(TAG, "> setUp()");

        super.onStart();

        mPresenter.setUp();

        Log.v(TAG, "< setUp()");
    }

    @Override
    @MainThread
    public void onStop() {
        Log.v(TAG, "> tearDown()");

        super.onStop();

        mPresenter.tearDown();

        Log.v(TAG, "< tearDown()");
    }

    @Override
    @MainThread
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "> onRequestPermissionsResult("
                + requestCode + ", "
                + Arrays.toString(permissions) + ", "
                + Arrays.toString(grantResults) + ")");

        switch (requestCode) {
            case REQUEST_FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "user granted permission");
                    mPresenter.startLocationUpdate();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "user did NOT grant permission");
                        displayToast(getString(R.string.fine_location_permission_rationale));
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
    public void onClick(View v) {
        Log.v(TAG, "> onClick(" + getResources().getResourceEntryName(v.getId())+ ")");

        switch (v.getId()) {
            case R.id.buttonEnterBus:
                buttonEnterBusClick();
                break;
            case R.id.buttonLeaveBus:
                buttonLeaveBusClick();
                break;
            default:
                Log.wtf(TAG, "I don't know how to handle this view's click: " + v.getId());
        }

        Log.v(TAG, "< onClick(" + getResources().getResourceEntryName(v.getId())+ ")");
    }

    @Override
    @UiThread
    public @NonNull String getBusID() {
        return mEditBusNumber.getText().toString();
    }

    @Override
    @UiThread
    public void displayToast(@Nullable String text) {
        Log.d(TAG, "displaying Toast: " + text);

        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    @UiThread
    public void enableBusID() {
        mEditBusNumber.setEnabled(true);
    }

    @Override
    @UiThread
    public void disableBusID() {
        mEditBusNumber.setEnabled(false);
    }
}