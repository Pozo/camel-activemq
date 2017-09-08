package com.github.pozo.analytics;

public class MQSettings {
    public static final String LOCAL_ERRORS = "LOCAL.ERRORS";
    public static final String LOCAL_USAGE = "LOCAL.USAGE";
    public static final String REMOTE_ERRORS = "REMOTE.ERRORS";
    private static final String LOCAL_BROKER_URI = "vm://test?broker.persistent=true";
    private static final String REMOTE_BROKER_URI_TEMPLATE = "failover:(nio://%s:%d)";
    private final String host;
    private final int port;

    private int reconnectDelayInSeconds;
    private int maxReconnectAttempts;

    MQSettings(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getRemoteBrokerUri() {
        final String remoteHostBase = String.format(REMOTE_BROKER_URI_TEMPLATE, host, port);
        final StringBuilder remoteUri = new StringBuilder(remoteHostBase);

        if (reconnectDelayInSeconds != 0) {
            remoteUri.append("?maxReconnectDelay=");
            final int delayInMiliseconds = reconnectDelayInSeconds * 1000;
            remoteUri.append(String.valueOf(delayInMiliseconds));
        }
        if (maxReconnectAttempts != 0) {
            remoteUri.append("&maxReconnectAttempts=");
            remoteUri.append(String.valueOf(maxReconnectAttempts));
        }
        return remoteUri.toString();
    }

    public String getLocalBrokerUri() {
        return LOCAL_BROKER_URI;
    }

    public void setReconnectDelayInSeconds(int reconnectDelayInSeconds) {
        this.reconnectDelayInSeconds = reconnectDelayInSeconds;
    }

    public void setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }
}
