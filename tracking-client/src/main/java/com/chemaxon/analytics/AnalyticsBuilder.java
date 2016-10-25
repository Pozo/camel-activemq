package com.chemaxon.analytics;

public class AnalyticsBuilder {
    private String clientId;
    private String sessionId;
    private String localStorageFolder;

    public AnalyticsBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AnalyticsBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Analytics build() {
        return new Analytics(clientId, sessionId,localStorageFolder);
    }

    public AnalyticsBuilder setLocalStorageFolder(String localStorageFolder) {
        this.localStorageFolder = localStorageFolder;
        return this;
    }
}