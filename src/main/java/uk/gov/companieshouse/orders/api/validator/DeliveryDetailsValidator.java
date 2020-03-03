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

        String postalCode = addDeliveryDetailsRequestDTO.getDeliveryDetails().getPostalCode();
        String region = addDeliveryDetailsRequestDTO.getDeliveryDetails().getRegion();
        if( (postalCode == null || postalCode.isEmpty()) &&
                (region==null || region.isEmpty())) {
            errors.add("Post code or Region is required");
        }
        return errors;
    }
}
