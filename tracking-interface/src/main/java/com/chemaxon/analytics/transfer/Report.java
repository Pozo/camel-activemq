package com.chemaxon.analytics.transfer;

import java.io.Serializable;

public class Report implements Serializable {
    private final String message;
    private final String clientId;
    private final String sessionId;

    Report(String message, String clientId, String sessionId) {
        this.message = message;
        this.clientId = clientId;
        this.sessionId = sessionId;
    }
    public static ReportBuilder builder() {
        return new ReportBuilder();
    }
    public String getMessage() {
        return message;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "Report{" +
                "message=" + message +
                ", clientId='" + clientId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
