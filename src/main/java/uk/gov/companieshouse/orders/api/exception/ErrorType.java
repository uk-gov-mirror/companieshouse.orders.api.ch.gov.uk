package uk.gov.companieshouse.orders.api.exception;

public enum ErrorType {
    BASKET_ITEMS_MISSING("Basket is empty"),
    BASKET_ITEM_INVALID("Failed to retrieve item");

    public String value;

    private ErrorType(String value){
        this.value = value;
    }
}
