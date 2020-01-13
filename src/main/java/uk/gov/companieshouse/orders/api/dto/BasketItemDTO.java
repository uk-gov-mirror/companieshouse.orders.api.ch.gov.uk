package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.ItemOptions;

import javax.validation.constraints.NotNull;

@JsonPropertyOrder(alphabetic = true)
public class BasketItemDTO {

    @NotNull
    @JsonProperty("item_options")
    private ItemOptions itemOptions;

    @NotNull
    @JsonProperty("company_number")
    private String companyNumber;

    public ItemOptions getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(ItemOptions itemOptions) {
        this.itemOptions = itemOptions;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
