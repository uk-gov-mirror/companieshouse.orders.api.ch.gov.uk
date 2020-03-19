package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;

@Mapper(componentModel = "spring")
public interface CheckoutToOrderMapper {

    @Mapping(source = "data.checkedOutBy", target = "data.orderedBy")
    @Mapping(target = "data.orderedAt", ignore = true)
    Order checkoutToOrder(Checkout checkout);

}
