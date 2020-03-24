package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(CheckoutToPaymentDetailsMapperTest.Config.class)
public class CheckoutToPaymentDetailsMapperTest {
    static final CheckoutData CHECKOUT_DATA = new CheckoutData();
    static final String ETAG = "d34e69c50fec2207808dd77f6ed17e832acc1f18";
    static final String USER_EMAIL = "demo@ch.gov.uk";
    static final String USER_ID = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";

    static final DeliveryDetails DELIVERY_DETAILS = new DeliveryDetails();
    static final String REFERENCE = "5e73592e5821a83750274f02";
    static final PaymentStatus STATUS = PaymentStatus.PENDING;
    static final CheckoutLinks CHECKOUT_LINKS = new CheckoutLinks();
    static final ActionedBy CHECKED_OUT_BY = new ActionedBy();
    static final String ITEM_ID = "CHS00000000000000001";
    static final String COMPANY_NAME = "Dummies Ltd";
    static final String COMPANY_NUMBER = "000000001";
    static final String DESC_VALUE_KEY = "company_number";
    static final String DESC_IDENTIFIER = "certificate";
    static final Map<String, String> DESC_VALUES = new HashMap<>();

    static final List<ItemCosts> ITEM_COSTS_LIST = new ArrayList<>();
    static final String DISCOUNT_APPLIED_1 = "0";
    static final String ITEM_COST_1 = "15";
    static final String CALCULATED_COST_1 = "15";
    static final ProductType PRODUCT_TYPE_1 = ProductType.CERTIFICATE;
    static final String DISCOUNT_APPLIED_2 = "5";
    static final String ITEM_COST_2 = "15";
    static final String CALCULATED_COST_2 = "10";
    static final ProductType PRODUCT_TYPE_2 = ProductType.CERTIFICATE_ADDITIONAL_COPY;
    static final String DISCOUNT_APPLIED_3 = "5";
    static final String ITEM_COST_3 = "15";
    static final String CALCULATED_COST_3 = "10";
    static final ProductType PRODUCT_TYPE_3 = ProductType.CERTIFICATE_ADDITIONAL_COPY;

    static final CertificateItemOptions ITEM_OPTIONS = new CertificateItemOptions();
    static final List<Item> ITEMS = new ArrayList<>();
    static final Item ITEM = new Item();
    static final String ITEM_ETAG = "5b5516a6ad2b1eb27b30213191884cda147529db";
    static final String ITEM_KIND = "item#certificate";
    static final ItemLinks ITEM_LINKS = new ItemLinks();
    static final String ITEM_SELF_URI = "/orderable/certificates/CHS00000000000000001";
    static final int ITEM_QUANTITY = 1;
    static final String CHECKOUT_PAYMENT_URI = "/basket/checkouts/5e73592e5821a83750274f02/payment";
    static final String CHECKOUT_SELF_URI = "/basket/checkouts/5e73592e5821a83750274f02";
    static final ItemStatus ITEM_STATUS = ItemStatus.UNKNOWN;
    static final String POSTAGE_COST = "0";
    static final String TOTAL_ITEM_COST = "35";
    static final boolean POSTAL_DELIVERY = true;
    static {
        final ItemCosts ITEM_COSTS_1 = new ItemCosts(DISCOUNT_APPLIED_1, ITEM_COST_1, CALCULATED_COST_1, PRODUCT_TYPE_1);
        final ItemCosts ITEM_COSTS_2 = new ItemCosts(DISCOUNT_APPLIED_2, ITEM_COST_2, CALCULATED_COST_2, PRODUCT_TYPE_2);
        final ItemCosts ITEM_COSTS_3 = new ItemCosts(DISCOUNT_APPLIED_3, ITEM_COST_3, CALCULATED_COST_3, PRODUCT_TYPE_3);
        ITEM_COSTS_LIST.add(ITEM_COSTS_1);
        ITEM_COSTS_LIST.add(ITEM_COSTS_2);
        ITEM_COSTS_LIST.add(ITEM_COSTS_3);
        ITEM_LINKS.setSelf(ITEM_SELF_URI);

        DESC_VALUES.put(DESC_VALUE_KEY, COMPANY_NUMBER);
        ITEM.setId(ITEM_ID);
        ITEM.setCompanyName(COMPANY_NAME);
        ITEM.setCompanyNumber(COMPANY_NUMBER);
        ITEM.setDescriptionIdentifier(DESC_IDENTIFIER);
        ITEM.setDescriptionValues(DESC_VALUES);
        ITEM.setItemOptions(ITEM_OPTIONS);
        ITEM.setEtag(ITEM_ETAG);
        ITEM.setKind(ITEM_KIND);
        ITEM.setQuantity(ITEM_QUANTITY);
        ITEM.setLinks(ITEM_LINKS);
        ITEM.setItemCosts(ITEM_COSTS_LIST);
        ITEM.setStatus(ITEM_STATUS);
        ITEM.setPostageCost(POSTAGE_COST);
        ITEM.setTotalItemCost(TOTAL_ITEM_COST);
        ITEM.setPostalDelivery(POSTAL_DELIVERY);
        ITEMS.add(ITEM);

        CHECKED_OUT_BY.setEmail(USER_EMAIL);
        CHECKED_OUT_BY.setId(USER_ID);

        CHECKOUT_LINKS.setSelf(CHECKOUT_SELF_URI);
        CHECKOUT_LINKS.setPayment(CHECKOUT_PAYMENT_URI);

        CHECKOUT_DATA.setEtag(ETAG);
        CHECKOUT_DATA.setDeliveryDetails(DELIVERY_DETAILS);
        CHECKOUT_DATA.setReference(REFERENCE);
        CHECKOUT_DATA.setCheckedOutBy(CHECKED_OUT_BY);
        CHECKOUT_DATA.setStatus(STATUS);
        CHECKOUT_DATA.setLinks(CHECKOUT_LINKS);
        CHECKOUT_DATA.setItems(ITEMS);

    }
    static final String COMPANY_NUMBER_KEY = "company_number";
    static final String EXPECTED_KIND = "payment-details#payment-details";
    static final String EXPECTED_ITEM_COST = "cost#cost";
    static final String EXPECTED_CLASS_OF_PAYMENT = "orderable-item";
    static final String EXPECTED_AVAILABLE_PAYMENT_METHODS = "credit-card";

    @Configuration
    @ComponentScan(basePackageClasses = CheckoutToPaymentDetailsMapperTest.class)
    static class Config {}

    @Autowired
    CheckoutToPaymentDetailsMapper checkoutToPaymentDetailsMapper;

    @Test
    public void testCheckoutToPaymentDetailsMapper() {
        Checkout source = new Checkout();
        source.setData(CHECKOUT_DATA);

        final PaymentDetailsDTO target
                = checkoutToPaymentDetailsMapper.checkoutToPaymentDetailsMapper(source);
        checkoutToPaymentDetailsMapper.updateDTOWithPaymentDetails(source.getData(), target);

        assertThat(target.getEtag(), is(source.getData().getEtag()));
        assertThat(target.getKind(), is(EXPECTED_KIND));
        assertThat(target.getStatus(), is(source.getData().getStatus()));
        assertThat(target.getPaymentReference(), is(source.getData().getReference()));

        testLinks(source, target);
        testItems(source, target);
    }

    private void testItems(Checkout source, PaymentDetailsDTO target){
        assertEquals(target.getItems().size(), source.getData().getItems().get(0).getItemCosts().size());
        assertThat(target.getItems().get(0).getDescriptionIdentifier(), is(source.getData().getItems().get(0).getDescriptionIdentifier()));
        assertThat(target.getItems().get(0).getKind(), is(EXPECTED_ITEM_COST));
        assertThat(target.getItems().get(0).getResourceKind(), is(source.getData().getItems().get(0).getKind()));
        assertFalse(target.getItems().get(0).getAvailablePaymentMethods().isEmpty());
        assertTrue(target.getItems().get(0).getAvailablePaymentMethods().contains(EXPECTED_AVAILABLE_PAYMENT_METHODS));
        assertThat(target.getItems().get(0).getAmount(), is(source.getData().getItems().get(0).getItemCosts().get(0).getCalculatedCost()));
        assertThat(target.getItems().get(0).getDescriptionValues().get(COMPANY_NUMBER_KEY), is(COMPANY_NUMBER));
        assertFalse(target.getItems().get(0).getClassOfPayment().isEmpty());
        assertTrue(target.getItems().get(0).getClassOfPayment().contains(EXPECTED_CLASS_OF_PAYMENT));
        assertThat(target.getItems().get(0).getProductType(), is(source.getData().getItems().get(0).getItemCosts().get(0).getProductType().getJsonName()));

        assertThat(target.getItems().get(1).getDescriptionIdentifier(), is(source.getData().getItems().get(0).getDescriptionIdentifier()));
        assertThat(target.getItems().get(1).getKind(), is(EXPECTED_ITEM_COST));
        assertThat(target.getItems().get(1).getResourceKind(), is(source.getData().getItems().get(0).getKind()));
        assertFalse(target.getItems().get(1).getAvailablePaymentMethods().isEmpty());
        assertTrue(target.getItems().get(1).getAvailablePaymentMethods().contains(EXPECTED_AVAILABLE_PAYMENT_METHODS));
        assertThat(target.getItems().get(1).getAmount(), is(source.getData().getItems().get(0).getItemCosts().get(1).getCalculatedCost()));
        assertThat(target.getItems().get(1).getDescriptionValues().get(COMPANY_NUMBER_KEY), is(COMPANY_NUMBER));
        assertFalse(target.getItems().get(1).getClassOfPayment().isEmpty());
        assertTrue(target.getItems().get(1).getClassOfPayment().contains(EXPECTED_CLASS_OF_PAYMENT));
        assertThat(target.getItems().get(1).getProductType(), is(source.getData().getItems().get(0).getItemCosts().get(1).getProductType().getJsonName()));

        assertThat(target.getItems().get(2).getDescriptionIdentifier(), is(source.getData().getItems().get(0).getDescriptionIdentifier()));
        assertThat(target.getItems().get(2).getKind(), is(EXPECTED_ITEM_COST));
        assertThat(target.getItems().get(2).getResourceKind(), is(source.getData().getItems().get(0).getKind()));
        assertFalse(target.getItems().get(2).getAvailablePaymentMethods().isEmpty());
        assertTrue(target.getItems().get(2).getAvailablePaymentMethods().contains(EXPECTED_AVAILABLE_PAYMENT_METHODS));
        assertThat(target.getItems().get(2).getAmount(), is(source.getData().getItems().get(0).getItemCosts().get(2).getCalculatedCost()));
        assertThat(target.getItems().get(2).getDescriptionValues().get(COMPANY_NUMBER_KEY), is(COMPANY_NUMBER));
        assertFalse(target.getItems().get(2).getClassOfPayment().isEmpty());
        assertTrue(target.getItems().get(2).getClassOfPayment().contains(EXPECTED_CLASS_OF_PAYMENT));
        assertThat(target.getItems().get(2).getProductType(), is(source.getData().getItems().get(0).getItemCosts().get(2).getProductType().getJsonName()));
    }

    private void testLinks(Checkout source, PaymentDetailsDTO target){
        assertThat(target.getLinks().getResource(), is(source.getData().getLinks().getSelf()));
        assertThat(target.getLinks().getSelf(), is(source.getData().getLinks().getPayment()));
    }
}
