package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.support.annotation.MainThread
import android.support.v4.content.Loader
import android.util.Log

@MainThread
internal class TrackBusPresenterLoader(context: Context) : Loader<TrackBusMvp.Presenter>(context) {
    companion object {
        private val TAG = TrackBusPresenterLoader::class.java.simpleName
    }

    private var mPresenter: TrackBusMvp.Presenter? = null

    override fun onStartLoading() {
        Log.v(TAG, "> onStartLoading()")

        if (mPresenter == null) {
            forceLoad()
        } else {
            deliverResult(mPresenter)
        }

        Log.v(TAG, "< onStartLoading()")
    }

    override fun onForceLoad() {
        Log.v(TAG, "> onForceLoad()")

        mPresenter = TrackBusPresenter(context.applicationContext)
        deliverResult(mPresenter)

        Log.v(TAG, "< onForceLoad()")
    }

    override fun onReset() {
        Log.v(TAG, "> onReset()")

        mPresenter = null

        Log.v(TAG, "< onReset()")
    }
}