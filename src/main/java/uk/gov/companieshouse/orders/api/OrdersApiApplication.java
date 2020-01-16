package uk.gov.companieshouse.orders.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrdersApiApplication {

	public static final String APPLICATION_NAMESPACE = "orders.api.ch.gov.uk";

	public static void main(String[] args) {
		SpringApplication.run(OrdersApiApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
