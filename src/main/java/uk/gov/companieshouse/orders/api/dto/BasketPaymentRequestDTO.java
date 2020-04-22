package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.util.Date;

public class BasketPaymentRequestDTO {

    @JsonProperty("paid_at")
    private Date paidAt;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private PaymentStatus status;

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

    @Override
    public String toString() { return new Gson().toJson(this); }

}
