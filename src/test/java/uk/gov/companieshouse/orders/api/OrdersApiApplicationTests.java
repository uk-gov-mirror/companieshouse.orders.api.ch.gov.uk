package uk.gov.companieshouse.orders.api;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
class OrdersApiApplicationTests {

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();
	private final String ORDERS_DATABASE = "ORDERS_DATABASE";
	private final String MONGODB_URL = "MONGODB_URL";

	@Test
	public void checkEnvironmentVariablesAllPresentReturnsTrue() {
		environmentVariables.set(ORDERS_DATABASE, ORDERS_DATABASE);
		environmentVariables.set(MONGODB_URL, MONGODB_URL);

		boolean present = OrdersApiApplication.checkEnvironmentVariables();
		assertTrue(present);
		environmentVariables.clear(ORDERS_DATABASE, MONGODB_URL);
	}

	@Test
	public void checkEnvironmentVariablesMissingMongodbUrlReturnsFalse() {
		environmentVariables.set(ORDERS_DATABASE, ORDERS_DATABASE);
		boolean present = OrdersApiApplication.checkEnvironmentVariables();
		assertFalse(present);
		environmentVariables.clear(ORDERS_DATABASE, MONGODB_URL);
	}

	@Test
	public void checkEnvironmentVariablesMissingOrdersDatabaseReturnFalse() {
		environmentVariables.set(MONGODB_URL, MONGODB_URL);
		boolean present = OrdersApiApplication.checkEnvironmentVariables();
		assertFalse(present);
		environmentVariables.clear(ORDERS_DATABASE, MONGODB_URL);
	}

	@Test
	void contextLoads() {
	}

}
