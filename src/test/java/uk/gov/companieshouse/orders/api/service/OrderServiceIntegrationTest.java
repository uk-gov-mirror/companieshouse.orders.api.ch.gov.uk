package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.test.context.EmbeddedKafka;
import uk.gov.companieshouse.orders.api.controller.GlobalExceptionHandler;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka
public class OrderServiceIntegrationTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private CheckoutToOrderMapper checkoutToOrderMapper;
    @Mock
    private LinksGeneratorService linksGeneratorService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private Checkout checkout;
    @Mock
    private DataAccessException dataAccessException;
    @Mock
    private Order order;
    @Mock
    private OrderData orderData;
    @Mock
    private OrderReceivedMessageProducer orderReceivedMessageProducer;
    @Mock
    private GlobalExceptionHandler exceptionHandlerMock;

    @Test
    @DisplayName("Order creation throws MongoOperationException when MongoDB request fails")
    void testMongoOperationException() {
        // given checkout item(s) have been paid for
        // when order being created from checkout
        // and when mongo db save operation fails with DataAccessException
        Mockito.when(checkoutToOrderMapper.checkoutToOrder(checkout)).thenReturn(order);
        Mockito.when(order.getData()).thenReturn(orderData);
        Mockito.doThrow(dataAccessException).when(orderRepository).save(order);

        // then global exception handler handles the exception
        Assertions.assertThrows(MongoOperationException.class, () -> {
            orderService.createOrder(checkout);
            verify(exceptionHandlerMock, times(1)).handleMongoOperationException(Mockito.any());
        });
    }
}
