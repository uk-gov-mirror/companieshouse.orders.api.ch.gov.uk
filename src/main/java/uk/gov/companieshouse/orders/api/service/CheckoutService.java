package uk.gov.companieshouse.orders.api.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;

import java.time.LocalDateTime;
import java.util.List;
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
        String totalOrderCostStr = calculateTotalOrderCostForCheckout(checkout) + "";
        checkout.getData().setTotalOrderCost(totalOrderCostStr);

        return checkoutRepository.save(checkout);
    }

    protected double calculateTotalOrderCostForCheckout(Checkout checkout){
        CheckoutData checkoutData = checkout.getData();
        List<Item> items = checkoutData.getItems();
        double totalOrderCost = 0.0;
        for (Item item : items){
            List<ItemCosts> itemCosts = item.getItemCosts();
            double totalCalculatedCosts = 0.0;
            if (itemCosts != null) {
                for (ItemCosts itemCost : itemCosts) {
                    totalCalculatedCosts += Double.parseDouble(itemCost.getCalculatedCost());
                }
                totalOrderCost += totalCalculatedCosts + Double.parseDouble(item.getPostageCost());
            }
        }

        return totalOrderCost;
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
