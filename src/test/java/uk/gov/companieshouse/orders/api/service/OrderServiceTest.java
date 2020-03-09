package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * Unit tests the {@link OrderService} class.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String ORDER_ID = "0001";

    @InjectMocks
    private OrderService serviceUnderTest;

    @Mock
    private Checkout checkout;

    @Mock
    private CheckoutToOrderMapper mapper;

    @Mock
    private OrderRepository repository;

    @Mock
    private LinksGeneratorService linksGeneratorService;

    @Test
    void createOrderCreatesOrder() {
        // Given
        final Order order = new Order();
        order.setId(ORDER_ID);
        when(mapper.checkoutToOrder(checkout)).thenReturn(order);
        when(repository.save(order)).thenReturn(order);

        // When and then
        assertThat(serviceUnderTest.createOrder(checkout), is(order));
        verify(linksGeneratorService, times(1)).generateOrderLinks(ORDER_ID);

    }
}
