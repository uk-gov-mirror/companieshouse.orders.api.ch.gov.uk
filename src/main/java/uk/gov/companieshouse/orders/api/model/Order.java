package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order")
public class Order extends AbstractOrder {

    private OrderData data = new OrderData();

    public OrderData getData() {
        return data;
    }

    public void setData(OrderData data) {
        this.data = data;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
