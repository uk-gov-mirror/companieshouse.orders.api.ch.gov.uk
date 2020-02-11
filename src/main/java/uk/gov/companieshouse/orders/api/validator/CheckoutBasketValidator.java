package uk.gov.companieshouse.orders.api.validator;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import java.util.ArrayList;
import java.util.List;

@Component
public class CheckoutBasketValidator {

    public List<String> getValidationErrors(final Basket basket) {
        List<String> errors = new ArrayList<>();
        List<BasketItem> basketItems = basket.getData().getItems();
        if(basketItems.isEmpty()) {
            errors.add("basket is empty");
        }
        return errors;
    }
}
