package com.gmail.cristiandeives.motofretado.http.jsonapi

import com.gmail.cristiandeives.motofretado.http.Error

internal data class ErrorDocument(val jsonapi: JSONAPI?, val errors: List<ErrorData>, val links: Links?) {
    internal fun toError(): Error {
        jsonapi?.validateVersion()
        val firstError = errors.firstOrNull() ?: throw IllegalStateException("no error found in document")

        return Error(firstError.status, firstError.title, firstError.detail)
    }
}