package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.item.request.PrivateItemURIPattern;
import uk.gov.companieshouse.api.handler.regex.URIValidator;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToItemMapper;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemStatus;

import java.io.IOException;

@Service
public class ApiClientService {

    private final ApiToItemMapper apiToItemMapper;

    private final Api apiClient;

    private static final UriTemplate GET_PAYMENT_URI =
            new UriTemplate("/payments/{paymentId}");

    public ApiClientService(ApiToItemMapper apiToItemMapper, Api apiClient) {
        this.apiToItemMapper = apiToItemMapper;
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

            // TODO GCI-1242 Do this properly - either by URI or by examination of JSON response.
            // TODO GCI-1242 Validation rejection could fall out of this?
            final Item item = URIValidator.validate(PrivateItemURIPattern.getCertificatesPattern(), itemUri) ?
                    apiToItemMapper.apiToCertificate((CertificateApi) baseItemApi) :
                    apiToItemMapper.apiToCertifiedCopy((CertifiedCopyApi) baseItemApi);

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
