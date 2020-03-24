package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonProperty("items")
    private List<ItemDTO> items = new ArrayList<>();

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("total_order_cost")
    private String totalOrderCost;

    @JsonProperty("reference")
    private String reference;

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

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
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

    public String toString() { return new Gson().toJson(this); }
}
