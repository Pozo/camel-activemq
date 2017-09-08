package com.github.pozo.analytics.message;

interface MessageProducerListener {
    void received(MessageProducerEvent event);
}
