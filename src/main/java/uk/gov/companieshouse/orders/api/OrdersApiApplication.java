package uk.gov.companieshouse.orders.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrdersApiApplication {

	public static final String APPLICATION_NAMESPACE = "orders.api.ch.gov.uk";

	public static void main(String[] args) {
		SpringApplication.run(OrdersApiApplication.class, args);
	}

}
