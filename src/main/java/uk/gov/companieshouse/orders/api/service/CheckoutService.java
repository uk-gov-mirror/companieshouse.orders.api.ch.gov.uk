package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.mapper.ApiToCertificateMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

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

    public Checkout createCheckout(Item item, String userId) {
        final LocalDateTime now = LocalDateTime.now();
        Checkout checkout = new Checkout();
        checkout.setUserId(userId);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);
        checkout.getData().getItems().add(item);
        return checkoutRepository.save(checkout);
    }

}
