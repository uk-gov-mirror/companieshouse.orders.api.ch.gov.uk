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
        if(addDeliveryDetailsRequestDTO.getAddressLine1().isEmpty()) {
            errors.add("Address line 1 is a required field");
        }
        if(addDeliveryDetailsRequestDTO.getCountry().isEmpty()) {
            errors.add("Country is a required field");
        }
        if(addDeliveryDetailsRequestDTO.getForename().isEmpty()) {
            errors.add("Forename is a required field");
        }
        if(addDeliveryDetailsRequestDTO.getLocality().isEmpty()) {
            errors.add("Locality is a required field");
        }
        if(addDeliveryDetailsRequestDTO.getPremises().isEmpty()) {
            errors.add("Premises is a required field");
        }
        if(addDeliveryDetailsRequestDTO.getSurname().isEmpty()) {
            errors.add("Surname is a required field");
        }
        return errors;
    }
}
