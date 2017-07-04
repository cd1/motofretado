package com.gmail.cristiandeives.motofretado.http.jsonapi

import com.gmail.cristiandeives.motofretado.http.Bus
import com.google.gson.annotations.SerializedName
import java.util.Date

internal data class BusAttributes(
        val latitude: Double?,
        val longitude: Double?,
        @SerializedName("created_at") val createdAt: Date?,
        @SerializedName("updated_at") val updatedAt: Date?
) {
    constructor(bus: Bus) : this(
            bus.latitude,
            bus.longitude,
            bus.createdAt,
            bus.updatedAt
    )
}