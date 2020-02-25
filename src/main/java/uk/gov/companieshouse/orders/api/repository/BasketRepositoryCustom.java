package uk.gov.companieshouse.orders.api.repository;

import uk.gov.companieshouse.orders.api.model.Basket;

public interface BasketRepositoryCustom {
    Basket clearBasketDataById(String id);
}
