package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ItemDTO {

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("available_payment_methods")
    private List<String> availablePaymentMethods;

    @JsonProperty("description")
    private String description;

    @JsonProperty("description_identifier")
    private String descriptionIdentifier;

    @JsonProperty("description_values")
    private Map<String, String> descriptionValues;

    @JsonProperty("class_of_payment")
    private List<String> classOfPayment;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty("resource_kind")
    private String resourceKind;

    public String getDescription() {
        return description;
    }

    public String getDescriptionIdentifier() {
        return descriptionIdentifier;
    }

    public void setDescriptionIdentifier(String descriptionIdentifier) {
        this.descriptionIdentifier = descriptionIdentifier;
    }

    public Map<String, String> getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(Map<String, String> descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<String> getAvailablePaymentMethods() {
        return availablePaymentMethods;
    }

    public void setAvailablePaymentMethods(List<String> availablePaymentMethods) {
        this.availablePaymentMethods = availablePaymentMethods;
    }

    public List<String> getClassOfPayment() {
        return classOfPayment;
    }

    public void setClassOfPayment(List<String> classOfPayment) {
        this.classOfPayment = classOfPayment;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(String resourceKind) {
        this.resourceKind = resourceKind;
    }
}
