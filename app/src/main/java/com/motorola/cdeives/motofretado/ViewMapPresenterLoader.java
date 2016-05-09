package com.motorola.cdeives.motofretado;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.v4.content.Loader;
import android.util.Log;

public class ViewMapPresenterLoader extends Loader<ViewMapMvp.Presenter> {
    private static final String TAG = ViewMapPresenterLoader.class.getSimpleName();

    private ViewMapMvp.Presenter mPresenter;

    public ViewMapPresenterLoader(Context context) {
        super(context);
    }

    @Override
    @MainThread
    protected void onStartLoading() {
        Log.v(TAG, "> onStartLoading()");

        if (mPresenter == null) {
            forceLoad();
        } else {
            deliverResult(mPresenter);
        }

        Log.v(TAG, "< onStartLoading()");
    }

    @Override
    @MainThread
    protected void onForceLoad() {
        Log.v(TAG, "> onForceLoad()");

        mPresenter = new ViewMapPresenter(getContext());
        deliverResult(mPresenter);

        Log.v(TAG, "< onForceLoad()");
    }

    @Override
    @MainThread
    protected void onReset() {
        Log.v(TAG, "> onReset()");

        mPresenter = null;

        Log.v(TAG, "< onReset()");
    }
}
