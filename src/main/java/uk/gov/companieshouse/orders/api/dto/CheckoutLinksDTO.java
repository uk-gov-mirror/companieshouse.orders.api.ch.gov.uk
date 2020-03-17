package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.AbstractLinks;

public class CheckoutLinksDTO extends AbstractLinks {

    @JsonProperty("payment")
    private String payment;

    @JsonProperty("resource")
    private String resource;

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
