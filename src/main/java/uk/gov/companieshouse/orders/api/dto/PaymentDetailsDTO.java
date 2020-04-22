package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.orders.api.model.PaymentLinks;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentDetailsDTO {
    @JsonProperty("description")
    private String description;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("items")
    private List<ItemDTO> items = new ArrayList<>();

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("links")
    private PaymentLinks links;

    @JsonProperty("paid_at")
    private Date paidAt;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private PaymentStatus status;

    public String getDescription() {
        return description;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PaymentLinks getLinks() {
        return links;
    }

    public void setLinks(PaymentLinks links) {
        this.links = links;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
