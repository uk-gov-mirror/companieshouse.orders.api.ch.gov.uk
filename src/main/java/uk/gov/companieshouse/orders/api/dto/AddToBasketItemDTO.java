package uk.gov.companieshouse.orders.api.dto;

import com.google.gson.Gson;

import javax.validation.constraints.NotNull;

public class AddToBasketItemDTO {

    @NotNull
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
