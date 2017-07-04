package com.gmail.cristiandeives.motofretado.http.jsonapi

import com.gmail.cristiandeives.motofretado.http.Bus

internal data class BusDocument(val jsonapi: JSONAPI?, val data: BusData, val links: Links?) {
    companion object {
        internal const val TYPE = "bus"
    }

    internal constructor(bus: Bus) : this(JSONAPI(CURRENT_VERSION), BusData(bus), null)

    internal fun toBus(): Bus {
        jsonapi?.validateVersion()

        return data.toBus()
    }
}