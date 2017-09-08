package com.github.pozo.analytics.message;

public interface MessageFiller<M, T> {
    public T fillMessage(M message);
}
