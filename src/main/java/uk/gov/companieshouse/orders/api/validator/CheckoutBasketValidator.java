package uk.gov.companieshouse.orders.api.validator;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.service.ApiClientService;

import java.util.ArrayList;
import java.util.List;

@Component
public class CheckoutBasketValidator {
    private ApiClientService apiClientService;

    public List<String> getValidationErrors(final Basket basket) {
        List<String> errors = new ArrayList<>();
        List<BasketItem> basketItems = basket.getData().getItems();
        if (basketItems.isEmpty()) {
            errors.add(ErrorType.BASKET_ITEMS_MISSING.value);
        }
        else {
            try {
                String itemUri = basketItems.get(0).getItemUri();
                apiClientService.getItem(itemUri);
            } catch (Exception exception) {
                errors.add(ErrorType.BASKET_ITEM_INVALID.value);
            }
        }
        return errors;
    }

    public void setApiClientService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }
}
