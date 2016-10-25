package com.chemaxon.analytics;

import com.chemaxon.analytics.transfer.Report;
import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ReportDispatcher.class);

    public void dispatch(@Body Report report) {
        logger.info(report.getClientId());
        logger.info(report.getSessionId());
        logger.info(report.getMessage());
    }
}
