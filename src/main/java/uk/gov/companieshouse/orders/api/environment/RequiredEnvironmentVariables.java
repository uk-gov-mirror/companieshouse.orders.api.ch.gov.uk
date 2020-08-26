package uk.gov.companieshouse.orders.api.environment;

public enum RequiredEnvironmentVariables {
    ORDERS_DATABASE("ORDERS_DATABASE"),
    MONGODB_URL("MONGODB_URL");

    private String name;

    RequiredEnvironmentVariables(String name) { this.name = name; }

    public String getName() { return this.name; }
}
