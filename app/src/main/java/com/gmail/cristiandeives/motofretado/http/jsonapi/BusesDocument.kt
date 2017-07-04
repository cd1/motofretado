package com.gmail.cristiandeives.motofretado.http.jsonapi

import com.gmail.cristiandeives.motofretado.http.Bus

internal data class BusesDocument(val jsonapi: JSONAPI?, val data: List<BusData>, val links: Links?) {
    internal fun toBuses(): List<Bus> {
        jsonapi?.validateVersion()

        return data.map { it.toBus() }
    }
}