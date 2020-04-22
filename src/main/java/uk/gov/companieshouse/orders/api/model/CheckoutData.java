package uk.gov.companieshouse.orders.api.model;

import java.util.Date;

public class CheckoutData extends AbstractOrderData {

    private Date paidAt;

    private ActionedBy checkedOutBy;

    private PaymentStatus status;

    private CheckoutLinks links;

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
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
