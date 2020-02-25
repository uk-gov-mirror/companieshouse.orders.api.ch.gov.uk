package uk.gov.companieshouse.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

import static uk.gov.companieshouse.orders.api.converter.EnumValueNameConverter.convertEnumValueNameToJson;

public enum PaymentStatus {
    PAID,
    FAILED,
    PENDING,
    EXPIRED,
    IN_PROGRESS,
    NO_FUNDS;

    @JsonValue
    public String getJsonName() {
        return convertEnumValueNameToJson(this);
    }
}
