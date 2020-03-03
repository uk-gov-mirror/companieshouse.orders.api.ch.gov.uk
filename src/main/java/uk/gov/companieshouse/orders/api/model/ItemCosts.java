package uk.gov.companieshouse.orders.api.model;

public class ItemCosts {

    private String discountApplied;

    private String individualItemCost;

    private String postageCost;

    private String totalCost;

    public ItemCosts() {
    }

    public ItemCosts(String discountApplied, String individualItemCost, String postageCost, String totalCost) {
        this.discountApplied = discountApplied;
        this.individualItemCost = individualItemCost;
        this.postageCost = postageCost;
        this.totalCost = totalCost;
    }

    public String getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(String discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getIndividualItemCost() {
        return individualItemCost;
    }

    public void setIndividualItemCost(String individualItemCost) {
        this.individualItemCost = individualItemCost;
    }

    public String getPostageCost() {
        return postageCost;
    }

    public void setPostageCost(String postageCost) {
        this.postageCost = postageCost;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }
}
