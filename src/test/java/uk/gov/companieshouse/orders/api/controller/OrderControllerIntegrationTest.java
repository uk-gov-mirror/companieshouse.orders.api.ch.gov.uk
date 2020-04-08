package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_OAUTH2_TYPE_VALUE;

@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
public class OrderControllerIntegrationTest {
    private static final String ORDER_ID = "0001";
    private static final String ORDER_REFERENCE = "0001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper mapper;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    public void getOrderSuccessfully() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderData)));
    }

    @Test
    public void respondsWithNotFoundIfOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getOrderUnauthorisedIfUserDoesNotOwnOrder() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, WRONG_ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
