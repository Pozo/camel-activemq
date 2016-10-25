package com.chemaxon.analytics.message;

import com.chemaxon.analytics.ProducerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageDispatcher implements Runnable, LocalMessageProducerListener {
    private final Stack<String> messageQueue = new Stack<String>();

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private final AtomicBoolean localMessageProducerStarted = new AtomicBoolean(false);

    private LocalMessageProducer localMessageProducer;

    @Override
    public void run() {
        logger.info("start local message dispatcher thread");
        try {
            localMessageProducer = new LocalMessageProducer();
            localMessageProducer.addLocalMessageProducerListener(this);
            localMessageProducer.initialize();

        } catch (JMSException e) {
            logger.error(e.getMessage());
        }
    }

    public void enqueue(String message) {
        synchronized (localMessageProducerStarted) {
            if (localMessageProducerStarted.get()) {
                localMessageProducer.send(message);
            } else {
                messageQueue.add(message);
            }
        }
    }

    @Override
    public void received(LocalMessageProducerEvent event) {
        final ProducerEvent eventSource = event.getSource();

        logger.error("" + eventSource);

        if (ProducerEvent.STARTED.equals(eventSource)) {
            purgeMessageQueue();

        }
    }

    private void purgeMessageQueue() {
        synchronized (localMessageProducerStarted) {
            for (String message : messageQueue) {
                localMessageProducer.send(message);
            }

            localMessageProducerStarted.set(true);
        }
    }
}
