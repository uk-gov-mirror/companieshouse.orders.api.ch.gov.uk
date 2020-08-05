package uk.gov.companieshouse.orders.api.kafka;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.orders.OrderReceived;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
public class OrdersKafkaProducerIntegrationTest {
    @Autowired
    private OrdersKafkaProducer ordersKafkaProducer;

    @Autowired
    private OrdersMessageFactory ordersAvroSerializer;

    private String ORDERS_URI = "/orders/123456";

    private Message createTestMessage() throws SerializationException {
        OrderReceived orderReceived = new OrderReceived(ORDERS_URI);
        return ordersAvroSerializer.createMessage(orderReceived);
    }

    @Test
    @DisplayName("sendMessage successfully returns offset of sent message")
    void testSendMessage() throws SerializationException, ExecutionException, InterruptedException {
        // given an Order message is created
        Message message = createTestMessage();

        // when message is sent to Kafka topic
        // then offset of the sent message is received
        ordersKafkaProducer.sendMessage(message, (recordMetadata) -> {
            Assert.assertNotNull(recordMetadata.offset());
        });
    }
}
