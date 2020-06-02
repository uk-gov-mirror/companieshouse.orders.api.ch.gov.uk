package uk.gov.companieshouse.orders.api;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrdersApiApplication {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(OrdersApiApplication.class, args);
    }

}
