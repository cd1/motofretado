package com.gmail.cristiandeives.motofretado.http

import android.util.Log

internal object EmptyModelListener : ModelListener<Any> {
    private val TAG = EmptyModelListener::class.java.simpleName

    override fun onSuccess(data: Any) {
        Log.v(TAG, "onSuccess(data=$data)")
    }

    override fun onError(ex: Exception) {
        Log.v(TAG, "onError(ex=$ex)")
    }
}