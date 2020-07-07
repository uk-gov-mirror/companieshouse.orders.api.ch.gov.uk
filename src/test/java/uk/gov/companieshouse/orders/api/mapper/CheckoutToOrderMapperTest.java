package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.model.*;

import java.time.LocalDateTime;
import java.util.Map;

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

/**
 * Unit tests the {@link CheckoutToOrderMapperTest} class.
 */
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(CheckoutToOrderMapperTest.Config.class)
public class CheckoutToOrderMapperTest {

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

    private static final CertificateItemOptions ITEM_OPTIONS;
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

        ITEM_OPTIONS = new CertificateItemOptions();
        ITEM_OPTIONS.setCertificateType(INCORPORATION);
        ITEM_OPTIONS.setCollectionLocation(BELFAST);
        ITEM_OPTIONS.setContactNumber(CONTACT_NUMBER);
        ITEM_OPTIONS.setDeliveryMethod(POSTAL);
        ITEM_OPTIONS.setDeliveryTimescale(STANDARD);
        ITEM_OPTIONS.setDirectorDetails(DIRECTOR_OR_SECRETARY_DETAILS);
        ITEM_OPTIONS.setIncludeCompanyObjectsInformation(INCLUDE_COMPANY_OBJECTS_INFORMATION);
        ITEM_OPTIONS.setIncludeEmailCopy(INCLUDE_EMAIL_COPY);
        ITEM_OPTIONS.setIncludeGoodStandingInformation(INCLUDE_GOOD_STANDING_INFORMATION);
        ITEM_OPTIONS.setRegisteredOfficeAddressDetails(REGISTERED_OFFICE_ADDRESS_DETAILS);
        ITEM_OPTIONS.setSecretaryDetails(DIRECTOR_OR_SECRETARY_DETAILS);

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
        final Item item = new Item();
        item.setCompanyName(COMPANY_NAME);
        item.setCompanyNumber(COMPANY_NUMBER);
        item.setCustomerReference(CUSTOMER_REFERENCE);
        item.setQuantity(QUANTITY);
        item.setDescription(DESCRIPTION);
        item.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        item.setDescriptionValues(DESCRIPTION_VALUES);
        item.setItemCosts(singletonList(ITEM_COSTS));
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);
        item.setKind(ITEM_KIND);
        item.setPostalDelivery(POSTAL_DELIVERY);
        // TODO GCI-1242 Restore this item.setItemOptions(ITEM_OPTIONS);
        item.setEtag(TOKEN_ETAG);
        data.setItems(singletonList(item));
        checkout.setData(data);
        final Order order = mapperUnderTest.checkoutToOrder(checkout);

        assertThat(order.getId(), is(checkout.getId()));
        assertThat(order.getUserId(), is(checkout.getUserId()));
        assertThat(order.getData(), is(notNullValue()));
        assertThat(order.getData().getItems(), is(checkout.getData().getItems()));

        // This assertion succeeds because the item is in fact the same instance.
        assertThat(order.getData().getItems().get(0), is(checkout.getData().getItems().get(0)));

        assertThat(order.getData().getDeliveryDetails(), is(checkout.getData().getDeliveryDetails()));
        assertThat(order.getData().getEtag(), is(checkout.getData().getEtag()));
        assertThat(order.getData().getKind(), is(checkout.getData().getKind()));
        assertThat(order.getData().getLinks().getSelf(), is(checkout.getData().getLinks().getSelf()));
        assertThat(order.getData().getPaymentReference(), is(checkout.getData().getPaymentReference()));
        assertThat(order.getData().getTotalOrderCost(), is(checkout.getData().getTotalOrderCost()));
        assertThat(order.getData().getReference(), is(checkout.getData().getReference()));
        assertThat(order.getData().getOrderedBy(), is(checkout.getData().getCheckedOutBy()));
    }

}
