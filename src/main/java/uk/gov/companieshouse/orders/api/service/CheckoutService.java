package uk.gov.companieshouse.orders.api.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
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

    public Checkout createCheckout(Item item, String userId, String email, DeliveryDetails deliveryDetails) {
        final LocalDateTime now = LocalDateTime.now();
        String objectId = new ObjectId().toString();

        Checkout checkout = new Checkout();
        checkout.setId(objectId);
        checkout.setUserId(userId);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);

        ActionedBy actionedBy = new ActionedBy();
        actionedBy.setId(userId);
        actionedBy.setEmail(email);
        checkout.getData().setCheckedOutBy(actionedBy);
        checkout.getData().setStatus(PaymentStatus.PENDING);
        checkout.getData().setEtag(etagGeneratorService.generateEtag());
        checkout.getData().setLinks(linksGeneratorService.generateCheckoutLinks(objectId));
        checkout.getData().getItems().add(item);
        checkout.getData().setReference(objectId);
        checkout.getData().setKind("order");
        checkout.getData().setDeliveryDetails(deliveryDetails);

        return checkoutRepository.save(checkout);
    }

    public Optional<Checkout> getCheckoutById(String id) {
        return checkoutRepository.findById(id);
    }
}
