package com.gmail.cristiandeives.motofretado;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.cristiandeives.motofretado.http.Bus;

@UiThread
class BusSpinnerAdapter extends ArrayAdapter<Bus> {
    private static final String TAG = BusSpinnerAdapter.class.getSimpleName();
    private static final @LayoutRes int DROPDOWN_ITEM_RESOURCE =
            android.R.layout.simple_spinner_dropdown_item;
    private static final @LayoutRes int ITEM_RESOURCE = android.R.layout.simple_spinner_item;

    private final @NonNull LayoutInflater mInflater;
    private @Nullable String mErrorMessage;

    BusSpinnerAdapter(@NonNull Context context) {
        super(context, ITEM_RESOURCE);
        setDropDownViewResource(DROPDOWN_ITEM_RESOURCE);
        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.v(TAG, "> getCount()");

        int count = Math.max(super.getCount(), 1);

        Log.v(TAG, "< getCount(): " + count);
        return count;
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView,
                                 @NonNull ViewGroup parent) {
        Log.v(TAG, "> getView(position=" + position + ", convertView=" + convertView
                + ", parent=" + parent + ")");

        View view;

        if (!hasActualBusData()) {
            if (convertView == null) {
                convertView = mInflater.inflate(ITEM_RESOURCE, parent, false);
            }

            TextView textView = (TextView) convertView;
            if (!TextUtils.isEmpty(mErrorMessage)) {
                textView.setError("");
                textView.setText(mErrorMessage);
            } else {
                textView.setText(R.string.loading_bus_numbers);
            }

            view = textView;
        } else {
            view = super.getView(position, convertView, parent);
        }

        Log.v(TAG, "< getView(position=" + position + ", convertView=" + convertView
                + ", parent=" + parent + "): " + view);
        return view;
    }

    void setError(@NonNull String message) {
        mErrorMessage = message;
        clear();
    }

    boolean hasActualBusData() {
        return (super.getCount() > 0);
    }
}
