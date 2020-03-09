package uk.gov.companieshouse.orders.api.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final EtagGeneratorService etagGeneratorService;
    private final LinksGeneratorService linksGeneratorService;

    public CheckoutService(CheckoutRepository checkoutRepository, EtagGeneratorService etagGeneratorService, LinksGeneratorService linksGeneratorService) {
        this.checkoutRepository = checkoutRepository;
        this.etagGeneratorService = etagGeneratorService;
        this.linksGeneratorService = linksGeneratorService;
    }

    public Checkout createCheckout(Item item, String userId) {
        final LocalDateTime now = LocalDateTime.now();
        String objectId = new ObjectId().toString();

        Checkout checkout = new Checkout();
        checkout.setId(objectId);
        checkout.setUserId(userId);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);
        checkout.getData().setStatus(PaymentStatus.PENDING);
        checkout.getData().setEtag(etagGeneratorService.generateEtag());
        checkout.getData().setLinks(linksGeneratorService.generateCheckoutLinks(objectId));
        checkout.getData().getItems().add(item);
        return checkoutRepository.save(checkout);
    }

    public Optional<Checkout> getCheckoutById(String id) {
        return checkoutRepository.findById(id);
    }

}
