package uk.gov.companieshouse.orders.api.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class DeliveryDetailsValidatorTest {

    private DeliveryDetailsValidator deliveryDetailsValidator;

    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String PREMISES = "premises";
    private static final String SURNAME = "surname";

    @BeforeEach
    void setUp() {
        deliveryDetailsValidator = new DeliveryDetailsValidator();
    }

    @Test
    @DisplayName("Postcode or region is mandatory")
    void postcodeOrRegionIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode("");
        dto.setPremises(PREMISES);
        dto.setRegion("");
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Post code or Region is required"));
    }
}
