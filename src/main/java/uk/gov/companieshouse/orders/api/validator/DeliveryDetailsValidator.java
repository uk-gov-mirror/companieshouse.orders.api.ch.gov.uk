package uk.gov.companieshouse.orders.api.validator;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryDetailsValidator {

    public DeliveryDetailsValidator() {
    }

    public List<String> getValidationErrors(final AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO) {
        List<String> errors = new ArrayList<>();

        if(addDeliveryDetailsRequestDTO.getPostalCode().isEmpty() && addDeliveryDetailsRequestDTO.getRegion().isEmpty()) {
            errors.add("Post code or Region is required");
        }
        return errors;
    }
}
