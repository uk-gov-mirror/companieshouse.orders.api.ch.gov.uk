package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.order.item.PrivateItemResourceHandler;
import uk.gov.companieshouse.api.handler.order.item.request.CertificateGet;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Item;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiClientServiceTest {
    private static final String VALID_CERTIFICATE_URI = "/orderable/certificates/CHS00001";
    private static final String INVALID_CERTIFICATE_URI = "/test/test/CHS00001";
    private static final String COMPANY_NUMBER = "00006400";

    @InjectMocks
    private ApiClientService serviceUnderTest;

    @Mock
    private ApiToCertificateMapper apiToCertificateMapper;

    @Mock
    private InternalApiClient mockInternalApiClient;

    @Mock
    private ApiClient apiClient;

    @Mock
    private PrivateItemResourceHandler privateItemResourceHandler;

    @Mock
    private CertificateGet certificateGet;

    @Mock
    private ApiResponse<CertificateApi> certificateApiResponse;

    @Test
    public void shouldGetCertificateItemIfUriIsValid() throws Exception {
        when(apiClient.getInternalApiClient()).thenReturn(mockInternalApiClient);
        when(mockInternalApiClient.privateItemResourceHandler()).thenReturn(privateItemResourceHandler);
        when(privateItemResourceHandler.getCertificate(VALID_CERTIFICATE_URI)).thenReturn(certificateGet);
        when(certificateGet.execute()).thenReturn(certificateApiResponse);

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(apiToCertificateMapper.apiToCertificate(certificateApiResponse.getData())).thenReturn(certificate);

        Item item = serviceUnderTest.getItem(VALID_CERTIFICATE_URI);

        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
    }

    @Test
    public void shouldThrowExceptionIfCertificateItemUriIsInvalid() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Item item = serviceUnderTest.getItem(INVALID_CERTIFICATE_URI);
        });
        assertEquals("Unrecognised uri pattern for "+INVALID_CERTIFICATE_URI, exception.getMessage());
    }
}
