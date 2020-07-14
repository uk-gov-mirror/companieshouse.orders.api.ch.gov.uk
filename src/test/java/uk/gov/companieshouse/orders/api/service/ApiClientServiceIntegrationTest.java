package uk.gov.companieshouse.orders.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.api.model.order.item.CertificateItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyItemOptionsApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.mapper.ApiToItemMapper;
import uk.gov.companieshouse.orders.api.model.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFICATE_URI;
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFIED_COPY_URI;
import static uk.gov.companieshouse.orders.api.util.TestUtils.givenSdkIsConfigured;

/**
 * Partially integration tests the {@link ApiClientService} service.
 */
@SpringBootTest
@SpringJUnitConfig(ApiClientServiceIntegrationTest.Config.class)
@AutoConfigureWireMock(port = 0)
public class ApiClientServiceIntegrationTest {

    private static final String UNKNOWN_CERTIFICATE_URI = "/orderable/certificates/CRT-000000-000000";
    private static final String UNKNOWN_CERTIFIED_COPY_URI = "/orderable/certified-copies/CCD-000000-000000";
    private static final String SDK_ERROR_MESSAGE =
            "field private java.util.List uk.gov.companieshouse.api.error.ApiErrorResponse.errors";

    private static String CERTIFICATES_API_NOT_FOUND_ERROR_RESPONSE_BODY =
            "{\"status\":\"NOT_FOUND\",\"errors\":[\"certificate resource not found\"]}";
    private static String CERTIFIED_COPIES_API_NOT_FOUND_ERROR_RESPONSE_BODY =
            "{\"status\":\"NOT_FOUND\",\"errors\":[\"certified copy resource not found\"]}";

    private static final CertificateApi CERTIFICATE;
    private static final CertifiedCopyApi CERTIFIED_COPY;

    static {
        CERTIFICATE = new CertificateApi();
        CERTIFICATE.setItemOptions(new CertificateItemOptionsApi());
        CERTIFIED_COPY = new CertifiedCopyApi();
        CERTIFIED_COPY.setItemOptions(new CertifiedCopyItemOptionsApi());
    }

    @Configuration
    @ComponentScan(basePackageClasses = {ApiClientServiceIntegrationTest.class, ApiToItemMapper.class, Api.class})
    static class Config { }

    @Autowired
    private ApiClientService serviceUnderTest;

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BasketService basketService;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private OrderService orderService;

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    @Test
    @DisplayName("getItem() gets a certificate correctly")
    void getsCertificateCorrectly() throws Exception {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(get(urlEqualTo(VALID_CERTIFICATE_URI))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(CERTIFICATE))));

        // When
        final Item item = serviceUnderTest.getItem(VALID_CERTIFICATE_URI);

        // Then
        assertThat(item instanceof Certificate, is(true));
        assertThat(item.getItemOptions() instanceof CertificateItemOptions, is(true));
    }

    @Test
    @DisplayName("getItem() gets a certified copy correctly")
    void getsCertifiedCopyCorrectly() throws Exception {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(get(urlEqualTo(VALID_CERTIFIED_COPY_URI))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(CERTIFIED_COPY))));

        // When
        final Item item = serviceUnderTest.getItem(VALID_CERTIFIED_COPY_URI);

        // Then
        assertThat(item instanceof CertifiedCopy, is(true));
        assertThat(item.getItemOptions() instanceof CertifiedCopyItemOptions, is(true));
    }

    @Test
    @DisplayName("getItem() incorrectly throws an IllegalArgumentException for unknown certificate")
    void throwsIllegalArgumentExceptionForCertificateNotFound () {
        throwsIllegalArgumentExceptionForItemNotFound(UNKNOWN_CERTIFICATE_URI,
                                                      CERTIFICATES_API_NOT_FOUND_ERROR_RESPONSE_BODY);
    }

    @Test
    @DisplayName("getItem() incorrectly throws an IllegalArgumentException for unknown certified copy")
    void throwsIllegalArgumentExceptionForCertifiedCopyNotFound () {
        throwsIllegalArgumentExceptionForItemNotFound(UNKNOWN_CERTIFIED_COPY_URI,
                CERTIFIED_COPIES_API_NOT_FOUND_ERROR_RESPONSE_BODY);
    }

    @Test
    @DisplayName("getItem() throws ApiErrorResponseException for connection reset")
    void getItemThrowsApiErrorResponseExceptionConnectionReset() {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(get(urlEqualTo(VALID_CERTIFICATE_URI))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ApiErrorResponseException exception =
                Assertions.assertThrows(ApiErrorResponseException.class,
                        () -> serviceUnderTest.getItem(VALID_CERTIFICATE_URI));
        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR.value()));
        assertThat(exception.getStatusMessage(), is("Connection reset"));
    }

    /**
     * Reproduces incorrect behaviour currently seen in the SDK contract this service has with the APIs it attempts to
     * retrieve items from. See GCI-1262.
     * @param unknownItemUri the path identifying an item that does not actually exist (and which should be reported
     *                       as not found)
     * @param notFoundResponseBody the body of the response, equivalent to that actually returned by the API
     */
    private void throwsIllegalArgumentExceptionForItemNotFound(final String unknownItemUri,
                                                               final String notFoundResponseBody) {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(get(urlEqualTo(unknownItemUri))
                .willReturn(notFound()
                        .withHeader("Content-Type", "application/json")
                        .withBody(notFoundResponseBody)));

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> serviceUnderTest.getItem(unknownItemUri));
        assertThat(exception.getCause() instanceof IllegalArgumentException, is(true));
        assertThat(exception.getCause().getMessage(), is(SDK_ERROR_MESSAGE));
    }

}
