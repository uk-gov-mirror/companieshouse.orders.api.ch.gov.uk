package uk.gov.companieshouse.orders.api.service;

import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.mapper.ApiToItemMapper;
import uk.gov.companieshouse.orders.api.model.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFICATE_URI;
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFIED_COPY_URI;

/**
 * Partially integration tests the {@link ApiClientService} service.
 */
@SpringBootTest
@SpringJUnitConfig(ApiClientServiceIntegrationTest.Config.class)
public class ApiClientServiceIntegrationTest {

    private static final String UNKNOWN_CERTIFICATE_URI = "/orderable/certificates/CRT-000000-000000";
    private static final String UNKNOWN_CERTIFIED_COPY_URI = "/orderable/certified-copies/CCD-000000-000000";
    private static final String SDK_ERROR_MESSAGE =
            "field private java.util.List uk.gov.companieshouse.api.error.ApiErrorResponse.errors";

    @Configuration
    @ComponentScan(basePackageClasses = {ApiClientServiceIntegrationTest.class, ApiToItemMapper.class, Api.class})
    static class Config { }

    @Autowired
    private ApiClientService serviceUnderTest;

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
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock

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
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock

        // When
        final Item item = serviceUnderTest.getItem(VALID_CERTIFIED_COPY_URI);

        // Then
        assertThat(item instanceof CertifiedCopy, is(true));
        assertThat(item.getItemOptions() instanceof CertifiedCopyItemOptions, is(true));
    }

    @Test
    @DisplayName("getItem() incorrectly throws an IllegalArgumentException for unknown certificate")
    void throwsIllegalArgumentExceptionForCertificateNotFound () {
        throwsIllegalArgumentExceptionForItemNotFound(UNKNOWN_CERTIFICATE_URI);
    }

    @Test
    @DisplayName("getItem() incorrectly throws an IllegalArgumentException for unknown certified copy")
    void throwsIllegalArgumentExceptionForCertifiedCopyNotFound () {
        throwsIllegalArgumentExceptionForItemNotFound(UNKNOWN_CERTIFIED_COPY_URI);
    }


    /**
     * Reproduces incorrect behaviour currently seen in the SDK contract this service has with the APIs it attempts to
     * retrieve items from. See GCI-1262.
     * @param unknownItemUri the path identifying an item that does not actually exist (and which should be reported
     *                       as not found)
     */
    private void throwsIllegalArgumentExceptionForItemNotFound(final String unknownItemUri) {

        // Given
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://api.chs-dev.internal:4001"); // TODO GCI-1022 WireMock

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> serviceUnderTest.getItem(UNKNOWN_CERTIFICATE_URI));
        assertThat(exception.getCause() instanceof IllegalArgumentException, is(true));
        assertThat(exception.getCause().getMessage(), is(SDK_ERROR_MESSAGE));
    }

}
