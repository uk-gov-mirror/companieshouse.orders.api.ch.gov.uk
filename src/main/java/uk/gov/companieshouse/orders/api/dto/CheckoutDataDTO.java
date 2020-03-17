package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("delivery_details")
    private DeliveryDetails deliveryDetails = new DeliveryDetails();

    private List<Item> items = new ArrayList<>();

    private String kind;

    private String totalOrderCost;

    private String reference;

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
