package com.motorola.cdeives.motofretado.http;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Bus {
    public String id;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("long")
    public double longitude;
    @SerializedName("updated_at")
    public Date updatedAt;
    @SerializedName("created_at")
    public Date createdAt;
}
