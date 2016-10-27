package com.chemaxon.analytics;

import com.chemaxon.analytics.message.MessageDispatcher;
import com.chemaxon.analytics.transfer.Report;
import com.chemaxon.analytics.transfer.ReportBuilder;
import com.google.common.base.Optional;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.io.File;

public class Analytics implements Runnable, ExceptionListener {

    private static final Logger logger = LoggerFactory.getLogger(Analytics.class);

    private static final int SLEEP_TIME = 3000;

    private final String clientId;
    private final String sessionId;
    private final int reconnectDelayInSeconds;
    private final int maxReconnectAttempts;

    private final MessageDispatcher messageDispatcher;

    private boolean isRunning = true;

    Analytics(String clientId, String sessionId, String localStorageLocation, int reconnectDelayInSeconds, int maxReconnectAttempts) {
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.reconnectDelayInSeconds = reconnectDelayInSeconds;
        this.maxReconnectAttempts = maxReconnectAttempts;
        setLocalStorageLocation(localStorageLocation);

        this.messageDispatcher = new MessageDispatcher();
    }

    private void setLocalStorageLocation(String localStorageLocation) {
        File localStorage = new File(localStorageLocation);

        if(localStorage.exists()) {
            System.setProperty("org.apache.activemq.default.directory.prefix", localStorageLocation);
        } else {
            logger.warn("localStorageLocation location : " + localStorageLocation + " does not exists");
        }
    }

    public static AnalyticsBuilder builder() {
        return new AnalyticsBuilder();
    }

    public void start() {
        final Thread loggerThread = new Thread(this, "Analytics main thread");
        loggerThread.setDaemon(false);
        loggerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                loggerThread.interrupt();
            }
        });

    }

    public void run() {
        Optional<Connection> localConnection = Optional.absent();
        Optional<Connection> remoteConnection = Optional.absent();

        try {
            localConnection = configureAndStartConnection(MQSettings.getLocalBrokerUri());
            remoteConnection = configureAndStartConnection(MQSettings.getRemoteBrokerUri(reconnectDelayInSeconds,maxReconnectAttempts));

            final Optional<MessageConsumer> errorConsumer = getLocalConsumer(localConnection);
            final Optional<MessageProducer> remoteProducer = getRemoteProducer(remoteConnection);

            if (localConnection.isPresent()) {
                final Thread messageDispatcherThread = new Thread(messageDispatcher, "Local message dispatcher thread");
                messageDispatcherThread.start();
                logger.info("loop started, wait for a message");

                while (isRunning && !Thread.interrupted()) {
                    if (errorConsumer.isPresent() && remoteProducer.isPresent()) {
                        final MessageConsumer messageConsumer = errorConsumer.get();
                        final Message message = messageConsumer.receive();

                        if (remoteProducer.isPresent()) {
                            final MessageProducer producer = remoteProducer.get();
                            produceMessage(message, producer);
                        }

                    } else {
                        Thread.sleep(SLEEP_TIME);
                        logger.info("errorConsumer or remoteProducer is not present");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            // we have to enqueue the failed message again in order to resend it when the client try to reconnect
        } finally {
            if (localConnection.isPresent()) {
                try {
                    localConnection.get().close();
                } catch (JMSException e) {
                    logger.error(e.getMessage());
                }
            }
            if (remoteConnection.isPresent()) {
                try {
                    remoteConnection.get().close();
                } catch (JMSException e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("finally");
        }
    }

    private void produceMessage(Message message, MessageProducer producer) throws JMSException {
        if (message instanceof ActiveMQTextMessage) {
            final ActiveMQTextMessage originalmessage = (ActiveMQTextMessage) message;
            ActiveMQObjectMessage newMessage = cloneOriginalMessage(originalmessage);

            producer.send(newMessage);
        } else {
            logger.debug("message is not instance of ActiveMQTextMessage");
        }
    }

    private ActiveMQObjectMessage cloneOriginalMessage(ActiveMQTextMessage originalMessage) throws JMSException {
        final String message = originalMessage.getText();
        final Report report = getReport(message);

        ActiveMQObjectMessage newMessage = new ActiveMQObjectMessage();
        newMessage.setObject(report);

        return newMessage;
    }

    private Report getReport(String message) {
        final ReportBuilder reportBuilder = Report.builder()
                .setClientId(clientId)
                .setSessionId(sessionId)
                .setMessage(message);

        return reportBuilder.createShare();
    }

    private Optional<Connection> configureAndStartConnection(String localBroker) {
        try {
            final ActiveMQConnectionFactory connectionFactoryLocal = new ActiveMQConnectionFactory(localBroker);
            final Connection localConnection = connectionFactoryLocal.createConnection();
            localConnection.setExceptionListener(this);
            localConnection.start();

            return Optional.of(localConnection);
        } catch (JMSException e) {
            logger.error(e.getMessage());
        }

        return Optional.absent();
    }

    private Optional<MessageConsumer> getLocalConsumer(Optional<Connection> localConnection) throws JMSException {
        try {
            if (localConnection.isPresent()) {
                final Connection connection = localConnection.get();
                final Session localSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                // JMS appender is using Topic
                final Queue localErrorQueue = localSession.createQueue(MQSettings.LOCAL_ERRORS);

                return Optional.of(localSession.createConsumer(localErrorQueue));
            }
        } catch (JMSException e) {
            logger.error(e.getMessage());
        }

        return Optional.absent();
    }

    private Optional<MessageProducer> getRemoteProducer(Optional<Connection> remoteConnection) {
        try {
            if (remoteConnection.isPresent()) {
                final Connection connection = remoteConnection.get();
                Session remoteSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination remoteDestination = remoteSession.createQueue(MQSettings.REMOTE_ERRORS);

                final MessageProducer producer = remoteSession.createProducer(remoteDestination);
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);

                return Optional.of(producer);
            }

        } catch (JMSException e) {
            logger.error(e.getMessage());
        }
        return Optional.absent();
    }

    public synchronized void onException(JMSException exception) {
        logger.error("JMS Exception occured.  Shutting down client." + exception.getMessage());
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void enqueue(String message) {
        messageDispatcher.enqueue(message);
    }

}
