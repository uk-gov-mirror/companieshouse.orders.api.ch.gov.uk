package uk.gov.companieshouse.orders.api.kafka;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@Service
public class OrdersKafkaProducer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private CHKafkaProducer chKafkaProducer;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String brokerAddresses;

    /**
     * Sends message to Kafka topic
     * @param message message
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(final Message message) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LOGGER.info("Sending message to kafka", logMap);
        CompletableFuture.supplyAsync(() -> chKafkaProducer.sendAndReturnFuture(message))
                .thenAccept(recordMetadataFuture -> {
                    RecordMetadata recordMetadata = null;
                    try {
                        while(!recordMetadataFuture.isDone()){
                            Thread.sleep(300);
                        }
                        recordMetadata = recordMetadataFuture.get();
                        LoggingUtils.logIfNotNull(logMap, LoggingUtils.TOPIC, recordMetadata.topic());
                        LoggingUtils.logIfNotNull(logMap, LoggingUtils.OFFSET, recordMetadata.offset());
                    }
                    catch (InterruptedException | ExecutionException e) {
                        LoggingUtils.logIfNotNull(logMap, LoggingUtils.EXCEPTION, e.getMessage());
                        LOGGER.info("Failed to send message to kafka", logMap);
                    }
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.trace("Configuring CH Kafka producer");
        ProducerConfig config = new ProducerConfig();
        if (brokerAddresses != null && !brokerAddresses.isEmpty()) {
            config.setBrokerAddresses(brokerAddresses.split(","));
        } else {
            throw new ProducerConfigException("Broker addresses for kafka broker missing, check if environment variable KAFKA_BROKER_ADDR is configured. " +
                    "[Hint: The property 'kafka.broker.addresses' uses the value of this environment variable in live environments " +
                    "and that of 'spring.embedded.kafka.brokers' property in test.]");
        }

        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(10);
        chKafkaProducer = new CHKafkaProducer(config);
    }
}
