package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.companieshouse.orders.api.controller.BasketController.*;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_ORDER_URI;

@Service
public class RequestMapper implements InitializingBean {

    static final String ADD_ITEM = "addItem";
    static final String CHECKOUT_BASKET = "checkoutBasket";
    static final String GET_PAYMENT_DETAILS = "getPaymentDetails";
    static final String PATCH_BASKET = "patchBasket";
    static final String PATCH_PAYMENT_DETAILS = "patchPaymentDetails";
    static final String GET_ORDER = "getOrder";

    private final String addItemUri;
    private final String checkoutBasketUri;
    private final String patchBasketUri;
    private final String getPaymentDetailsUri;
    private final String getOrderUri;
    private final String patchPaymentDetailsUri;

    /**
     * Represents the requests identified by this.
     */
    private List<RequestMappingInfo> knownRequests;

    public RequestMapper(
            @Value(ADD_ITEM_URI)
            final String addItemUri,
            @Value(CHECKOUT_BASKET_URI)
            final String checkoutBasketUri,
            @Value(PATCH_BASKET_URI)
            final String patchBasketUri,
            @Value(GET_PAYMENT_DETAILS_URI)
            final String getPaymentDetailsUri,
            @Value(GET_ORDER_URI)
            final String getOrderUri,
            @Value(PATCH_PAYMENT_DETAILS_URI)
            final String patchPaymentDetailsUri) {
        this.addItemUri = addItemUri;
        this.checkoutBasketUri = checkoutBasketUri;
        this.patchBasketUri = patchBasketUri;
        this.getPaymentDetailsUri = getPaymentDetailsUri;
        this.getOrderUri = getOrderUri;
        this.patchPaymentDetailsUri = patchPaymentDetailsUri;
    }

    /**
     * Gets the request mapping found for the request provided.
     * @param request the HTTP request to be authenticated
     * @return the mapping representing the request if it is to be handled, or <code>null</code> if not
     */
    RequestMappingInfo getRequestMapping(final HttpServletRequest request) {
        for (final RequestMappingInfo mapping: knownRequests) {
            final RequestMappingInfo match = mapping.getMatchingCondition(request);
            if (match != null) {
                return match;
            }
        }
        return null; // no match found
    }

    @Override
    public void afterPropertiesSet() {
        final RequestMappingInfo addItem =
                new RequestMappingInfo(ADD_ITEM,
                        new PatternsRequestCondition(addItemUri),
                        new RequestMethodsRequestCondition(RequestMethod.POST),
                        null, null, null, null, null);
        final RequestMappingInfo checkoutBasket =
                new RequestMappingInfo(CHECKOUT_BASKET,
                        new PatternsRequestCondition(checkoutBasketUri),
                        new RequestMethodsRequestCondition(RequestMethod.POST),
                        null, null, null, null, null);
        final RequestMappingInfo getPaymentDetails =
                new RequestMappingInfo(GET_PAYMENT_DETAILS,
                        new PatternsRequestCondition(getPaymentDetailsUri),
                        new RequestMethodsRequestCondition(RequestMethod.GET),
                        null, null, null, null, null);
        final RequestMappingInfo patchBasket =
                new RequestMappingInfo(PATCH_BASKET,
                        new PatternsRequestCondition(patchBasketUri),
                        new RequestMethodsRequestCondition(RequestMethod.PATCH),
                        null, null, null, null, null);
        final RequestMappingInfo patchPaymentDetails =
                new RequestMappingInfo(PATCH_PAYMENT_DETAILS,
                        new PatternsRequestCondition(patchPaymentDetailsUri),
                        new RequestMethodsRequestCondition(RequestMethod.PATCH),
                        null, null, null, null, null);
        final RequestMappingInfo getOrder =
                new RequestMappingInfo(GET_ORDER,
                        new PatternsRequestCondition(getOrderUri),
                        new RequestMethodsRequestCondition(RequestMethod.GET),
                        null, null, null, null, null);

        knownRequests = asList(
                addItem, checkoutBasket, getPaymentDetails, patchBasket, patchPaymentDetails, getOrder
        );

    }
}
