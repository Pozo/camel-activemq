package com.chemaxon.analytics;

public class MQSettings {
    private static final String LOCAL_BROKER_URI = "vm://test?broker.persistent=true";
    private static final String REMOTE_BROKER_URI = "failover:(nio://localhost:61616)";

    public static final String LOCAL_ERRORS = "LOCAL.ERRORS";
    public static final String LOCAL_USAGE = "LOCAL.USAGE";

    public static final String REMOTE_ERRORS = "REMOTE.ERRORS";

    public static String getRemoteBrokerUri(int reconnectDelayInSeconds, int maxReconnectAttempts) {
        final StringBuilder remoteUri = new StringBuilder(REMOTE_BROKER_URI);
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

    public static String getLocalBrokerUri() {
        return LOCAL_BROKER_URI;
    }
}
