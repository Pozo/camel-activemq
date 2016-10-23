package com.chemaxon.logging.remote.transfer;

import org.apache.log4j.spi.LoggingEvent;

import java.io.Serializable;

public class ErrorReport implements Serializable {
    private final LoggingEvent loggingEvent;
    private final String clientId;
    private final String sessionId;

    ErrorReport(LoggingEvent loggingEvent, String clientId, String sessionId) {
        this.loggingEvent = loggingEvent;
        this.clientId = clientId;
        this.sessionId = sessionId;
    }

    public LoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "ErrorReport{" +
                "loggingEvent=" + loggingEvent +
                ", clientId='" + clientId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
