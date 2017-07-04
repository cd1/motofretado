package com.gmail.cristiandeives.motofretado.http

internal interface ModelListener<in T> {
    fun onSuccess(data: T)
    fun onError(ex: Exception)
}