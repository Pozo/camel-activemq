package com.github.pozo.analytics.message;

import com.github.pozo.analytics.transfer.Report;
import com.github.pozo.analytics.transfer.ReportBuilder;

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
