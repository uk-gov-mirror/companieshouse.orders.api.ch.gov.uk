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
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.service.ApiClientService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CheckoutBasketValidatorTest {
    private static final String INVALID_ITEM_URI = "invalid_uri";
    @InjectMocks
    private CheckoutBasketValidator validatorUnderTest;

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
    public void getValidationErrorsReportsInvalidItem() throws Exception {
        // Given
        Basket basket = setupBasketWithInvalidItem();
        // When
        when(apiClientService.getItem(anyString())).thenThrow(Exception.class);
        List<String> errors = validatorUnderTest.getValidationErrors(basket);
        // Then
        assertThat(errors.isEmpty(), is(false));
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(ErrorType.BASKET_ITEM_INVALID.getValue()));
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
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);

        return basket;
    }
}
