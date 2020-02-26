package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.CheckoutDataDTO;
import uk.gov.companieshouse.orders.api.model.CheckoutData;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckoutDataMapper {

    static final String CLASS_OF_PAYMENT_ORDERABLE_ITEM = "orderable-item";
    CheckoutData checkoutDataDTOToCheckoutData(CheckoutDataDTO checkoutDataDTO);
    CheckoutDataDTO checkoutDataToCheckoutDataDTO(CheckoutData checkoutData);

    @AfterMapping
    default void updateDTOWithPaymentClass(@MappingTarget CheckoutDataDTO checkoutDataDTO) {
        List<String> classOfPayment = new ArrayList<>();
        classOfPayment.add(CLASS_OF_PAYMENT_ORDERABLE_ITEM);
        checkoutDataDTO.getItems().get(0).setClassOfPayment(classOfPayment);
    }
}
