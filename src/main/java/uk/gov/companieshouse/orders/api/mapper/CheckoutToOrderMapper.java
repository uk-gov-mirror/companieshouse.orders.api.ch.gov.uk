package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;

@Mapper(componentModel = "spring")
public interface CheckoutToOrderMapper {
    Order checkoutToOrder(Checkout checkout);
}
