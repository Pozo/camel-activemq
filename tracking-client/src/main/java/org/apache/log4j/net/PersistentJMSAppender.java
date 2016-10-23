package org.apache.log4j.net;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class PersistentJMSAppender extends AppenderSkeleton {
    private final Logger logger = LoggerFactory.getLogger(PersistentJMSAppender.class);

    private final Session session;
    private final MessageProducer producer;

    public PersistentJMSAppender(String brokerUri, String queueName) throws JMSException {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUri);

        // Create a Connection
        Connection localConnection = connectionFactory.createConnection();
        localConnection.start();

        // Create a Session
        session = localConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the localDestination (Topic or Queue)
        Destination localDestination = session.createQueue(queueName);

        // Create a ClientMessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(localDestination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

    }

    protected void append(LoggingEvent event) {
        try {
            ObjectMessage message = session.createObjectMessage(event);
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        logger.info("closed");
    }

    public boolean requiresLayout() {
        return false;
    }
}
