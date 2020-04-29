package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;

public class CheckoutData extends AbstractOrderData {

    private LocalDateTime paidAt;

    private ActionedBy checkedOutBy;

    private PaymentStatus status;

    private CheckoutLinks links;

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public ActionedBy getCheckedOutBy() {
        return checkedOutBy;
    }

    public void setCheckedOutBy(ActionedBy checkedOutBy) {
        this.checkedOutBy = checkedOutBy;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public CheckoutLinks getLinks() {
        return links;
    }

    public void setLinks(CheckoutLinks links) {
        this.links = links;
    }
}
