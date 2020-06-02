package uk.gov.companieshouse.orders.api.validator;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.service.ApiClientService;

@Component
public class CheckoutBasketValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private ApiClientService apiClientService;

    public CheckoutBasketValidator(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public List<String> getValidationErrors(final Basket basket) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.BASKET_ID, basket.getId());
        List<String> errors = new ArrayList<>();
        List<Item> basketItems = basket.getData().getItems();
        if (basketItems.isEmpty()) {
            errors.add(ErrorType.BASKET_ITEMS_MISSING.getValue());
        }
        else {
            String itemUri = "";
            try {
                itemUri = basketItems.get(0).getItemUri();
                LoggingUtils.logIfNotNull(logMap, LoggingUtils.ITEM_URI, itemUri);
                
                apiClientService.getItem(itemUri);
            } catch (Exception exception) {
                logMap.put(LoggingUtils.EXCEPTION, exception);
                logMap.put(LoggingUtils.ERROR_TYPE, ErrorType.BASKET_ITEM_INVALID.getValue());
                LOGGER.error("Failed to get item", logMap);
                errors.add(ErrorType.BASKET_ITEM_INVALID.getValue());
            }
        }
        return errors;
    }
}
