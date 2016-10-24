package com.chemaxon.logging.remote;

public class AnalyticsBuilder {
    private String clientId;
    private String sessionId;

    public AnalyticsBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AnalyticsBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Analytics build() {
        return new Analytics(clientId, sessionId);
    }
}