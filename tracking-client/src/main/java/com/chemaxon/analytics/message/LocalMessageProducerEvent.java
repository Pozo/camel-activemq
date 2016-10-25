package com.chemaxon.analytics.message;

import com.chemaxon.analytics.ProducerEvent;

import java.util.EventObject;

class LocalMessageProducerEvent extends EventObject {

    LocalMessageProducerEvent(ProducerEvent producerEvent) {
        super(producerEvent);
    }

    @Override
    public ProducerEvent getSource() {
        return (ProducerEvent) super.getSource();
    }
}
