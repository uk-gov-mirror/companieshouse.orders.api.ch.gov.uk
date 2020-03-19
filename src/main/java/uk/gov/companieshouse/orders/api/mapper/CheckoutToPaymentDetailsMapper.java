package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.model.*;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckoutToPaymentDetailsMapper {

    static final String CLASS_OF_PAYMENT_ORDERABLE_ITEM = "orderable-item";
    static final String AVAILABLE_PAYMENT_METHOD_CREDIT_CARD = "credit-card";
    static final String AVAILABLE_PAYMENT_METHOD_ACCOUNT = "account";
    static final String ITEM_RESOURCE_TYPE = "cost#cost";
    static final String CHECKOUT_RESOURCE_TYPE = "payment-details#payment-details";
    static final String LINK_TYPE_SELF_URI = "/basket/checkouts/$id/payment";
    static final String LINK_TYPE_RESOURCE_URI = "/basket/checkouts/$id";

//    @Mapping(source = "data.items", target = "items")
//    @Mapping(source = "data.kind", target = "kind")
    PaymentDetailsDTO CheckoutToPaymentDetailsMapper(Checkout checkout);

//    @AfterMapping
//    default void updateDTOWithPaymentDetails(CheckoutData checkoutData, @MappingTarget PaymentDetailsDTO paymentDetailsDTO) {
//        List<ItemDTO> items = new ArrayList<>();
//        for (Item item : checkoutData.getItems()) {
//            ItemDTO itemDTO = new ItemDTO();
//
//            List<String> classOfPayment = new ArrayList<>();
//            classOfPayment.add(CLASS_OF_PAYMENT_ORDERABLE_ITEM);
//            itemDTO.setClassOfPayment(classOfPayment);
//
//            List<String> availablePaymentMethods = new ArrayList<>();
//            availablePaymentMethods.add(AVAILABLE_PAYMENT_METHOD_CREDIT_CARD);
//            itemDTO.setAvailablePaymentMethods(availablePaymentMethods);
//
//            itemDTO.setKind(ITEM_RESOURCE_TYPE);
//            itemDTO.setResourceKind(ITEM_RESOURCE_TYPE);
//
//            items.add(itemDTO);
//        }
//
//        //PaymentLinks paymentLinks = new PaymentLinks();
//        //paymentLinks.setSelf(String.format(LINK_TYPE_SELF_URI, checkoutData.ge));
//        paymentDetailsDTO.setKind(CHECKOUT_RESOURCE_TYPE);
//        paymentDetailsDTO.setStatus(PaymentStatus.PENDING);
//    }
}
