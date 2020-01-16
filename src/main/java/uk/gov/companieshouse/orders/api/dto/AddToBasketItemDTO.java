package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AddToBasketItemDTO {

    @NotBlank(message = "item_uri cannot be null")
    @JsonProperty("item_uri")
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
