package uk.gov.companieshouse.orders.api.model;

public class PaymentLinks extends AbstractLinks {
    private String resource;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
