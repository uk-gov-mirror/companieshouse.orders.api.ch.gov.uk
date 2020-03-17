package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.CheckoutDataDTO;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckoutDataMapper {

    static final String CLASS_OF_PAYMENT_ORDERABLE_ITEM = "orderable-item";
    static final String AVAILABLE_PAYMENT_METHOD_CREDIT_CARD = "credit-card";
    static final String AVAILABLE_PAYMENT_METHOD_ACCOUNT = "account";
    static final String ITEM_RESOURCE_TYPE = "cost#cost";
    static final String CHECKOUT_RESOURCE_TYPE = "payment-details#payment-details";
    static final String LINK_TYPE_SELF_URL_PREFIX = "/basket/payment/";

    CheckoutData checkoutDataDTOToCheckoutData(CheckoutDataDTO checkoutDataDTO);
    CheckoutDataDTO checkoutDataToCheckoutDataDTO(CheckoutData checkoutData);

    @AfterMapping
    default void updateDTOWithPaymentClass(CheckoutData checkoutData, @MappingTarget CheckoutDataDTO checkoutDataDTO) {
        Item item = checkoutData.getItems().get(0);
        ItemDTO itemDTO = checkoutDataDTO.getItems().get(0);

        List<String> classOfPayment = new ArrayList<>();
        classOfPayment.add(CLASS_OF_PAYMENT_ORDERABLE_ITEM);
        itemDTO.setClassOfPayment(classOfPayment);

        List<String> availablePaymentMethods = new ArrayList<>();
        availablePaymentMethods.add(AVAILABLE_PAYMENT_METHOD_CREDIT_CARD);
        itemDTO.setAvailablePaymentMethods(availablePaymentMethods);

        itemDTO.setKind(ITEM_RESOURCE_TYPE);
        itemDTO.setResourceKind(ITEM_RESOURCE_TYPE);

        checkoutDataDTO.setKind(CHECKOUT_RESOURCE_TYPE);
        checkoutDataDTO.setStatus(PaymentStatus.PENDING.toString());
    }
}
