package uk.gov.companieshouse.orders.api.controller;

import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.mapper.BasketItemMapper;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;

@ExtendWith(MockitoExtension.class)
public class BasketControllerTest {
    @InjectMocks
    BasketController controllerUnderTest;

    @Mock
    BasketItemMapper mapper;

    @Mock
    BasketItemDTO basketItemDto;

    @Mock
    BasketItem basketItem;

    @Test
    @DisplayName("Add item to basket successfully adds item to basket")
    public void addItemReturnsSuccessfullyAddsBasketItem(){
        when(mapper.basketItemDTOtoBasketItem(basketItemDto)).thenReturn(basketItem);

        ResponseEntity<BasketItem> basketItem = controllerUnderTest.addItemToBasket(basketItemDto, TOKEN_REQUEST_ID_VALUE);
        assertThat(basketItem.getStatusCodeValue(), is(200));
    }
}
