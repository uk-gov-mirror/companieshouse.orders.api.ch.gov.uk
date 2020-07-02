package uk.gov.companieshouse.orders.api.exception;

public enum ErrorType {
    BASKET_ITEMS_MISSING("Basket is empty"),
    BASKET_ITEM_INVALID("Failed to retrieve item"),
    DELIVERY_DETAILS_MISSING("Delivery details missing for postal delivery");

    private String value;

    private ErrorType(String value){
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
