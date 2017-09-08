package com.github.pozo.analytics;

import com.github.pozo.analytics.message.ReportMessageFiller;

public class AnalyticsBuilder {
    private final String serverHostName;
    private final int serverPortNumber;

    private String clientId;
    private String sessionId;
    private String localStorageFolder;
    private int reconnectDelayInSeconds;
    private int maxReconnectAttempts;

    AnalyticsBuilder(String serverHostName, int serverPortNumber) {
        if (serverHostName == null) {
            throw new IllegalArgumentException("serverHostName cant be null");
        }
        if (serverPortNumber == 0) {
            throw new IllegalArgumentException("serverPortNumber cant be null");
        }
        this.serverHostName = serverHostName;
        this.serverPortNumber = serverPortNumber;
    }

    public AnalyticsBuilder setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AnalyticsBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Analytics build() {
        final MQSettings mqSettings = new MQSettings(serverHostName, serverPortNumber);
        mqSettings.setMaxReconnectAttempts(maxReconnectAttempts);
        mqSettings.setReconnectDelayInSeconds(reconnectDelayInSeconds);

        ReportMessageFiller reportMessageFiller = new ReportMessageFiller(clientId, sessionId);

        return new Analytics(mqSettings, reportMessageFiller, localStorageFolder);
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