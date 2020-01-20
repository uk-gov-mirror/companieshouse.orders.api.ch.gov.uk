package uk.gov.companieshouse.orders.api.dto;

import com.google.gson.Gson;

public class AddToBasketResponseDTO {

    private String itemUri;

    public String getItemUri() {
        return itemUri;
    }

    public void setItemUri(String itemUri) {
        this.itemUri = itemUri;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
