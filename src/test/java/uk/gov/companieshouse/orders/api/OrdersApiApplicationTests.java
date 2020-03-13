package uk.gov.companieshouse.orders.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
				properties = "kafka.broker.addresses=chs-kafka:8081")
class OrdersApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
