package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderData {
    private String paymentReference;

    private String etag;

    private DeliveryDetails deliveryDetails = new DeliveryDetails();

    private List<Item> items = new ArrayList<>();

    private String kind;

    private Links links;

    private String totalBasketCost;

    private PaymentStatus status;

    private String reference;

    private LocalDateTime orderedAt;

    private ActionedBy orderedBy;

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getTotalBasketCost() {
        return totalBasketCost;
    }

    public void setTotalBasketCost(String totalBasketCost) {
        this.totalBasketCost = totalBasketCost;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public ActionedBy getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(ActionedBy orderedBy) {
        this.orderedBy = orderedBy;
    }
}
