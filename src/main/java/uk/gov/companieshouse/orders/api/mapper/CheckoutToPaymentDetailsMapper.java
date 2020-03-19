package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckoutToPaymentDetailsMapper {

    static final String CLASS_OF_PAYMENT_ORDERABLE_ITEM = "orderable-item";
    static final String AVAILABLE_PAYMENT_METHOD_CREDIT_CARD = "credit-card";
    static final String ITEM_RESOURCE_TYPE = "cost#cost";
    static final String CHECKOUT_RESOURCE_TYPE = "payment-details#payment-details";

    @Mapping(source = "data.paidAt", target = "paidAt")
    @Mapping(source = "data.reference", target = "paymentReference")
    @Mapping(source = "data.items", target = "items")
    @Mapping(source = "data.status", target = "status")
    @Mapping(source = "data.links.payment", target = "links.self")
    @Mapping(source = "data.links.self", target = "links.resource")
    @Mapping(source = "data.etag", target = "etag")
    PaymentDetailsDTO checkoutToPaymentDetailsMapper(Checkout checkout);

    @AfterMapping
    default void updateDTOWithPaymentDetails(CheckoutData checkoutData, @MappingTarget PaymentDetailsDTO paymentDetailsDTO) {
        List<ItemDTO> itemDTOs = paymentDetailsDTO.getItems();
        for (ItemDTO itemDTO : itemDTOs) {
            List<String> classOfPayment = new ArrayList<>();
            classOfPayment.add(CLASS_OF_PAYMENT_ORDERABLE_ITEM);
            itemDTO.setClassOfPayment(classOfPayment);

            List<String> availablePaymentMethods = new ArrayList<>();
            availablePaymentMethods.add(AVAILABLE_PAYMENT_METHOD_CREDIT_CARD);
            itemDTO.setAvailablePaymentMethods(availablePaymentMethods);

            itemDTO.setResourceKind(itemDTO.getKind());
            itemDTO.setKind(ITEM_RESOURCE_TYPE);
        }

        paymentDetailsDTO.setKind(CHECKOUT_RESOURCE_TYPE);
    }
}
