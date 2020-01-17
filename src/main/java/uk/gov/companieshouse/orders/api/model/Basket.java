package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "basket")
public class Basket {
    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BasketData data = new BasketData();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BasketData getData() {
        return data;
    }

    public void setData(BasketData data) {
        this.data = data;
    }

    public void setItems(List<Item> items) {
        data.setItems(items);
    }

    public List<Item> getItems() {
        return data.getItems();
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
