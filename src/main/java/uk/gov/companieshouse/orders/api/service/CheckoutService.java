package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.item.request.PrivateItemURIPattern;
import uk.gov.companieshouse.api.handler.regex.URIValidator;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private final BasketService basketService;

    private final CheckoutRepository repository;

    private final ApiClientService apiClientService;

    private final ApiToCertificateMapper apiToCertificateMapper;

    public CheckoutService(BasketService basketService, CheckoutRepository repository, ApiClientService apiClientService, ApiToCertificateMapper apiToCertificateMapper) {
        this.basketService = basketService;
        this.repository = repository;
        this.apiClientService = apiClientService;
        this.apiToCertificateMapper = apiToCertificateMapper;
    }

    public void createCheckout(Basket basket) {

        if(basket != null) {
            System.out.println("not null");
            BasketData basketData = basket.getData();
            if(!basketData.getItems().isEmpty()) {
                // use private sdk to get certificate from items api
                // for mapping have a look at APIToOffenceMapper
                try {
                    CertificateApi b = getCertificate(basketData.getItems().get(0).getItemUri());
                    System.out.println(b);
                    System.out.println("Company Name");
                    System.out.println(b.getCompanyName());
                    Certificate certificate = apiToCertificateMapper.apiToCertificate(b);
                    System.out.println(certificate.getCompanyName());

                    Checkout checkout = new Checkout();
                    checkout.setCreatedAt(LocalDateTime.now());
                    checkout.getData().getItems().add(certificate);
                    repository.save(checkout);
                } catch (Exception exception) {
                    System.out.println(exception);
                    // return 409
                }


            } else {
                // throw error
            }
        }
    }

    public CertificateApi getCertificate(String itemUri) throws ApiErrorResponseException, URIValidationException {
        System.out.println(itemUri);
        if (URIValidator.validate(PrivateItemURIPattern.getCertificatesPattern(), itemUri)) {
            ApiResponse<CertificateApi> certificateApi = apiClientService.getInternalApiClient().privateItemResourceHandler().getCertificate(itemUri).execute();
            return certificateApi.getData();
        } else {
            // throw error
            return null;
        }

    }

}
