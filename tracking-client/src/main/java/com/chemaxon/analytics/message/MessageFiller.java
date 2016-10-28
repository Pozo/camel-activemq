package com.chemaxon.analytics.message;

public interface MessageFiller<M,T> {
    public T fillMessage(M message);
}
