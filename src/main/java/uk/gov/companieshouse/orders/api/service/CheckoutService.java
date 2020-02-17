package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;

    public CheckoutService(CheckoutRepository checkoutRepository) {
        this.checkoutRepository = checkoutRepository;
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
