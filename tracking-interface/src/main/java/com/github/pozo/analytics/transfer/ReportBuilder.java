package com.github.pozo.analytics.transfer;

public class ReportBuilder {
    private String message;
    private String clientId;
    private String sessionId;

    ReportBuilder() {

    }

    public ReportBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public ReportBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ReportBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Report createShare() {
        return new Report(message, clientId, sessionId);
    }
}