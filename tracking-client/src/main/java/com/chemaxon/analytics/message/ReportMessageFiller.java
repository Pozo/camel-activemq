package com.chemaxon.analytics.message;

import com.chemaxon.analytics.transfer.Report;
import com.chemaxon.analytics.transfer.ReportBuilder;

public class ReportMessageFiller implements MessageFiller<String, Report> {
    private final String clientId;
    private final String sessionId;

    public ReportMessageFiller(String clientId, String sessionId) {
        this.clientId = clientId;
        this.sessionId = sessionId;
    }
    @Override
    public Report fillMessage(String message) {
        final ReportBuilder reportBuilder = Report.builder()
                .setClientId(clientId)
                .setSessionId(sessionId)
                .setMessage(message);

        return reportBuilder.createShare();
    }
}
