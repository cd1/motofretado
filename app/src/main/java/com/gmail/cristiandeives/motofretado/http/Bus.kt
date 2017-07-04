package com.gmail.cristiandeives.motofretado.http

import com.gmail.cristiandeives.motofretado.http.jsonapi.BusDocument
import com.gmail.cristiandeives.motofretado.http.jsonapi.BusesDocument
import java.util.Date

internal data class Bus(
        val id: String,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val createdAt: Date? = null,
        val updatedAt: Date? = null
) {
    companion object {
        internal fun createFromHTTPBody(body: String): Bus {
            val doc = Gson.gson.fromJson(body, BusDocument::class.java)
            return doc.toBus()
        }

        internal fun createBusesFromHTTPBody(body: String): List<Bus> {
            val doc = Gson.gson.fromJson(body, BusesDocument::class.java)
            return doc.toBuses()
        }
    }

    // TODO: do not override this method and leave the presentation details with the SpinnerAdapter
    override fun toString() = id

    internal fun toHTTPBody(): String {
        val doc = BusDocument(this)
        return Gson.gson.toJson(doc, BusDocument::class.java)
    }
}