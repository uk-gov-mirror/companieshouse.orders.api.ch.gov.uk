package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

public class Links {
    
    private String self;

    private String payment;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
