package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AddDeliveryDetailsRequestDTO {
    @Valid
    @NotNull
    @JsonProperty("delivery_details")
    DeliveryDetailsDTO deliveryDetails;

    public DeliveryDetailsDTO getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(DeliveryDetailsDTO deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }
}
