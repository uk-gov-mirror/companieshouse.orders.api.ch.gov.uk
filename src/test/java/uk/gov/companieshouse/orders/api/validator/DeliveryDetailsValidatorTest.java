package uk.gov.companieshouse.orders.api.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class DeliveryDetailsValidatorTest {

    private DeliveryDetailsValidator deliveryDetailsValidator;

    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String PREMISES = "premises";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";

    @BeforeEach
    void setUp() {
        deliveryDetailsValidator = new DeliveryDetailsValidator();
    }

    @Test
    @DisplayName("Address line 1 is mandatory")
    void addressLine1IsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1("");
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Address line 1 is a required field"));
    }

    @Test
    @DisplayName("Country is mandatory")
    void countryIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry("");
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Country is a required field"));
    }

    @Test
    @DisplayName("Forename is mandatory")
    void forenameIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename("");
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Forename is a required field"));
    }

    @Test
    @DisplayName("Locality is mandatory")
    void localityIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality("");
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Locality is a required field"));
    }

    @Test
    @DisplayName("Premises is mandatory")
    void premisesIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises("");
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Premises is a required field"));
    }

    @Test
    @DisplayName("Surname is mandatory")
    void surnameIsMandatory() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname("");

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Surname is a required field"));
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

    @Test
    @DisplayName("List of multiple mandatory items is returned")
    void listOfMandatoryItemsReturned() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1("");
        dto.setAddressLine2(ADDRESS_LINE_2);
        dto.setCountry("");
        dto.setForename("");
        dto.setLocality("");
        dto.setPoBox(PO_BOX);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises("");
        dto.setRegion(REGION);
        dto.setSurname("");

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, contains("Address line 1 is a required field",
                                    "Country is a required field",
                                    "Forename is a required field",
                                    "Locality is a required field",
                                    "Premises is a required field",
                                    "Surname is a required field"));
    }

    @Test
    @DisplayName("All mandatory fields provided")
    void allMandatoryFieldsProvided() {
        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        dto.setAddressLine1(ADDRESS_LINE_1);
        dto.setCountry(COUNTRY);
        dto.setForename(FORENAME);
        dto.setLocality(LOCALITY);
        dto.setPostalCode(POSTAL_CODE);
        dto.setPremises(PREMISES);
        dto.setRegion(REGION);
        dto.setSurname(SURNAME);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(dto);

        assertThat(errors, is(empty()));
    }
}
