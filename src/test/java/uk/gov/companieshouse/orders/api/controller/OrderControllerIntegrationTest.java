package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFIED_COPY_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.DOCUMENT;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_OAUTH2_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_PERMISSION_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.WRONG_ERIC_IDENTITY_VALUE;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
class OrderControllerIntegrationTest {
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
    void getOrderSuccessfully() throws Exception {
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
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderData)));
    }

    @Test
    @DisplayName("Get order responds with correctly populated certificate item options")
    void getOrderCertificateItemOptionsCorrectly() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        final Certificate certificate = new Certificate();
        certificate.setKind(CERTIFICATE_KIND);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        certificate.setItemOptions(options);
        orderData.setItems(singletonList(certificate));
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.certificate_type",
                        is(INCORPORATION_WITH_ALL_NAME_CHANGES.getJsonName())));
    }

    @Test
    @DisplayName("Get order responds with correctly populated certified copy item options")
    void getOrderCertifiedCopyItemOptionsCorrectly() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        final CertifiedCopy copy = new CertifiedCopy();
        copy.setKind(CERTIFIED_COPY_KIND);
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(DOCUMENT));
        copy.setItemOptions(options);
        orderData.setItems(singletonList(copy));
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_date",
                        is(DOCUMENT.getFilingHistoryDate())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_description",
                        is(DOCUMENT.getFilingHistoryDescription())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_id",
                        is(DOCUMENT.getFilingHistoryId())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_type",
                        is(DOCUMENT.getFilingHistoryType())));
    }

    @Test
   void respondsWithNotFoundIfOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderUnauthorisedIfUserDoesNotOwnOrder() throws Exception {
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
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
