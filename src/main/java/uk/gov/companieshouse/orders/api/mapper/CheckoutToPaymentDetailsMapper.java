package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckoutToPaymentDetailsMapper {

    @Mapping(source = "data.paidAt", target = "paidAt")
    @Mapping(source = "data.reference", target = "paymentReference")
    @Mapping(source = "data.status", target = "status")
    @Mapping(source = "data.links.payment", target = "links.self")
    @Mapping(source = "data.links.self", target = "links.resource")
    @Mapping(source = "data.etag", target = "etag")
    PaymentDetailsDTO checkoutToPaymentDetailsMapper(Checkout checkout);

    default void updateDTOWithPaymentDetails(CheckoutData checkoutData, PaymentDetailsDTO paymentDetailsDTO) {
        List<ItemDTO> itemDTOs = new ArrayList<>();
        Item item = checkoutData.getItems().get(0);
        for (ItemCosts itemCosts : item.getItemCosts()) {
            ItemDTO itemDTO = new ItemDTO();

            List<String> classOfPayment = new ArrayList<>();
            classOfPayment.add("orderable-item");
            itemDTO.setClassOfPayment(classOfPayment);

            List<String> availablePaymentMethods = new ArrayList<>();
            availablePaymentMethods.add("credit-card");
            itemDTO.setAvailablePaymentMethods(availablePaymentMethods);

            itemDTO.setResourceKind(item.getKind());
            itemDTO.setKind("cost#cost");

            itemDTO.setProductType(itemCosts.getProductType().getJsonName());
            itemDTO.setAmount(itemCosts.getCalculatedCost());

            itemDTO.setDescriptionIdentifier(item.getDescriptionIdentifier());
            itemDTO.setDescriptionValues(item.getDescriptionValues());

            itemDTOs.add(itemDTO);
        }

        paymentDetailsDTO.setItems(itemDTOs);
        paymentDetailsDTO.setKind("payment-details#payment-details");
    }
}
