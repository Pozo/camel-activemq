package com.chemaxon.analytics.message;

import com.chemaxon.analytics.MQSettings;
import com.chemaxon.analytics.ProducerEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class LocalMessageProducer {
    private final List<MessageProducerListener> listeners = new ArrayList<MessageProducerListener>();

    private Session session;
    private MessageProducer producer;

    private final MQSettings mqSettings;

    public LocalMessageProducer(MQSettings mqSettings) {
        this.mqSettings = mqSettings;
    }

    public void initialize() throws JMSException {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mqSettings.getLocalBrokerUri());

        // Create a Connection
        Connection localConnection = connectionFactory.createConnection();
        localConnection.start();

        // Create a Session
        session = localConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the localDestination (Topic or Queue)
        Destination localDestination = session.createQueue(MQSettings.LOCAL_ERRORS);

        // Create a ClientMessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(localDestination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        fireEvent(ProducerEvent.STARTED);
    }

    protected void send(String text) {
        try {
            TextMessage message = session.createTextMessage(text);
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addLocalMessageProducerListener(MessageProducerListener messageProducerListener) {
        listeners.add(messageProducerListener);
    }

    private synchronized void fireEvent(ProducerEvent event) {
        for (MessageProducerListener listener : listeners) {
            listener.received(new MessageProducerEvent(event));
        }
    }
}
