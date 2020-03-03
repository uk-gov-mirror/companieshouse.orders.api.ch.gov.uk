package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class OrdersMessageProducer {
    private static CHKafkaProducer producer;

    public OrdersMessageProducer() {
        ProducerConfig config = new ProducerConfig();
        ProducerConfigHelper.assignBrokerAddresses(config);
        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(10);
        producer = new CHKafkaProducer(config);
    }

    public void sendMessage(byte[] data, String topic) throws ExecutionException, InterruptedException {
        Message message = new Message();
        message.setValue(data);
        message.setTopic(topic);
        message.setTimestamp((new Date()).getTime());
        producer.send(message);
    }
}
