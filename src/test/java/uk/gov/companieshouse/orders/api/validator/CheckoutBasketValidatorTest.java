package uk.gov.companieshouse.orders.api.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.service.ApiClientService;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_ADDITIONAL_COPY;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_SAME_DAY;

@ExtendWith(MockitoExtension.class)
public class CheckoutBasketValidatorTest {
    private static final String ITEM_URI = "/orderable/certificates/12345678";
    private static final String INVALID_ITEM_URI = "invalid_uri";
    private static final String COMPANY_NUMBER = "00000000";
    private static final List<ItemCosts> ITEM_COSTS =
            asList(new ItemCosts( "0", "50", "50", CERTIFICATE_SAME_DAY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY));
    private static final String POSTAGE_COST = "0";
    private static final String TOTAL_ITEM_COST = "70";
    @InjectMocks
    private CheckoutBasketValidator validatorUnderTest;
    @Mock
    private DeliveryDetailsValidator deliveryDetailsValidator;

    @Mock
    private ApiClientService apiClientService;

    @Test
    @DisplayName("getValidationErrors returns error for missing items")
    public void getValidationErrorsReportsMissingItems(){
        // Given
        Basket basket = setupBasketWithMissingItems();
        // When
        List<String> errors = validatorUnderTest.getValidationErrors(basket);
        // Then
        assertThat(errors.isEmpty(), is(false));
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(ErrorType.BASKET_ITEMS_MISSING.getValue()));
    }

    @Test
    @DisplayName("getValidationErrors returns error for invalid item")
    public void getValidationErrorsReportsInvalidItem() {
        // Given
        Basket basket = setupBasketWithInvalidItem();
        // When
        List<String> errors = validatorUnderTest.getValidationErrors(basket);
        // Then
        assertThat(errors.isEmpty(), is(false));
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(ErrorType.BASKET_ITEM_INVALID.getValue()));
    }

    @Test
    @DisplayName("getValidationErrors returns error for missing delivery details for postal delivery")
    public void getValidationErrorsReportsMissingDeliveryDetails() throws Exception {
        // Given
        Basket basket = setupBasketWithMissingDeliveryDetails();
        // When
        final Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        certificate.setPostalDelivery(true);
        when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);
        List<String> errors = validatorUnderTest.getValidationErrors(basket);
        // Then
        assertThat(errors.isEmpty(), is(false));
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(ErrorType.DELIVERY_DETAILS_MISSING.getValue()));
    }

    @Test
    @DisplayName("getValidationErrors returns error for incomplete address details for postal delivery")
    public void getValidationErrorsReportsIncompleteAddressDetails() throws Exception {
        // Given
        Basket basket = setupBasketWithMissingAddressDetails();
        // When
        final Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        certificate.setPostalDelivery(true);
        when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);
        List<String> errors = validatorUnderTest.getValidationErrors(basket);
        // Then
        assertThat(errors.isEmpty(), is(false));
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(ErrorType.DELIVERY_DETAILS_MISSING.getValue()));
    }

    private Basket setupBasketWithMissingItems(){
        Basket basket = new Basket();
        BasketData basketData = new BasketData();
        basket.setData(basketData);

        return basket;
    }

    private Basket setupBasketWithInvalidItem(){
        Basket basket = new Basket();
        BasketData basketData = new BasketData();
        Item basketItem = new Item();
        basketItem.setItemUri(INVALID_ITEM_URI);
        basketItem.setPostalDelivery(false);
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);

        return basket;
    }

    private Basket setupBasketWithMissingDeliveryDetails(){
        Basket basket = new Basket();
        BasketData basketData = new BasketData();
        Item basketItem = new Item();
        basketItem.setItemUri(ITEM_URI);
        basketItem.setPostalDelivery(true);
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);

        return basket;
    }

    private Basket setupBasketWithMissingAddressDetails(){
        Basket basket = new Basket();
        BasketData basketData = new BasketData();
        DeliveryDetails deliveryDetails = new DeliveryDetails();
        basketData.setDeliveryDetails(deliveryDetails);
        Item basketItem = new Item();
        basketItem.setItemUri(ITEM_URI);
        basketItem.setPostalDelivery(true);
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);

        return basket;
    }
}
