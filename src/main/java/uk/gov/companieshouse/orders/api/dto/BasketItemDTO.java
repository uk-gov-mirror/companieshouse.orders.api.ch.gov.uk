package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.ItemLinks;
import uk.gov.companieshouse.orders.api.model.ItemOptions;

import java.util.List;
import java.util.Map;

public class BasketItemDTO extends ItemDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("description")
    private String description;

    @JsonProperty("description_identifier")
    private String descriptionIdentifier;

    @JsonProperty("description_values")
    private Map<String, String> descriptionValues;

    @JsonProperty("item_costs")
    private List<ItemCosts> itemCosts;

    @JsonProperty("item_options")
    private ItemOptions itemOptions;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("links")
    private ItemLinks links;

    @JsonProperty("postal_delivery")
    private Boolean isPostalDelivery;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("item_uri")
    private String itemUri;

    @JsonProperty("postage_cost")
    private String postageCost;

    @JsonProperty("total_item_cost")
    private String totalItemCost;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

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

    public ItemLinks getLinks() {
        return links;
    }

    public void setLinks(ItemLinks links) {
        this.links = links;
    }

    public Boolean getPostalDelivery() {
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

    public String getItemUri() {
        return itemUri;
    }

    public void setItemUri(String itemUri) {
        this.itemUri = itemUri;
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
