package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class BasketData {
    private DeliveryDetails deliveryDetails;

    private String etag;

    private List<Item> items = new ArrayList<>();

    private String kind;

    private BasketLinks links;

    private String totalBasketCost;


    public DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public BasketLinks getLinks() {
        return links;
    }

    public void setLinks(BasketLinks links) {
        this.links = links;
    }

    public String getTotalBasketCost() {
        return totalBasketCost;
    }

    public void setTotalBasketCost(String totalBasketCost) {
        this.totalBasketCost = totalBasketCost;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
