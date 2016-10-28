package com.chemaxon.analytics.message;

import com.chemaxon.analytics.ProducerEvent;

import java.util.EventObject;

class MessageProducerEvent extends EventObject {

    MessageProducerEvent(ProducerEvent producerEvent) {
        super(producerEvent);
    }

    @Override
    public ProducerEvent getSource() {
        return (ProducerEvent) super.getSource();
    }
}
