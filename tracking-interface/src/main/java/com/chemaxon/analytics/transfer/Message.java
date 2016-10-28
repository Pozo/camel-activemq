package com.chemaxon.analytics.transfer;

public interface Message<T> {
    T getMessage();

    String getClientId();

    String getSessionId();
}
