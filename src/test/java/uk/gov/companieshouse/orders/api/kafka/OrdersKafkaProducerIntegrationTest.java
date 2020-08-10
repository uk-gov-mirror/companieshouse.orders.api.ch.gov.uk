package uk.gov.companieshouse.orders.api.kafka;

import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.controller.GlobalExceptionHandler;
import uk.gov.companieshouse.orders.api.exception.KafkaMessagingException;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
public class OrdersKafkaProducerIntegrationTest {
    @Autowired
    private OrdersKafkaProducer ordersKafkaProducer;

    @Spy
    @InjectMocks
    private OrderReceivedMessageProducer orderReceivedMessageProducer;
    @Mock
    private OrdersKafkaProducer ordersKafkaProducerMock;
    @Mock
    private GlobalExceptionHandler exceptionHandlerMock;
    @Mock
    private ExecutionException executionExceptionMock;
    @Mock
    private InterruptedException interruptedExceptionMock;
    @Mock
    private SerializationException serializationExceptionMock;
    @Mock
    private OrdersMessageFactory ordersMessageFactoryMock;

    @Autowired
    private OrdersMessageFactory ordersAvroSerializer;
    private String ORDER_ID = "123456";
    private String ORDERS_URI = "/orders/" + ORDER_ID;
    private OrderReceived orderReceived = new OrderReceived(ORDERS_URI);

    private Message createTestMessage() throws SerializationException {
        return ordersAvroSerializer.createMessage(orderReceived);
    }

    @Test
    @DisplayName("sendMessage successfully returns offset of sent message")
    void testSendMessage() throws SerializationException, ExecutionException, InterruptedException {
        // given a Kafka message is requested to be sent
        // and an Order message is created
        Message message = createTestMessage();

        // when message is sent to Kafka topic
        // then offset of the sent message is received
        ordersKafkaProducer.sendMessage(ORDER_ID, message, (recordMetadata) -> {
            Assert.assertNotNull(recordMetadata.offset());
        });
    }

    @Test
    @DisplayName("sendMessage throws KafkaMessagingException when ExecutionException is caught.")
    void testExecutionExceptionHandling() throws SerializationException, ExecutionException, InterruptedException {
        // given a Kafka message is requested to be sent
        // and an Order message is created
        Message message = createTestMessage();
        when(ordersMessageFactoryMock.createMessage(any())).thenReturn(message);

        // when ExecutionException is thrown while sending Kafka message
        // then KafkaMessagingException is thrown and handled by GlobalExceptionHandler advice
        doThrow(executionExceptionMock).when(ordersKafkaProducerMock).sendMessage(ORDER_ID, message, recordMetadata -> {
            assertThrows(KafkaMessagingException.class, () -> orderReceivedMessageProducer.sendMessage(ORDER_ID, orderReceived));
            verify(exceptionHandlerMock, times(1)).handleKafkaMessagingException(Mockito.any());
        });
        orderReceivedMessageProducer.sendMessage(ORDER_ID, orderReceived);
    }

    @Test
    @DisplayName("sendMessage throws KafkaMessagingException when InterruptedException is caught.")
    void testInterruptedExceptionHandling() throws SerializationException, ExecutionException, InterruptedException {
        // given a Kafka message is requested to be sent
        // and an Order message is created
        Message message = createTestMessage();
        when(ordersMessageFactoryMock.createMessage(any())).thenReturn(message);

        // when InterruptedException is thrown while sending Kafka message
        // then KafkaMessagingException is thrown and handled by GlobalExceptionHandler advice
        doThrow(interruptedExceptionMock).when(ordersKafkaProducerMock).sendMessage(ORDER_ID, message, recordMetadata -> {
            assertThrows(KafkaMessagingException.class, () -> orderReceivedMessageProducer.sendMessage(ORDER_ID, orderReceived));
            verify(exceptionHandlerMock, times(1)).handleKafkaMessagingException(Mockito.any());
        });
        orderReceivedMessageProducer.sendMessage(ORDER_ID, orderReceived);
    }

    @Test
    @DisplayName("sendMessage throws KafkaMessagingException when message creation fails.")
    void testMessageCreationFailure() throws SerializationException {
        // given a Kafka message is requested to be sent
        // when Order message creation fails with SerializationException
        when(ordersMessageFactoryMock.createMessage(any())).thenThrow(serializationExceptionMock);

        // then KafkaMessagingException is thrown and handled by GlobalExceptionHandler advice
        assertThrows(KafkaMessagingException.class, () -> {
            orderReceivedMessageProducer.sendMessage(ORDER_ID, orderReceived);
            verify(exceptionHandlerMock, times(1)).handleKafkaMessagingException(Mockito.any());
        });
    }
}
