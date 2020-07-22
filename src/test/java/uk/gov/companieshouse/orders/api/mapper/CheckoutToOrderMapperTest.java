package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.CheckoutLinks;
import uk.gov.companieshouse.orders.api.model.CollectionLocation;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.orders.api.model.DirectorOrSecretaryDetails;
import uk.gov.companieshouse.orders.api.model.IncludeAddressRecordsType;
import uk.gov.companieshouse.orders.api.model.IncludeDobType;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.RegisteredOfficeAddressDetails;

import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION;
import static uk.gov.companieshouse.orders.api.model.CollectionLocation.BELFAST;
import static uk.gov.companieshouse.orders.api.model.DeliveryMethod.POSTAL;
import static uk.gov.companieshouse.orders.api.model.DeliveryTimescale.STANDARD;
import static uk.gov.companieshouse.orders.api.model.IncludeAddressRecordsType.CURRENT;
import static uk.gov.companieshouse.orders.api.model.IncludeDobType.PARTIAL;
import static uk.gov.companieshouse.orders.api.model.PaymentStatus.PAID;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.DOCUMENT;

/**
 * Unit tests the {@link CheckoutToOrderMapperTest} class.
 */
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(CheckoutToOrderMapperTest.Config.class)
class CheckoutToOrderMapperTest {

    private static final String ID = "CHS00000000000000001";
    private static final String COMPANY_NUMBER = "00006444";
    private static final int QUANTITY = 10;
    private static final String DESCRIPTION = "Certificate";
    private static final String DESCRIPTION_IDENTIFIER = "Description Identifier";
    private static final Map<String, String> DESCRIPTION_VALUES = singletonMap("key1", "value1");
    private static final ItemCosts ITEM_COSTS = new ItemCosts("1", "2", "3", CERTIFICATE);
    private static final String POSTAGE_COST = "0";
    private static final String TOTAL_ITEM_COST = "100";
    private static final String ITEM_KIND = "certificate";
    private static final String ORDER_KIND = "order";
    private static final boolean POSTAL_DELIVERY = true;
    private static final String CUSTOMER_REFERENCE = "Certificate ordered by NJ.";
    private static final String ORDER_REFERENCE = "Order reference";
    private static final String COMPANY_NAME = "Phillips & Daughters";
    private static final String TOKEN_ETAG = "9d39ea69b64c80ca42ed72328b48c303c4445e28";
    private static final String CONTACT_NUMBER = "+44 1234 123456";
    private static final String USER_ID = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";
    private static final boolean INCLUDE_COMPANY_OBJECTS_INFORMATION = true;
    private static final boolean INCLUDE_EMAIL_COPY = true;
    private static final boolean INCLUDE_GOOD_STANDING_INFORMATION = false;

    private static final boolean INCLUDE_ADDRESS = true;
    private static final boolean INCLUDE_APPOINTMENT_DATE = false;
    private static final boolean INCLUDE_BASIC_INFORMATION = true;
    private static final boolean INCLUDE_COUNTRY_OF_RESIDENCE = false;
    private static final IncludeDobType INCLUDE_DOB_TYPE = PARTIAL;
    private static final boolean INCLUDE_NATIONALITY= false;
    private static final boolean INCLUDE_OCCUPATION = true;

    private static final IncludeAddressRecordsType INCLUDE_ADDRESS_RECORDS_TYPE = CURRENT;
    private static final boolean INCLUDE_DATES = true;
    private static final String FORENAME = "John";
    private static final String SURNAME = "Smith";

    private static final CertificateItemOptions CERTIFICATE_ITEM_OPTIONS;
    private static final CertifiedCopyItemOptions CERTIFIED_COPY_ITEM_OPTIONS;
    private static final DirectorOrSecretaryDetails DIRECTOR_OR_SECRETARY_DETAILS;
    private static final RegisteredOfficeAddressDetails REGISTERED_OFFICE_ADDRESS_DETAILS;
    private static final DeliveryDetails DELIVERY_DETAILS;
    private static final CheckoutLinks LINKS;
    private static final ActionedBy ACTIONED_BY;

    static {
        DIRECTOR_OR_SECRETARY_DETAILS = new DirectorOrSecretaryDetails();
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeAddress(INCLUDE_ADDRESS);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeAppointmentDate(INCLUDE_APPOINTMENT_DATE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeBasicInformation(INCLUDE_BASIC_INFORMATION);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeCountryOfResidence(INCLUDE_COUNTRY_OF_RESIDENCE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeDobType(INCLUDE_DOB_TYPE);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeNationality(INCLUDE_NATIONALITY);
        DIRECTOR_OR_SECRETARY_DETAILS.setIncludeOccupation(INCLUDE_OCCUPATION);

        REGISTERED_OFFICE_ADDRESS_DETAILS = new RegisteredOfficeAddressDetails();
        REGISTERED_OFFICE_ADDRESS_DETAILS.setIncludeAddressRecordsType(INCLUDE_ADDRESS_RECORDS_TYPE);
        REGISTERED_OFFICE_ADDRESS_DETAILS.setIncludeDates(INCLUDE_DATES);

        CERTIFICATE_ITEM_OPTIONS = new CertificateItemOptions();
        CERTIFICATE_ITEM_OPTIONS.setCertificateType(INCORPORATION);
        CERTIFICATE_ITEM_OPTIONS.setCollectionLocation(BELFAST);
        CERTIFICATE_ITEM_OPTIONS.setContactNumber(CONTACT_NUMBER);
        CERTIFICATE_ITEM_OPTIONS.setDeliveryMethod(POSTAL);
        CERTIFICATE_ITEM_OPTIONS.setDeliveryTimescale(STANDARD);
        CERTIFICATE_ITEM_OPTIONS.setDirectorDetails(DIRECTOR_OR_SECRETARY_DETAILS);
        CERTIFICATE_ITEM_OPTIONS.setIncludeCompanyObjectsInformation(INCLUDE_COMPANY_OBJECTS_INFORMATION);
        CERTIFICATE_ITEM_OPTIONS.setIncludeEmailCopy(INCLUDE_EMAIL_COPY);
        CERTIFICATE_ITEM_OPTIONS.setIncludeGoodStandingInformation(INCLUDE_GOOD_STANDING_INFORMATION);
        CERTIFICATE_ITEM_OPTIONS.setRegisteredOfficeAddressDetails(REGISTERED_OFFICE_ADDRESS_DETAILS);
        CERTIFICATE_ITEM_OPTIONS.setSecretaryDetails(DIRECTOR_OR_SECRETARY_DETAILS);

        CERTIFIED_COPY_ITEM_OPTIONS = new CertifiedCopyItemOptions();
        CERTIFIED_COPY_ITEM_OPTIONS.setCollectionLocation(CollectionLocation.BELFAST);
        CERTIFIED_COPY_ITEM_OPTIONS.setContactNumber(CONTACT_NUMBER);
        CERTIFIED_COPY_ITEM_OPTIONS.setDeliveryMethod(DeliveryMethod.POSTAL);
        CERTIFIED_COPY_ITEM_OPTIONS.setDeliveryTimescale(DeliveryTimescale.STANDARD);
        CERTIFIED_COPY_ITEM_OPTIONS.setFilingHistoryDocuments(singletonList(DOCUMENT));
        CERTIFIED_COPY_ITEM_OPTIONS.setForename(FORENAME);
        CERTIFIED_COPY_ITEM_OPTIONS.setSurname(SURNAME);

        DELIVERY_DETAILS = new DeliveryDetails();
        DELIVERY_DETAILS.setForename("George");
        DELIVERY_DETAILS.setSurname("Best");
        DELIVERY_DETAILS.setAddressLine1("Line 1");
        DELIVERY_DETAILS.setAddressLine2("Line 2");
        DELIVERY_DETAILS.setLocality("Locality");
        DELIVERY_DETAILS.setPoBox("PO Box");
        DELIVERY_DETAILS.setPostalCode("CF23 8LL");
        DELIVERY_DETAILS.setRegion("Region");
        DELIVERY_DETAILS.setCountry("Wales");

        LINKS = new CheckoutLinks();
        LINKS.setSelf("Self");

        ACTIONED_BY = new ActionedBy();
        ACTIONED_BY.setId("1234");
        ACTIONED_BY.setEmail("1234@nowhere.com");
    }

    @Configuration
    @ComponentScan(basePackageClasses = CheckoutToOrderMapperTest.class)
    static class Config {}

    @Autowired
    private CheckoutToOrderMapper mapperUnderTest;

    @Test
    void testCheckoutToOrderMapping() {
        final LocalDateTime time = LocalDateTime.now();
        final Checkout checkout = new Checkout();
        checkout.setId(ID);
        checkout.setUserId(USER_ID);
        final CheckoutData data = new CheckoutData();
        data.setDeliveryDetails(DELIVERY_DETAILS);
        data.setEtag(TOKEN_ETAG);
        data.setKind(ORDER_KIND);
        data.setLinks(LINKS);
        data.setPaymentReference("1234");
        data.setTotalOrderCost("100");
        data.setStatus(PAID);
        data.setReference(ORDER_REFERENCE);
        data.setPaidAt(time);
        data.setCheckedOutBy(ACTIONED_BY);

        final Certificate checkoutCertificate = new Certificate();
        checkoutCertificate.setCompanyName(COMPANY_NAME);
        checkoutCertificate.setCompanyNumber(COMPANY_NUMBER);
        checkoutCertificate.setCustomerReference(CUSTOMER_REFERENCE);
        checkoutCertificate.setQuantity(QUANTITY);
        checkoutCertificate.setDescription(DESCRIPTION);
        checkoutCertificate.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        checkoutCertificate.setDescriptionValues(DESCRIPTION_VALUES);
        checkoutCertificate.setItemCosts(singletonList(ITEM_COSTS));
        checkoutCertificate.setPostageCost(POSTAGE_COST);
        checkoutCertificate.setTotalItemCost(TOTAL_ITEM_COST);
        checkoutCertificate.setKind(ITEM_KIND);
        checkoutCertificate.setPostalDelivery(POSTAL_DELIVERY);
        checkoutCertificate.setItemOptions(CERTIFICATE_ITEM_OPTIONS);
        checkoutCertificate.setEtag(TOKEN_ETAG);

        final CertifiedCopy checkoutCopy = new CertifiedCopy();
        checkoutCopy.setCompanyName(COMPANY_NAME);
        checkoutCopy.setCompanyNumber(COMPANY_NUMBER);
        checkoutCopy.setCustomerReference(CUSTOMER_REFERENCE);
        checkoutCopy.setQuantity(QUANTITY);
        checkoutCopy.setDescription(DESCRIPTION);
        checkoutCopy.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        checkoutCopy.setDescriptionValues(DESCRIPTION_VALUES);
        checkoutCopy.setItemCosts(singletonList(ITEM_COSTS));
        checkoutCopy.setPostageCost(POSTAGE_COST);
        checkoutCopy.setTotalItemCost(TOTAL_ITEM_COST);
        checkoutCopy.setKind(ITEM_KIND);
        checkoutCopy.setPostalDelivery(POSTAL_DELIVERY);
        checkoutCopy.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);
        checkoutCopy.setEtag(TOKEN_ETAG);

        data.setItems(asList(checkoutCertificate, checkoutCopy));
        checkout.setData(data);
        final Order order = mapperUnderTest.checkoutToOrder(checkout);

        assertThat(order.getId(), is(checkout.getId()));
        assertThat(order.getUserId(), is(checkout.getUserId()));
        assertThat(order.getData(), is(notNullValue()));
        assertThat(order.getData().getItems(), is(checkout.getData().getItems()));

        assertThat(order.getData().getDeliveryDetails(), is(checkout.getData().getDeliveryDetails()));
        assertThat(order.getData().getEtag(), is(checkout.getData().getEtag()));
        assertThat(order.getData().getKind(), is(checkout.getData().getKind()));
        assertThat(order.getData().getLinks().getSelf(), is(checkout.getData().getLinks().getSelf()));
        assertThat(order.getData().getPaymentReference(), is(checkout.getData().getPaymentReference()));
        assertThat(order.getData().getTotalOrderCost(), is(checkout.getData().getTotalOrderCost()));
        assertThat(order.getData().getReference(), is(checkout.getData().getReference()));
        assertThat(order.getData().getOrderedBy(), is(checkout.getData().getCheckedOutBy()));

        assertThat(order.getData().getItems().size(), is(2));
        assertThat(order.getData().getItems().get(0) instanceof Certificate, is(true));
        final Certificate orderCertificate = (Certificate) order.getData().getItems().get(0);
        assertThat(order.getData().getItems().get(1) instanceof CertifiedCopy, is(true));
        final CertifiedCopy orderCopy = (CertifiedCopy) order.getData().getItems().get(1);

        org.assertj.core.api.Assertions.assertThat(orderCertificate).isEqualToComparingFieldByField(checkoutCertificate);
        org.assertj.core.api.Assertions.assertThat(orderCopy).isEqualToComparingFieldByField(checkoutCopy);

    }

}
