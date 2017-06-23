package com.gmail.cristiandeives.motofretado.http;

public interface ModelListener<T> {
    void onSuccess(T data);
    void onError(Exception ex);
}
