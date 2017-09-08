package com.github.pozo.analytics;

import com.github.pozo.analytics.message.MessageDispatcher;
import com.github.pozo.analytics.message.ReportMessageFiller;
import com.github.pozo.analytics.transfer.Report;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class Analytics implements Runnable, ExceptionListener {

    private static final Logger logger = LoggerFactory.getLogger(Analytics.class);

    private static final int SLEEP_TIME = 3000;

    private final MQSettings mqSettings;

    private final MessageDispatcher messageDispatcher;
    private final ReportMessageFiller reportMessageFiller;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    Analytics(MQSettings mqSettings, ReportMessageFiller reportMessageFiller, String localStorageLocation) {
        this.mqSettings = mqSettings;
        this.reportMessageFiller = reportMessageFiller;

        setLocalStorageLocation(localStorageLocation);

        this.messageDispatcher = new MessageDispatcher(mqSettings);
    }

    public static AnalyticsBuilder builderFor(String hostName, int portNumber) {
        return new AnalyticsBuilder(hostName, portNumber);
    }

    private void setLocalStorageLocation(String localStorageLocation) {
        File localStorage = new File(localStorageLocation);

        if (localStorage.exists()) {
            System.setProperty("org.apache.activemq.default.directory.prefix", localStorageLocation);
        } else {
            logger.warn("localStorageLocation location : " + localStorageLocation + " does not exists");
        }
    }

    public void start() {
        final Thread loggerThread = new Thread(this, "Analytics main thread");
        loggerThread.setDaemon(false);
        loggerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                setRunning(false);
            }
        });

    }

    public void run() {
        Optional<Connection> localConnection = Optional.absent();
        Optional<Connection> remoteConnection = Optional.absent();

        try {
            localConnection = configureAndStartConnection(mqSettings.getLocalBrokerUri());
            if (localConnection.isPresent()) {
                final Thread messageDispatcherThread = new Thread(messageDispatcher, "Local message dispatcher thread");
                messageDispatcherThread.start();
                logger.info("Local message dispatcher has started");
            }

            remoteConnection = configureAndStartConnection(mqSettings.getRemoteBrokerUri());

            final Optional<MessageConsumer> errorConsumer = getLocalConsumer(localConnection);
            final Optional<MessageProducer> remoteProducer = getRemoteProducer(remoteConnection);
            logger.info("remoteProducer is present " + localConnection.isPresent());

            if (localConnection.isPresent()) {
                logger.info("loop started, wait for a message");

                while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
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
            e.printStackTrace();
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
        final Report report = reportMessageFiller.fillMessage(message);

        ActiveMQObjectMessage newMessage = new ActiveMQObjectMessage();
        newMessage.setObject(report);

        return newMessage;
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
        isRunning.set(running);
    }

    public void enqueue(String message) {
        messageDispatcher.enqueue(message);
    }

}
