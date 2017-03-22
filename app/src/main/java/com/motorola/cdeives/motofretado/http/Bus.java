package com.motorola.cdeives.motofretado.http;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Bus implements Cloneable {
    public String id;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("long")
    public double longitude;
    @SerializedName("updated_at")
    public Date updatedAt;
    @SerializedName("created_at")
    public Date createdAt;

    @Override
    public Bus clone() {
        Bus bus;
        try {
            bus = (Bus) super.clone();
        } catch (CloneNotSupportedException ex) {
            // should never happen
            return null;
        }

        if (updatedAt != null) {
            bus.updatedAt = (Date) updatedAt.clone();
        }

        if (createdAt != null) {
            bus.createdAt = (Date) createdAt.clone();
        }

        return bus;
    }
}
