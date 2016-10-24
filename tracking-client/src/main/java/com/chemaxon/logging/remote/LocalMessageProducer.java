package com.chemaxon.logging.remote;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.spi.LoggingEvent;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class LocalMessageProducer {

    private final Session session;
    private final MessageProducer producer;

    public LocalMessageProducer() throws JMSException {
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
    }

    protected void send(LoggingEvent event) {
        try {
            ObjectMessage message = session.createObjectMessage(event);
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
