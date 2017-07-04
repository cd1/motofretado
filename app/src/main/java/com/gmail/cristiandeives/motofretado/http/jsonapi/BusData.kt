package com.gmail.cristiandeives.motofretado.http.jsonapi

import com.gmail.cristiandeives.motofretado.http.Bus

internal data class BusData(val type: String, val id: String, val attributes: BusAttributes?) {
    internal constructor(bus: Bus) : this(BusDocument.TYPE, bus.id, BusAttributes(bus))

    internal fun toBus(): Bus {
        if (type != BusDocument.TYPE) {
            throw IllegalStateException("invalid JSONAPI type: $type")
        }

        if (id.isEmpty()) {
            throw IllegalStateException("missing JSONAPI ID")
        }

        return Bus(
                id,
                attributes?.latitude,
                attributes?.longitude,
                attributes?.createdAt,
                attributes?.updatedAt
        )
    }
}