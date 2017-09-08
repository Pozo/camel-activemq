package com.github.pozo.analytics;

import com.github.pozo.analytics.transfer.Message;
import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ReportDispatcher.class);

    // this method runs concurrently!
    public void dispatch(@Body Message<String> message) {
        logger.info(message.getClientId());
        logger.info(message.getSessionId());
        logger.info(message.getMessage());
    }
}
