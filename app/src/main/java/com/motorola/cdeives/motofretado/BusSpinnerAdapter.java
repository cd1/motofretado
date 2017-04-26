package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.motorola.cdeives.motofretado.http.Bus;

import java.util.List;

class BusSpinnerAdapter extends ArrayAdapter<Bus> {
    private static final String TAG = BusSpinnerAdapter.class.getSimpleName();

    private final @NonNull LayoutInflater mInflater;

    BusSpinnerAdapter(Context context, List<Bus> buses) {
        super(context, android.R.layout.simple_spinner_item, buses);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.v(TAG, "> getCount()");

        int count = (super.getCount() > 0)
                ? super.getCount()
                : 1;

        Log.v(TAG, "< getCount(): " + count);
        return count;
    }

    @Override
    public @Nullable Bus getItem(int position) {
        Log.v(TAG, "> getItem(position=" + position + ")");

        Bus item = (super.getCount() > 0)
                ? super.getItem(position)
                : null;

        Log.v(TAG, "< getItem(position=" + position + "): " + item);
        return item;
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView,
                                 @NonNull ViewGroup parent) {
        Log.v(TAG, "> getView(position=" + position + ", convertView=" + convertView
                + ", parent=" + parent + ")");

        View view;

        if (super.getCount() == 0) {
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent,
                        false);
            }

            TextView textView = (TextView) convertView;
            textView.setText(R.string.no_buses_available);

            view = textView;
        } else {
            view = super.getView(position, convertView, parent);
        }

        Log.v(TAG, "< getView(position=" + position + ", convertView=" + convertView
                + ", parent=" + parent + "): " + view);
        return view;
    }

    boolean hasActualBusData() {
        return (super.getCount() > 0);
    }
}
