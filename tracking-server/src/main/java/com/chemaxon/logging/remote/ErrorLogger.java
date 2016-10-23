package com.chemaxon.logging.remote;

import com.chemaxon.logging.remote.transfer.ErrorReport;
import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLogger {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogger.class);

    public void log(@Body ErrorReport errorReport) {
        logger.info(errorReport.getClientId());
        logger.info(errorReport.getSessionId());
        logger.info(errorReport.getLoggingEvent().getMessage()+"");
    }
}
