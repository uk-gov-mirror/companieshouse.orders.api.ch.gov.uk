package uk.gov.companieshouse.orders.api.model;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class CertifiedCopyItemOptions extends ItemOptions {

    private List<FilingHistoryDocument> filingHistoryDocuments;

    public List<FilingHistoryDocument> getFilingHistoryDocuments() {
        return filingHistoryDocuments;
    }

    public void setFilingHistoryDocuments(List<FilingHistoryDocument> filingHistoryDocuments) {
        this.filingHistoryDocuments = filingHistoryDocuments;
    }
}
