package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.item.request.PrivateItemURIPattern;
import uk.gov.companieshouse.api.handler.regex.URIValidator;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private final BasketService basketService;

    private final CheckoutRepository checkoutRepository;

    private final ApiClientService apiClientService;

    private final ApiToCertificateMapper apiToCertificateMapper;

    public CheckoutService(BasketService basketService, CheckoutRepository checkoutRepository, ApiClientService apiClientService, ApiToCertificateMapper apiToCertificateMapper) {
        this.basketService = basketService;
        this.checkoutRepository = checkoutRepository;
        this.apiClientService = apiClientService;
        this.apiToCertificateMapper = apiToCertificateMapper;
    }

    public Checkout createCheckout(Item item) {
        final LocalDateTime now = LocalDateTime.now();
        Checkout checkout = new Checkout();
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);
        checkout.getData().getItems().add(item);
        return checkoutRepository.save(checkout);
    }

}
