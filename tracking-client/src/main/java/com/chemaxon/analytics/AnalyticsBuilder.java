package com.chemaxon.analytics;

public class AnalyticsBuilder {
    private String clientId;
    private String sessionId;
    private String localStorageFolder;
    private int reconnectDelayInSeconds;
    private int maxReconnectAttempts;

    public AnalyticsBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AnalyticsBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Analytics build() {
        return new Analytics(clientId, sessionId, localStorageFolder,reconnectDelayInSeconds,maxReconnectAttempts);
    }

    public AnalyticsBuilder setLocalStorageFolder(String localStorageFolder) {
        this.localStorageFolder = localStorageFolder;
        return this;
    }

    public AnalyticsBuilder setReconnectDelayInSeconds(int reconnectDelayInSeconds) {
        this.reconnectDelayInSeconds = reconnectDelayInSeconds;
        return this;
    }

    public AnalyticsBuilder setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
        return this;
    }
}