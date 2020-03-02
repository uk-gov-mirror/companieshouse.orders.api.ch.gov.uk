package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckoutData {
    private String paymentReference;

    private LocalDateTime paidAt;

    private String etag;

    private DeliveryDetails deliveryDetails = new DeliveryDetails();

    private List<Item> items = new ArrayList<>();

    private String kind;

    private Links links;

    private String totalBasketCost;

    private PaymentStatus status;

    private String reference;

    private ActionedBy checkedOutBy;

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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

    public ActionedBy getCheckedOutBy() {
        return checkedOutBy;
    }

    public void setCheckedOutBy(ActionedBy checkedOutBy) {
        this.checkedOutBy = checkedOutBy;
    }
}
