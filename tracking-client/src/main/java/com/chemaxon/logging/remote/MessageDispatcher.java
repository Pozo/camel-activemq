package com.chemaxon.logging.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

class MessageDispatcher implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private final List<LocalMessageProducerListener> listeners = new ArrayList<LocalMessageProducerListener>();

    public synchronized void addLocalMessageProducerListener(LocalMessageProducerListener localMessageProducerListener) {
        listeners.add(localMessageProducerListener);
    }

    public synchronized void removeLocalMessageProducerListener(LocalMessageProducerListener localMessageProducerListener) {
        listeners.remove(localMessageProducerListener);
    }

    private synchronized void fireLocalMessageProducerEvent() {
        for (LocalMessageProducerListener listener : listeners) {
            listener.received(new LocalMessageProducerEvent("started"));
        }
    }

    @Override
    public void run() {
        logger.info("start local message dispatcher trhread");
        try {
            LocalMessageProducer localMessageProducer = new LocalMessageProducer();

        } catch (JMSException e) {
            logger.error(e.getMessage());
        }
    }
}
