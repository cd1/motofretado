package com.motorola.cdeives.motofretado;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

@MainThread
public class AddBusDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener, TextWatcher {
    private static final String TAG = AddBusDialogFragment.class.getSimpleName();

    private @Nullable OnClickListener mOnClickListener;
    private EditText mEditBusId;
    private Button mButtonOK;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate(savedInstanceState=" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);

        Fragment fragment = getTargetFragment();

        if (fragment instanceof OnClickListener) {
            mOnClickListener = (OnClickListener) fragment;
        } else {
            Log.wtf(TAG, "parent fragment cannot handle clicks; dialog buttons won't work");
        }

        Log.v(TAG, "< onCreate(savedInstanceState=" + savedInstanceState + ")");
    }

    @Override
    public void onStart() {
        Log.v(TAG, "> onStart()");
        super.onStart();

        mEditBusId = (EditText) getDialog().findViewById(R.id.editBusId);
        mEditBusId.addTextChangedListener(this);

        mButtonOK = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        mButtonOK.setEnabled(false);

        Log.v(TAG, "< onStart()");
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "> onCreateDialog(savedInstanceState=" + savedInstanceState + ")");
        super.onCreateDialog(savedInstanceState);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.add_bus_dialog_title)
                .setView(R.layout.dialog_add_bus)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        Log.v(TAG, "< onCreateDialog(savedInstanceState=" + savedInstanceState + "): "
                + dialog);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.v(TAG, "> onClick(dialog=" + dialog + ", which=" + which + ")");

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mOnClickListener != null) {
                    String busId = mEditBusId.getText().toString().trim();
                    mOnClickListener.onPositiveButtonClick(busId);
                } else {
                    Log.wtf(TAG, "parent fragment callback is null");
                }
                break;
            default:
                Log.wtf(TAG, "unexpected button click: " + which);
        }

        Log.v(TAG, "< onClick(dialog=" + dialog + ", which=" + which + ")");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.v(TAG, "beforeTextChanged(s=" + s + ", start=" + start
                + ", count=" + count + ", after=" + after + ")");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v(TAG, "onTextChanged(s=" + s + ", start=" + start
                + ", before=" + before + ", count=" + count + ")");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "> afterTextChanged(s=" + s + ")");

        mButtonOK.setEnabled(s.toString().trim().length() > 0);

        Log.v(TAG, "< afterTextChanged(s=" + s + ")");
    }

    interface OnClickListener {
        @UiThread
        void onPositiveButtonClick(String text);
    }
}
