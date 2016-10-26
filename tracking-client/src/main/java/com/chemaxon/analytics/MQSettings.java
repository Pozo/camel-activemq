package com.chemaxon.analytics;

public class MQSettings {
    public static final String LOCAL_BROKER_URI = "vm://test?broker.persistent=true";
    public static final String REMOTE_BROKER_URI = "nio://localhost:61616";

    public static final String LOCAL_ERRORS = "LOCAL.ERRORS";
    public static final String LOCAL_USAGE = "LOCAL.USAGE";

    public static final String REMOTE_ERRORS = "REMOTE.ERRORS";
}
