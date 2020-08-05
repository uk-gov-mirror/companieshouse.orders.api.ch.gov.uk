package uk.gov.companieshouse.orders.api.kafka;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.orders.OrderReceived;

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
    void testSendMessage() throws SerializationException {
        // given an Order message is created
        Message message = createTestMessage();

        // when message is sent to Kafka topic
        // then offset of the sent message is received
        ordersKafkaProducer.sendMessage(message, (recordMetadata) -> {
            Assert.assertNotNull(recordMetadata.offset());
        });
    }
}
