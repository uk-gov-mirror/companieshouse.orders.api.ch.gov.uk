package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;

public class OrderData extends AbstractOrderData {

    private LocalDateTime orderedAt;

    private ActionedBy orderedBy;

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
