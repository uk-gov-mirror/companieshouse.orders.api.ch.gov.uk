package uk.gov.companieshouse.orders.api.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.mapper.BasketItemMapper;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class BasketControllerTest {
    @InjectMocks
    BasketController controllerUnderTest;

    @Mock
    BasketItemMapper mapper;


    @Mock
    BasketItem basketItem;


}
