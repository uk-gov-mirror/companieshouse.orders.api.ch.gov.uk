package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;

@Service
public class UserAuthenticationInterceptor extends HandlerInterceptorAdapter implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

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
     * Represents the requests authenticated by this.
     */
    private List<RequestMappingInfo> authenticatedRequests;

    // TODO GCI-332 Use constants for complete SPEL expressions.
    public UserAuthenticationInterceptor(
            @Value("${uk.gov.companieshouse.orders.api.basket.items}")
            final String addItemUri,
            @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}")
            final String checkoutBasketUri,
            @Value("${uk.gov.companieshouse.orders.api.basket}")
            final String patchBasketUri,
            @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}/{checkoutId}/payment")
            final String getPaymentDetailsUri,
            @Value("${uk.gov.companieshouse.orders.api.orders}/{id}")
            final String getOrderUri,
            @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}/{id}/payment")
            final String patchPaymentDetailsUri) {
        this.addItemUri = addItemUri;
        this.checkoutBasketUri = checkoutBasketUri;
        this.patchBasketUri = patchBasketUri;
        this.getPaymentDetailsUri = getPaymentDetailsUri;
        this.getOrderUri = getOrderUri;
        this.patchPaymentDetailsUri = patchPaymentDetailsUri;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        final RequestMappingInfo match = getRequestMapping(request);
        if (match != null) {
            switch (match.getName()) {
                case ADD_ITEM:
                case CHECKOUT_BASKET:
                case PATCH_BASKET:
                    return hasSignedInUser(request, response);
                case GET_PAYMENT_DETAILS:
                case GET_ORDER:
                    return hasAuthenticatedClient(request, response);
                case PATCH_PAYMENT_DETAILS:
                    return hasAuthenticatedApi(request, response);
                default:
                    // This should not happen.
                    throw new IllegalArgumentException("Mapped request with no authenticator: " + match.getName());
            }
        }
        return true;
    }

    /**
     * Gets the request mapping found for the request provided.
     * @param request the HTTP request to be authenticated
     * @return the mapping representing the request if it is to be authenticated, or <code>null</code> if not
     */
    RequestMappingInfo getRequestMapping(final HttpServletRequest request) {
        for (final RequestMappingInfo mapping: authenticatedRequests) {
            final RequestMappingInfo match = mapping.getMatchingCondition(request);
            if (match != null) {
                return match;
            }
        }
        return null; // no match found
    }

    private boolean hasSignedInUser(final HttpServletRequest request,
                                    final HttpServletResponse response) {
        final String identityType = getAuthorisedIdentityType(request, response);
        if (identityType == null) {
            return false;
        }
        return hasRequiredIdentity(request, response, identityType, OAUTH2_IDENTITY_TYPE);
    }

    private boolean hasAuthenticatedApi(final HttpServletRequest request,
                                        final HttpServletResponse response) {
        final String identityType = getAuthorisedIdentityType(request, response);
        if (identityType == null) {
            return false;
        }
        return hasRequiredIdentity(request, response, identityType, API_KEY_IDENTITY_TYPE);
    }

    private boolean hasAuthenticatedClient(final HttpServletRequest request,
                                           final HttpServletResponse response) {
        final String identityType = getAuthorisedIdentityType(request, response);
        if (identityType == null) {
            return false;
        }
        final String identity = EricHeaderHelper.getIdentity(request);
        // TODO GCI-332 Rationalise?
        if (!identityType.equals(OAUTH2_IDENTITY_TYPE) && !identityType.equals(API_KEY_IDENTITY_TYPE) || isBlank(identity)) {
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity", null);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }

    private String getAuthorisedIdentityType(final HttpServletRequest request,
                                             final HttpServletResponse response) {
        final String identityType = EricHeaderHelper.getIdentityType(request);
        if (identityType == null) {
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity type", null);
            response.setStatus(UNAUTHORIZED.value());
        }
        return identityType;
    }

    private boolean hasRequiredIdentity(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final String actualIdentityType,
                                        final String requiredIdentityType) {
        final String identity = EricHeaderHelper.getIdentity(request);
        if (!actualIdentityType.equals(requiredIdentityType) || isBlank(identity)) {
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity", null);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
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

        authenticatedRequests = asList(
                addItem, checkoutBasket, getPaymentDetails, patchBasket, patchPaymentDetails, getOrder
        );

    }
}
