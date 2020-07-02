package uk.gov.companieshouse.orders.api.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryDetailsValidator {

    public DeliveryDetailsValidator() { }

    public List<String> getValidationErrors(final AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO) {
        List<String> errors = new ArrayList<>();

        String postalCode = addDeliveryDetailsRequestDTO.getDeliveryDetails().getPostalCode();
        String region = addDeliveryDetailsRequestDTO.getDeliveryDetails().getRegion();

        if(StringUtils.isBlank(postalCode) && StringUtils.isBlank(region)) {
            errors.add("Postcode or Region is required");
        }

        return errors;
    }

    public boolean isValid(final DeliveryDetails deliveryDetails) {
        return (deliveryDetails != null &&
                StringUtils.isNotBlank(deliveryDetails.getAddressLine1()) &&
                StringUtils.isNotBlank(deliveryDetails.getForename()) &&
                StringUtils.isNotBlank(deliveryDetails.getSurname()) &&
                StringUtils.isNotBlank(deliveryDetails.getCountry()) &&
                StringUtils.isNotBlank(deliveryDetails.getLocality()) &&
                (StringUtils.isNotBlank(deliveryDetails.getPostalCode()) ||
                        StringUtils.isNotBlank(deliveryDetails.getRegion())));
    }
}
