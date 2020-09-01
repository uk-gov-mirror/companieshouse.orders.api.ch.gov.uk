package uk.gov.companieshouse.orders.api.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = CertificateItemOptions.class, name = "CertificateItemOptions"),
//        @JsonSubTypes.Type(value = CertifiedCopyItemOptions.class, name = "CertifiedCopyItemOptions")
//})
public class Item {

    @Field("id")
    private String id;

    private String companyName;

    private String companyNumber;

    private String customerReference;

    private String description;

    private String descriptionIdentifier;

    private Map<String, String> descriptionValues;

    private List<ItemCosts> itemCosts;

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME)
    private ItemOptions itemOptions;

    private String etag;

    private String kind;

    private ItemLinks links;

    private Boolean isPostalDelivery;

    private Integer quantity;

    private String itemUri;

    private LocalDateTime satisfiedAt;

    private ItemStatus status;

    private String postageCost;

    private String totalItemCost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<ItemCosts> getItemCosts() {
        return itemCosts;
    }

    public void setItemCosts(List<ItemCosts> itemCosts) {
        this.itemCosts = itemCosts;
    }

    public ItemOptions getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(ItemOptions itemOptions) {
        this.itemOptions = itemOptions;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Boolean isPostalDelivery() {
        return isPostalDelivery;
    }

    public void setPostalDelivery(Boolean postalDelivery) {
        isPostalDelivery = postalDelivery;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ItemLinks getLinks() {
        return links;
    }

    public void setLinks(ItemLinks links) {
        this.links = links;
    }

    public String getItemUri() {
        return itemUri;
    }

    public void setItemUri(String itemUri) {
        this.itemUri = itemUri;
    }

    public LocalDateTime getSatisfiedAt() {
        return satisfiedAt;
    }

    public void setSatisfiedAt(LocalDateTime satisfiedAt) {
        this.satisfiedAt = satisfiedAt;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getPostageCost() {
        return postageCost;
    }

    public void setPostageCost(String postageCost) {
        this.postageCost = postageCost;
    }

    public String getTotalItemCost() {
        return totalItemCost;
    }

    public void setTotalItemCost(String totalItemCost) {
        this.totalItemCost = totalItemCost;
    }
}
