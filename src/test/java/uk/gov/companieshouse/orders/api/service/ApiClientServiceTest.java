package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.item.PrivateItemResourceHandler;
import uk.gov.companieshouse.api.handler.order.item.request.ItemGet;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToItemMapper;
import uk.gov.companieshouse.orders.api.model.*;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@ExtendWith(MockitoExtension.class)
public class ApiClientServiceTest {
    private static final String PAYMENT_ID = "987654321";
    private static final String AMOUNT = "500";
    private static final String DESCRIPTION = "description";
    private static final String VALID_PAYMENT_URI = "/payments/" + PAYMENT_ID;
    private static final String INVALID_CERTIFICATE_URI = "/test/test/CHS00001";
    private static final String COMPANY_NUMBER = "00006400";
    private static final String PASS_THROUGH_HEADER = "passThroughHeader";

    @InjectMocks
    private ApiClientService serviceUnderTest;

    @Mock
    private ApiToItemMapper apiToItemMapper;

    @Mock
    private InternalApiClient mockInternalApiClient;

    @Mock
    private ApiClient mockApiClient;

    @Mock
    private Api api;

    @Mock
    private PrivateItemResourceHandler privateItemResourceHandler;

    @Mock
    private PaymentResourceHandler paymentResourceHandler;

    @Mock
    private PaymentGet paymentGet;

    @Mock
    private ItemGet itemGet;

    @Mock
    private ApiResponse<CertificateApi> certificateApiResponse;

    @Mock
    private ApiResponse<CertifiedCopyApi> certifiedCopyApiResponse;

    @Test
    public void shouldGetCertificateItemIfUriIsValid() throws Exception {
        when(api.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getItem(VALID_CERTIFICATE_URI)).thenReturn(itemGet);
        doReturn(certificateApiResponse).when(itemGet).execute();

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(apiToItemMapper.apiToCertificate(certificateApiResponse.getData())).thenReturn(certificate);

        Item item = serviceUnderTest.getItem(VALID_CERTIFICATE_URI);

        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        assertEquals(VALID_CERTIFICATE_URI, item.getItemUri());
        assertEquals(ItemStatus.UNKNOWN, item.getStatus());
    }

    @Test
    public void shouldGetCertificateItemOptions() throws Exception {

        // Given
        when(api.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getItem(VALID_CERTIFICATE_URI)).thenReturn(itemGet);
        doReturn(certificateApiResponse).when(itemGet).execute();
        final Certificate certificate = new Certificate();
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        certificate.setItemOptions(options);
        when(apiToItemMapper.apiToCertificate(certificateApiResponse.getData())).thenReturn(certificate);

        // When
        final Item item = serviceUnderTest.getItem(VALID_CERTIFICATE_URI);

        // Then
        assertEquals(CertificateItemOptions.class, item.getItemOptions().getClass());
        assertThat(((CertificateItemOptions) item.getItemOptions()).getCertificateType(),
                is(INCORPORATION_WITH_ALL_NAME_CHANGES));
    }

    @Test
    public void shouldGetCertifiedCopyItemOptions() throws Exception {

        // Given
        when(api.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getItem(VALID_CERTIFIED_COPY_URI)).thenReturn(itemGet);
        doReturn(certifiedCopyApiResponse).when(itemGet).execute();
        final CertifiedCopy copy = new CertifiedCopy();
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(DOCUMENT));
        copy.setItemOptions(options);
        when(apiToItemMapper.apiToCertifiedCopy(certifiedCopyApiResponse.getData())).thenReturn(copy);

        // When
        final Item item = serviceUnderTest.getItem(VALID_CERTIFIED_COPY_URI);

        // Then
        assertEquals(CertifiedCopyItemOptions.class, item.getItemOptions().getClass());
        assertThat(((CertifiedCopyItemOptions) item.getItemOptions()).getFilingHistoryDocuments().size(), is(1));
        assertThat(((CertifiedCopyItemOptions) item.getItemOptions()).getFilingHistoryDocuments().get(0), is(DOCUMENT));
    }

    @Test
    public void shouldThrowExceptionIfCertificateItemUriIsInvalid() throws Exception {

        // Given
        when(api.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getItem(INVALID_CERTIFICATE_URI)).thenReturn(itemGet);
        when(itemGet.execute()).thenThrow(new URIValidationException("Test exception"));

        // When and then
        ServiceException exception =
                assertThrows(ServiceException.class, () -> serviceUnderTest.getItem(INVALID_CERTIFICATE_URI));
        assertEquals("Unrecognised uri pattern for " + INVALID_CERTIFICATE_URI, exception.getMessage());
    }

    @Test
    public void getPaymentSessionSuccess() throws IOException, URIValidationException {

        PaymentApi paymentSession = new PaymentApi();
        paymentSession.setAmount("500");
        paymentSession.setDescription("description");

        when(api.getPublicApiClient(PASS_THROUGH_HEADER)).thenReturn(mockApiClient);
        when(mockApiClient.payment()).thenReturn(paymentResourceHandler);
        when(mockApiClient.payment().get(VALID_PAYMENT_URI)).thenReturn(paymentGet);
        when(paymentGet.execute()).thenReturn(new ApiResponse<>(201, null, paymentSession));

        PaymentApi returnedPaymentSession = serviceUnderTest.getPaymentSummary(PASS_THROUGH_HEADER, PAYMENT_ID);

        assertEquals(AMOUNT, returnedPaymentSession.getAmount());
        assertEquals(DESCRIPTION, returnedPaymentSession.getDescription());
    }

    @Test
    @DisplayName("Get Payment Session - URI Validation Exception Thrown")
    void getPaymentSessionUriValidationExceptionThrown() throws IOException, URIValidationException {

        when(api.getPublicApiClient(PASS_THROUGH_HEADER)).thenReturn(mockApiClient);
        when(mockApiClient.payment()).thenReturn(paymentResourceHandler);
        when(mockApiClient.payment().get(VALID_PAYMENT_URI)).thenReturn(paymentGet);
        when(paymentGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () -> serviceUnderTest.getPaymentSummary(PASS_THROUGH_HEADER, PAYMENT_ID));
    }

    @Test
    @DisplayName("Get Payment Session - API Error Response Exception Thrown")
    void getPaymentSessionApiResponseExceptionThrown() throws IOException, URIValidationException {

        when(api.getPublicApiClient(PASS_THROUGH_HEADER)).thenReturn(mockApiClient);
        when(mockApiClient.payment()).thenReturn(paymentResourceHandler);
        when(mockApiClient.payment().get(VALID_PAYMENT_URI)).thenReturn(paymentGet);
        when(paymentGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () -> serviceUnderTest.getPaymentSummary(PASS_THROUGH_HEADER, PAYMENT_ID));
    }
}
