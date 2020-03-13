package uk.gov.companieshouse.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

import static uk.gov.companieshouse.orders.api.converter.EnumValueNameConverter.convertEnumValueNameToJson;

public enum ItemStatus {
    UNKNOWN,
    PROCESSING,
    SATISFIED;

    @JsonValue
    public String getJsonName() {
        return convertEnumValueNameToJson(this);
    }
}
