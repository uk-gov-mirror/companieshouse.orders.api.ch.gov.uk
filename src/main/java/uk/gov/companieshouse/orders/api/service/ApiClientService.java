package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.client.Api;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToItemMapper;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
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

    /**
     * Gets an item from a remote API by sending it a get item HTTP GET request.
     * @param passthroughHeader the eric access token header to pass through as auth for api to api communication
     * @param itemUri the URI path representing the item (and implicitly the type of item) sought
     * @return the item (either a {@link Certificate}, or a {@link CertifiedCopy})
     * @throws ApiErrorResponseException should there be a 4xx or 5xx response from the API
     * @throws IOException
     */
    public Item getItem(String passthroughHeader, String itemUri) throws ApiErrorResponseException, IOException {
        final BaseItemApi baseItemApi;
        try {
            baseItemApi = apiClient
                    .getInternalApiClient(passthroughHeader)
                    .privateItemResourceHandler()
                    .getItem(itemUri)
                    .execute()
                    .getData();
        } catch (URIValidationException uve) {
            throw new ServiceException("Unrecognised uri pattern for " + itemUri);
        }

        final Item item = apiToItemMapper.apiToItem(baseItemApi);
        item.setItemUri(itemUri);
        item.setStatus(ItemStatus.UNKNOWN);
        return item;
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
