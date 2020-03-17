package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.time.LocalDateTime;

@JsonPropertyOrder(alphabetic = true)
public class CheckoutDataDTO {
    @JsonProperty("paid_at")
    private LocalDateTime paidAt;

    @JsonProperty("checked_out_by")
    private ActionedByDTO checkedOutBy;

    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("links")
    private CheckoutLinksDTO links;

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public ActionedByDTO getCheckedOutBy() {
        return checkedOutBy;
    }

    public void setCheckedOutBy(ActionedByDTO checkedOutBy) {
        this.checkedOutBy = checkedOutBy;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public CheckoutLinksDTO getLinks() {
        return links;
    }

    public void setLinks(CheckoutLinksDTO links) {
        this.links = links;
    }

    public String toString() { return new Gson().toJson(this); }
}
