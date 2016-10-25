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
    private final List<LocalMessageProducerListener> listeners = new ArrayList<LocalMessageProducerListener>();

    private Session session;
    private MessageProducer producer;

    public void initialize() throws JMSException {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MQSettings.LOCAL_BROKER_URI);

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

    public synchronized void addLocalMessageProducerListener(LocalMessageProducerListener localMessageProducerListener) {
        listeners.add(localMessageProducerListener);
    }

    private synchronized void fireEvent(ProducerEvent event) {
        for (LocalMessageProducerListener listener : listeners) {
            listener.received(new LocalMessageProducerEvent(event));
        }
    }
}
