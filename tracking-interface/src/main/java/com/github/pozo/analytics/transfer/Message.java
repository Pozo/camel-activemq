package com.github.pozo.analytics.transfer;

public interface Message<T> {
    T getMessage();

    String getClientId();

    String getSessionId();
}
