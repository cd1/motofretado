package com.motorola.cdeives.motofretado.http;

public interface ModelListener<T> {
    void onSuccess(T data);
    void onError(Exception ex);
}
