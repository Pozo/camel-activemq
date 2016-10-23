package com.chemaxon.logging.remote;

import com.chemaxon.logging.remote.transfer.ErrorReport;
import com.chemaxon.logging.remote.transfer.ErrorReportBuilder;
import com.google.common.base.Optional;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.net.PersistentJMSAppender;
import org.apache.log4j.spi.LoggingEvent;
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

public class RemoteLogger implements Runnable, ExceptionListener {
    private static final String JMS_APPENDER_NAME = "jms";

    private static final Logger localLogger = LoggerFactory.getLogger(RemoteLogger.class);

    private static final String REMOTE_LOGGER_NAME = "remote";
    public static final Logger remoteLogger = LoggerFactory.getLogger(REMOTE_LOGGER_NAME);

    private static final int SLEEP_TIME = 3000;


    private final String clientId;
    private final String sessionId;

    private boolean isRunning = true;

    private RemoteLogger(String clientId, String sessionId) {
        this.clientId = clientId;
        this.sessionId = sessionId;
    }

    public static Thread start(String clientId, String sessionId) {
        final RemoteLogger remoteLogger = new RemoteLogger(clientId,sessionId);

        Thread loggerThread = new Thread(remoteLogger, "RemoteLogger main thread");
        loggerThread.setDaemon(false);
        loggerThread.start();

        return loggerThread;
    }

    public void run() {
        Optional<Connection> localConnection = Optional.absent();
        Optional<Connection> remoteConnection = Optional.absent();

        try {
            localConnection = configureAndStartConnection(MQSettings.LOCAL_BROKER_URI);
            remoteConnection = configureAndStartConnection(MQSettings.REMOTE_BROKER_URI);

            final Optional<MessageConsumer> errorConsumer = getLocalConsumer(localConnection);
            final Optional<MessageProducer> remoteProducer = getRemoteProducer(remoteConnection);

            if (localConnection.isPresent()) {
                setupJMSAppenderForLogger(); // we had to wait until active-mq started

                localLogger.info("loop started, wait for a message");

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
                        localLogger.info("errorConsumer or remoteProducer is not present");
                    }
                }
            }
        } catch (Exception e) {
            localLogger.error(e.getMessage());
        } finally {
            if (localConnection.isPresent()) {
                try {
                    localConnection.get().close();
                } catch (JMSException e) {
                    localLogger.error(e.getMessage());
                }
            }
            if (remoteConnection.isPresent()) {
                try {
                    remoteConnection.get().close();
                } catch (JMSException e) {
                    localLogger.error(e.getMessage());
                }
            }
            localLogger.info("finally");
        }

    }

    private void produceMessage(Message message, MessageProducer producer) throws JMSException {
        if (message instanceof ActiveMQObjectMessage) {
            final ActiveMQObjectMessage originalmessage = (ActiveMQObjectMessage) message;
            ActiveMQObjectMessage newMessage = copyOriginalMessageAndInitialize(originalmessage);

            producer.send(newMessage);
        } else {
            localLogger.debug("message is not instance of ActiveMQObjectMessage");
        }
    }

    private ActiveMQObjectMessage copyOriginalMessageAndInitialize(ActiveMQObjectMessage originalMessage) throws JMSException {
        final LoggingEvent loggingEvent = (LoggingEvent) originalMessage.getObject();
        final ErrorReport errorReport = getErrorReport(loggingEvent);

        ActiveMQObjectMessage newMessage = new ActiveMQObjectMessage();
        newMessage.setObject(errorReport);

        return newMessage;
    }

    private ErrorReport getErrorReport(LoggingEvent loggingEvent) {
        final ErrorReportBuilder errorReportBuilder = new ErrorReportBuilder()
                .setClientId(clientId)
                .setSessionId(sessionId)
                .setLoggingEvent(loggingEvent);

        return errorReportBuilder.createShare();
    }

    private Optional<Connection> configureAndStartConnection(String localBroker) {
        try {
            final ActiveMQConnectionFactory connectionFactoryLocal = new ActiveMQConnectionFactory(localBroker);
            final Connection localConnection = connectionFactoryLocal.createConnection();
            localConnection.setExceptionListener(this);
            localConnection.start();

            return Optional.of(localConnection);
        } catch (JMSException e) {
            localLogger.error(e.getMessage());
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
            localLogger.error(e.getMessage());
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
            localLogger.error(e.getMessage());
        }
        return Optional.absent();
    }

    private void setupJMSAppenderForLogger() throws JMSException {
        PersistentJMSAppender jmsAppender = new PersistentJMSAppender(MQSettings.LOCAL_BROKER_URI, MQSettings.LOCAL_ERRORS);
        jmsAppender.activateOptions();
        jmsAppender.setName(JMS_APPENDER_NAME);
        LogManager.getLogger(REMOTE_LOGGER_NAME).addAppender(jmsAppender);
    }

    public synchronized void onException(JMSException exception) {
        localLogger.error("JMS Exception occured.  Shutting down client." + exception.getMessage());
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
