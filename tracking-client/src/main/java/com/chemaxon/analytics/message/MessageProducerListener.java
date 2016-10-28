package com.chemaxon.analytics.message;

interface MessageProducerListener {
    void received(MessageProducerEvent event);
}
