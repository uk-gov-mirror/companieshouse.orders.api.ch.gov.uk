package uk.gov.companieshouse.orders.api.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.CheckoutHelper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final EtagGeneratorService etagGeneratorService;
    private final LinksGeneratorService linksGeneratorService;
    private final CheckoutHelper checkoutHelper;

    public CheckoutService(CheckoutRepository checkoutRepository,
                           EtagGeneratorService etagGeneratorService,
                           LinksGeneratorService linksGeneratorService,
                           CheckoutHelper checkoutHelper) {
        this.checkoutRepository = checkoutRepository;
        this.etagGeneratorService = etagGeneratorService;
        this.linksGeneratorService = linksGeneratorService;
        this.checkoutHelper = checkoutHelper;
    }

    private String autoGenerateId() {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[4];
        random.nextBytes(values);
        String rand = String.format("%04d", random.nextInt(9999));
        String time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        String rawId = rand + time;
        String[] tranId = rawId.split("(?<=\\G.{6})");
        return "ORD-" + String.join("-", tranId);
    }

    public Checkout createCheckout(Item item, String userId, String email, DeliveryDetails deliveryDetails) {
        final LocalDateTime now = LocalDateTime.now();
        String checkoutId = autoGenerateId();

        Checkout checkout = new Checkout();
        checkout.setId(checkoutId);
        checkout.setUserId(userId);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);

        ActionedBy actionedBy = new ActionedBy();
        actionedBy.setId(userId);
        actionedBy.setEmail(email);
        checkout.getData().setCheckedOutBy(actionedBy);
        checkout.getData().setStatus(PaymentStatus.PENDING);
        checkout.getData().setEtag(etagGeneratorService.generateEtag());
        checkout.getData().setLinks(linksGeneratorService.generateCheckoutLinks(checkoutId));
        checkout.getData().getItems().add(item);
        checkout.getData().setReference(checkoutId);
        checkout.getData().setKind("order");
        checkout.getData().setDeliveryDetails(deliveryDetails);
        String totalOrderCostStr = checkoutHelper.calculateTotalOrderCostForCheckout(checkout) + "";
        checkout.getData().setTotalOrderCost(totalOrderCostStr);

        return checkoutRepository.save(checkout);
    }

    public Optional<Checkout> getCheckoutById(String id) {
        return checkoutRepository.findById(id);
    }

    /**
     * Saves the checkout, assumed to have been updated, to the database.
     * @param updatedCheckout the certificate item to save
     * @return the latest checkout state resulting from the save
     */
    public Checkout saveCheckout(final Checkout updatedCheckout) {
        final LocalDateTime now = LocalDateTime.now();
        updatedCheckout.setUpdatedAt(now);
        updatedCheckout.getData().setEtag(etagGeneratorService.generateEtag());
        return checkoutRepository.save(updatedCheckout);
    }
}
