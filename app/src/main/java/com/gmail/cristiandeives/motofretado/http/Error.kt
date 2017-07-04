package com.gmail.cristiandeives.motofretado.http

import com.gmail.cristiandeives.motofretado.http.jsonapi.ErrorDocument

internal data class Error(val status: String?, val title: String?, val detail: String?) : Throwable(title) {
    companion object {
        internal fun createFromHTTPBody(body: String): Error {
            val doc = Gson.gson.fromJson(body, ErrorDocument::class.java)
            return doc.toError()
        }
    }

    override fun toString() = "$title [$status]: $detail"
}