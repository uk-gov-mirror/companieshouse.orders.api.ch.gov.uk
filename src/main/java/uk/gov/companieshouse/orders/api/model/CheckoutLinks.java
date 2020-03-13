package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

public class CheckoutLinks extends AbstractLinks {

    private String payment;

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
