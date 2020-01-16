package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

public class Links {
    
    private String self;


    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
