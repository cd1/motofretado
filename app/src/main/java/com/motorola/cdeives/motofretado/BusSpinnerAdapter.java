package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.motorola.cdeives.motofretado.http.Bus;

import java.util.List;

class BusSpinnerAdapter extends ArrayAdapter<Bus> {
    BusSpinnerAdapter(Context context, List<Bus> buses) {
        super(context, android.R.layout.simple_spinner_item, buses);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
