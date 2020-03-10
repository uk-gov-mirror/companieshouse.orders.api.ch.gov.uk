package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.api.model.order.item.*;
import uk.gov.companieshouse.orders.api.model.*;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.companieshouse.api.model.order.item.ProductTypeApi.CERTIFICATE;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(ApiToCertificateMapperTest.Config.class)
public class ApiToCertificateMapperTest {
    private static final String ID = "CHS00000000000000001";
    private static final String COMPANY_NUMBER = "00006444";
    private static final int QUANTITY = 10;
    private static final String DESCRIPTION = "Certificate";
    private static final String DESCRIPTION_IDENTIFIER = "Description Identifier";
    private static final Map<String, String> DESCRIPTION_VALUES = singletonMap("key1", "value1");
    private static final String KIND = "certificate";
    private static final boolean POSTAL_DELIVERY = true;
    private static final String CUSTOMER_REFERENCE = "Certificate ordered by NJ.";
    private static final String COMPANY_NAME = "Phillips & Daughters";
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String CONTACT_NUMBER = "+44 1234 123456";
    private static final boolean INCLUDE_COMPANY_OBJECTS_INFORMATION = true;
    private static final boolean INCLUDE_EMAIL_COPY = true;
    private static final boolean INCLUDE_GOOD_STANDING_INFORMATION = false;

    private static final boolean INCLUDE_ADDRESS = true;
    private static final boolean INCLUDE_APPOINTMENT_DATE = false;
    private static final boolean INCLUDE_BASIC_INFORMATION = true;
    private static final boolean INCLUDE_COUNTRY_OF_RESIDENCE = false;
    private static final IncludeDobTypeApi INCLUDE_DOB_TYPE = IncludeDobTypeApi.PARTIAL;
    private static final boolean INCLUDE_NATIONALITY= false;
    private static final boolean INCLUDE_OCCUPATION = true;

    private static final IncludeAddressRecordsTypeApi INCLUDE_ADDRESS_RECORDS_TYPE = IncludeAddressRecordsTypeApi.CURRENT;
    private static final boolean INCLUDE_DATES = true;
    private static final String FORENAME = "John";
    private static final String SURNAME = "Smith";

    private static final String LINKS_SELF = "links/self";

    private static final CertificateItemOptionsApi ITEM_OPTIONS;
    private static final DirectorOrSecretaryDetailsApi DIRECTOR_OR_SECRETARY_DETAILS;
    private static final RegisteredOfficeAddressDetailsApi REGISTERED_OFFICE_ADDRESS_DETAILS;
    private static final ItemCostsApi ITEM_COSTS;
    private static final LinksApi LINKS_API;

    @Configuration
    @ComponentScan(basePackageClasses = ApiToCertificateMapperTest.class)
    static class Config {}

    @Autowired
    private ApiToCertificateMapper apiToCertificateMapper;

    static {
        ITEM_COSTS = new ItemCostsApi();
        ITEM_COSTS.setDiscountApplied("1");
        ITEM_COSTS.setItemCost("2");
        ITEM_COSTS.setCalculatedCost("3");
        ITEM_COSTS.setProductType(CERTIFICATE);

        DIRECTOR_OR_SECRETARY_DETAILS = new DirectorOrSecretaryDetailsApi();
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeAddress(INCLUDE_ADDRESS);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeAppointmentDate(INCLUDE_APPOINTMENT_DATE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeBasicInformation(INCLUDE_BASIC_INFORMATION);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeCountryOfResidence(INCLUDE_COUNTRY_OF_RESIDENCE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeDobType(INCLUDE_DOB_TYPE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeNationality(INCLUDE_NATIONALITY);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeOccupation(INCLUDE_OCCUPATION);

        REGISTERED_OFFICE_ADDRESS_DETAILS = new RegisteredOfficeAddressDetailsApi();
        REGISTERED_OFFICE_ADDRESS_DETAILS.setIncludeAddressRecordsType(INCLUDE_ADDRESS_RECORDS_TYPE);
        REGISTERED_OFFICE_ADDRESS_DETAILS.setIncludeDates(INCLUDE_DATES);

        ITEM_OPTIONS = new CertificateItemOptionsApi();
        ITEM_OPTIONS.setCertificateType(CertificateTypeApi.INCORPORATION);
        ITEM_OPTIONS.setCollectionLocation(CollectionLocationApi.BELFAST);
        ITEM_OPTIONS.setContactNumber(CONTACT_NUMBER);
        ITEM_OPTIONS.setDeliveryMethod(DeliveryMethodApi.POSTAL);
        ITEM_OPTIONS.setDeliveryTimescale(DeliveryTimescaleApi.STANDARD);
        ITEM_OPTIONS.setDirectorDetails(DIRECTOR_OR_SECRETARY_DETAILS);
        ITEM_OPTIONS.setForename(FORENAME);
        ITEM_OPTIONS.setIncludeCompanyObjectsInformation(INCLUDE_COMPANY_OBJECTS_INFORMATION);
        ITEM_OPTIONS.setIncludeEmailCopy(INCLUDE_EMAIL_COPY);
        ITEM_OPTIONS.setIncludeGoodStandingInformation(INCLUDE_GOOD_STANDING_INFORMATION);
        ITEM_OPTIONS.setRegisteredOfficeAddressDetails(REGISTERED_OFFICE_ADDRESS_DETAILS);
        ITEM_OPTIONS.setSecretaryDetails(DIRECTOR_OR_SECRETARY_DETAILS);
        ITEM_OPTIONS.setSurname(SURNAME);

        LINKS_API = new LinksApi();
        LINKS_API.setSelf(LINKS_SELF);

    }

    @Test
    public void testCertificateApiToCertificate() {
        CertificateApi certificateApi = new CertificateApi();
        certificateApi.setId(ID);
        certificateApi.setCompanyName(COMPANY_NAME);
        certificateApi.setCompanyNumber(COMPANY_NUMBER);
        certificateApi.setCustomerReference(CUSTOMER_REFERENCE);
        certificateApi.setQuantity(QUANTITY);
        certificateApi.setDescription(DESCRIPTION);
        certificateApi.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        certificateApi.setDescriptionValues(DESCRIPTION_VALUES);
        certificateApi.setItemCosts(singletonList(ITEM_COSTS));
        certificateApi.setKind(KIND);
        certificateApi.setPostalDelivery(POSTAL_DELIVERY);
        certificateApi.setItemOptions(ITEM_OPTIONS);
        certificateApi.setLinks(LINKS_API);

        final Certificate certificate = apiToCertificateMapper.apiToCertificate(certificateApi);

        assertEquals(certificateApi.getId(), certificate.getId());
        assertThat(certificateApi.getId(), is(certificate.getId()));
        assertThat(certificateApi.getCompanyName(), is(certificate.getCompanyName()));
        assertThat(certificateApi.getCompanyNumber(), is(certificate.getCompanyNumber()));
        assertThat(certificateApi.getCustomerReference(), is(certificate.getCustomerReference()));
        assertThat(certificateApi.getQuantity(), is(certificate.getQuantity()));
        assertThat(certificateApi.getDescription(), is(certificate.getDescription()));
        assertThat(certificateApi.getDescriptionIdentifier(), is(certificate.getDescriptionIdentifier()));
        assertThat(certificateApi.getDescriptionValues(), is(certificate.getDescriptionValues()));
        assertThat(certificateApi.getKind(), is(certificate.getKind()));
        assertThat(certificateApi.isPostalDelivery(), is(certificate.isPostalDelivery()));
        assertThat(certificateApi.getEtag(), is(certificate.getEtag()));
        assertThat(certificateApi.getLinks().getSelf(), is(certificate.getItemUri()));
        assertThat(certificateApi.getLinks().getSelf(), is(certificate.getLinks().getSelf()));

        assertItemCosts(certificateApi.getItemCosts().get(0), certificate.getItemCosts().get(0));
        assertItemOptionsSame(certificateApi.getItemOptions(), certificate.getItemOptions());
    }

    private void assertItemCosts(final ItemCostsApi itemCostsApi, final ItemCosts itemCosts) {
        assertThat(itemCostsApi.getDiscountApplied(), is(itemCosts.getDiscountApplied()));
        assertThat(itemCostsApi.getItemCost(), is(itemCosts.getItemCost()));
        assertThat(itemCostsApi.getCalculatedCost(), is(itemCosts.getCalculatedCost()));
        assertThat(itemCostsApi.getProductType().getJsonName(), is(itemCosts.getProductType().getJsonName()));
    }

    private void assertItemOptionsSame(final CertificateItemOptionsApi options1,
                                       final CertificateItemOptions options2) {
        assertThat(options1.getCertificateType().getJsonName(), is(options2.getCertificateType().getJsonName()));
        assertThat(options1.getCollectionLocation().getJsonName(), is(options2.getCollectionLocation().getJsonName()));
        assertThat(options1.getContactNumber(), is(options2.getContactNumber()));
        assertThat(options1.getDeliveryMethod().getJsonName(), is(options2.getDeliveryMethod().getJsonName()));
        assertThat(options1.getDeliveryTimescale().getJsonName(), is(options2.getDeliveryTimescale().getJsonName()));
        assertDetailsSame(options1.getDirectorDetails(), options2.getDirectorDetails());
        assertThat(options1.getForename(), is(options2.getForename()));
        assertThat(options1.getIncludeCompanyObjectsInformation(), is(options2.getIncludeCompanyObjectsInformation()));
        assertThat(options1.getIncludeEmailCopy(), is(options2.getIncludeEmailCopy()));
        assertThat(options1.getIncludeGoodStandingInformation(), is(options2.getIncludeGoodStandingInformation()));
        assertAddressDetailsSame(options1.getRegisteredOfficeAddressDetails(), options2.getRegisteredOfficeAddressDetails());
        assertDetailsSame(options1.getSecretaryDetails(), options2.getSecretaryDetails());
        assertThat(options1.getSurname(), is(options2.getSurname()));
    }

    private void assertDetailsSame(final DirectorOrSecretaryDetailsApi details1,
                                   final DirectorOrSecretaryDetails details2) {
        assertThat(details1.getIncludeAddress(), is(details2.getIncludeAddress()));
        assertThat(details1.getIncludeAppointmentDate(), is(details2.getIncludeAppointmentDate()));
        assertThat(details1.getIncludeBasicInformation(), is(details2.getIncludeBasicInformation()));
        assertThat(details1.getIncludeCountryOfResidence(), is(details2.getIncludeCountryOfResidence()));
        assertThat(details1.getIncludeDobType().getJsonName(), is(details2.getIncludeDobType().getJsonName()));
        assertThat(details1.getIncludeNationality(), is(details2.getIncludeNationality()));
        assertThat(details1.getIncludeOccupation(), is(details2.getIncludeOccupation()));
    }

    private void assertAddressDetailsSame(final RegisteredOfficeAddressDetailsApi details1,
                                          final RegisteredOfficeAddressDetails details2) {
        assertThat(details1.getIncludeAddressRecordsType().getJsonName(), is(details2.getIncludeAddressRecordsType().getJsonName()));
        assertThat(details1.getIncludeDates(), is(details2.getIncludeDates()));
    }
}
