package com.gmail.cristiandeives.motofretado.http.jsonapi

internal data class ErrorData(
        val status: String?,
        val source: ErrorSource?,
        val title: String?,
        val detail: String?
)