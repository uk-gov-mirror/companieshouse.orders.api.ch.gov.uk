package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.item.request.PrivateItemURIPattern;
import uk.gov.companieshouse.api.handler.regex.URIValidator;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemStatus;

import java.io.IOException;

@Service
public class ApiClientService {

    private final ApiToCertificateMapper apiToCertificateMapper;

    private final Api apiClient;

    private static final UriTemplate GET_PAYMENT_URI =
            new UriTemplate("/payments/{paymentId}");

    public ApiClientService(ApiToCertificateMapper apiToCertificateMapper, Api apiClient) {
        this.apiToCertificateMapper = apiToCertificateMapper;
        this.apiClient = apiClient;
    }

    public Item getItem(String itemUri) throws Exception {
        if (URIValidator.validate(PrivateItemURIPattern.getCertificatesPattern(), itemUri) ||
            URIValidator.validate(PrivateItemURIPattern.getCertifiedCopyPattern(), itemUri)) {
            final BaseItemApi baseItemApi = apiClient
                    .getInternalApiClient()
                    .privateItemResourceHandler()
                    .getCertificate(itemUri)
                    .execute()
                    .getData();
            final Item item = apiToCertificateMapper.apiToItem(baseItemApi);
            item.setItemUri(itemUri);
            item.setStatus(ItemStatus.UNKNOWN);
            return item;
        } else {
            throw new ServiceException("Unrecognised uri pattern for "+itemUri);
        }
    }

    public PaymentApi getPaymentSummary(String passthroughHeader, String paymentId) throws IOException {

        try {
            String uri = GET_PAYMENT_URI.expand(paymentId).toString();
            return apiClient.getPublicApiClient(passthroughHeader).payment().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            throw new ServiceException("Error retrieving payments session for " + paymentId + ", Error response: " + ex.getStatusCode());
        } catch (URIValidationException ex) {
            throw new ServiceException("Invalid URI for payments session" + paymentId);
        }
    }

}
