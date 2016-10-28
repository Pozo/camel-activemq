package com.chemaxon.analytics.message;

import com.chemaxon.analytics.MQSettings;
import com.chemaxon.analytics.ProducerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageDispatcher implements Runnable, MessageProducerListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private final LinkedList<String> messageQueue = new LinkedList<String>();
    private final AtomicBoolean localMessageProducerStarted = new AtomicBoolean(false);
    private LocalMessageProducer localMessageProducer;

    private final MQSettings mqSettings;

    public MessageDispatcher(MQSettings mqSettings) {
        this.mqSettings = mqSettings;
    }

    @Override
    public void run() {
        logger.info("start local message dispatcher thread");
        try {
            localMessageProducer = new LocalMessageProducer(mqSettings);
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
    public void received(MessageProducerEvent event) {
        final ProducerEvent eventSource = event.getSource();

        logger.info("ProducerEvent received : " + eventSource);

        if (ProducerEvent.STARTED.equals(eventSource)) {
            sendEverythingFromTheMessageQueue();
        }
    }

    private void sendEverythingFromTheMessageQueue() {
        synchronized (localMessageProducerStarted) {
            while(messageQueue.size()>0) {
                localMessageProducer.send(messageQueue.removeFirst());
            }

            localMessageProducerStarted.set(true);
        }
    }
}
