package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.order.item.request.PrivateItemURIPattern;
import uk.gov.companieshouse.api.handler.regex.URIValidator;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.orders.api.client.ApiClient;
import uk.gov.companieshouse.orders.api.exception.ServiceException;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Item;

@Service
public class ApiClientService {

    private final ApiToCertificateMapper apiToCertificateMapper;

    private final ApiClient apiClient;

    public ApiClientService(ApiToCertificateMapper apiToCertificateMapper, ApiClient apiClient) {
        this.apiToCertificateMapper = apiToCertificateMapper;
        this.apiClient = apiClient;
    }

    public Item getItem(String itemUri) throws Exception {
        if (URIValidator.validate(PrivateItemURIPattern.getCertificatesPattern(), itemUri)) {
            CertificateApi certificateApi = apiClient.getInternalApiClient().privateItemResourceHandler().getCertificate(itemUri).execute().getData();
            Item certificate = apiToCertificateMapper.apiToCertificate(certificateApi);
            certificate.setItemUri(itemUri);
            return certificate;
        } else {
            throw new ServiceException("Unrecognised uri pattern for "+itemUri);
        }
    }

}
