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
import uk.gov.companieshouse.api.handler.order.item.request.CertificateGet;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemStatus;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiClientServiceTest {
    private static final String PAYMENT_ID = "987654321";
    private static final String AMOUNT = "500";
    private static final String DESCRIPTION = "description";
    private static final String VALID_CERTIFICATE_URI = "/orderable/certificates/CHS00001";
    private static final String VALID_PAYMENT_URI = "/payments/" + PAYMENT_ID;
    private static final String INVALID_CERTIFICATE_URI = "/test/test/CHS00001";
    private static final String COMPANY_NUMBER = "00006400";
    private static final String PASS_THROUGH_HEADER = "passThroughHeader";

    @InjectMocks
    private ApiClientService serviceUnderTest;

    @Mock
    private ApiToCertificateMapper apiToCertificateMapper;

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
    private CertificateGet certificateGet;

    @Mock
    private ApiResponse<CertificateApi> certificateApiResponse;

    @Test
    public void shouldGetCertificateItemIfUriIsValid() throws Exception {
        when(api.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getCertificate(VALID_CERTIFICATE_URI)).thenReturn(certificateGet);
        when(certificateGet.execute()).thenReturn(certificateApiResponse);

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(apiToCertificateMapper.apiToCertificate(certificateApiResponse.getData())).thenReturn(certificate);

        Item item = serviceUnderTest.getItem(VALID_CERTIFICATE_URI);

        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        assertEquals(VALID_CERTIFICATE_URI, item.getItemUri());
        assertEquals(ItemStatus.UNKNOWN, item.getStatus());
    }

    @Test
    public void shouldThrowExceptionIfCertificateItemUriIsInvalid() throws ServiceException {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            Item item = serviceUnderTest.getItem(INVALID_CERTIFICATE_URI);
        });
        assertEquals("Unrecognised uri pattern for "+INVALID_CERTIFICATE_URI, exception.getMessage());
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
