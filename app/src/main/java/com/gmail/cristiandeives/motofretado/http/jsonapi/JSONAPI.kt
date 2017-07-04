package com.gmail.cristiandeives.motofretado.http.jsonapi

internal data class JSONAPI(val version: String?) {
    internal fun validateVersion() {
        version?.let { ver ->
            if (ver != CURRENT_VERSION) {
                throw IllegalStateException("unsupported JSONAPI version: $ver")
            }
        }
    }
}