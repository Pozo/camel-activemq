package com.chemaxon.logging.remote.transfer;

import org.apache.log4j.spi.LoggingEvent;

public class ErrorReportBuilder {
    private LoggingEvent loggingEvent;
    private String clientId;
    private String sessionId;

    public ErrorReportBuilder setLoggingEvent(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
        return this;
    }

    public ErrorReportBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ErrorReportBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ErrorReport createShare() {
        return new ErrorReport(loggingEvent, clientId, sessionId);
    }
}