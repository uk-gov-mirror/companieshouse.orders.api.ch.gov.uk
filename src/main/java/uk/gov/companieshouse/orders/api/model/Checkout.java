package uk.gov.companieshouse.orders.api.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "checkout")
public class Checkout extends AbstractOrder {

    private CheckoutData data = new CheckoutData();

    public CheckoutData getData() {
        return data;
    }

    public void setData(CheckoutData data) {
        this.data = data;
    }

}
