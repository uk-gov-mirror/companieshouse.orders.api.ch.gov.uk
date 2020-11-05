package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.AbstractOrder;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.controller.OrderController.ORDER_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.*;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;

@Service
public class UserAuthorisationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private static final String PATH_VARIABLES_ERROR = "No URI template path variables found in the request!";

    private final RequestMapper requestMapper;
    private final CheckoutRepository checkoutRepository;
    private final OrderRepository orderRepository;

    public UserAuthorisationInterceptor(final RequestMapper requestMapper,
                                        final CheckoutRepository checkoutRepository,
                                        final OrderRepository orderRepository) {
        this.requestMapper = requestMapper;
        this.checkoutRepository = checkoutRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        final RequestMappingInfo match = requestMapper.getRequestMapping(request);
        if (match != null) {
            switch (match.getName()) {
                case ADD_ITEM:
                case CHECKOUT_BASKET:
                case BASKET:
                    return true; // no authorisation required
                case GET_PAYMENT_DETAILS:
                    return getRequestClientIsAuthorised(request, response, this::getPaymentDetailsUserIsResourceOwner);
                case GET_ORDER:
                    return getRequestClientIsAuthorised(request, response, this::getOrderUserIsResourceOwner);
                case PATCH_PAYMENT_DETAILS:
                    return clientIsAuthorisedInternalApi(request, response);
                default:
                    // This should not happen.
                    throw new IllegalArgumentException("Mapped request with no authoriser: " + match.getName());
            }
        }
        return true;
    }

    /**
     * Inspects ERIC populated headers to determine whether the request is authorised.
     * @param request the request checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @param isResourceOwner the method to call to check resource ownership, should it be necessary
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getRequestClientIsAuthorised(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final BiPredicate<HttpServletRequest, HttpServletResponse> isResourceOwner) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        final String identityType = EricHeaderHelper.getIdentityType(request);
        logMap.put(LoggingUtils.IDENTITY_TYPE, identityType);
        if (API_KEY_IDENTITY_TYPE.equals(identityType)) {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting an API key", logMap);
            return clientIsAuthorisedInternalApi(request, response);
        } else {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting signed in user credentials", logMap);
            return isResourceOwner.test(request, response);
        }
    }

    /**
     * Inspects ERIC populated headers to determine whether the request comes from a user who is the owner of the
     * checkout resource the get payment details request attempts to access.
     * @param request the request checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getPaymentDetailsUserIsResourceOwner(final HttpServletRequest request,
                                                         final HttpServletResponse response) {
        return getRequestUserIsResourceOwner(request, response, CHECKOUT_ID_PATH_VARIABLE, this::retrieveCheckout);
    }

    /**
     * Inspects ERIC populated headers to determine whether the request comes from a user who is the owner of the
     * order resource the get order request attempts to access.
     * @param request the request checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getOrderUserIsResourceOwner(final HttpServletRequest request,
                                                final HttpServletResponse response) {
        return getRequestUserIsResourceOwner(request, response, ORDER_ID_PATH_VARIABLE, this::retrieveOrder);
    }

    /**
     * Inspects ERIC populated headers to determine whether the request comes from a user who is the owner of the
     * checkout resource the get payment details request attempts to access.
     * @param request the request checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @param resourceIdPathVariable the name of the resource ID Spring path variable
     * @param findById the method to call to retrieve the resource by its ID
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getRequestUserIsResourceOwner(final HttpServletRequest request,
                                                  final HttpServletResponse response,
                                                  final String resourceIdPathVariable,
                                                  final Function<String, AbstractOrder> findById) {
        final String requestUserId = EricHeaderHelper.getIdentity(request);
        final String orderId = getPathVariable(request, resourceIdPathVariable);
        final AbstractOrder order = findById.apply(orderId);
        if (requestUserId.equals(order.getUserId())) {
            LOGGER.infoRequest(request, "UserAuthorisationInterceptor: user is resource owner", null);
            return true;
        } else {
            LOGGER.infoRequest(request, "UserAuthorisationInterceptor: user is not resource owner", null);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }
    }

    /**
     * Extracts the named Spring path variable from the request.
     * @param request assumed to have been populated by Spring with the required path variable
     * @param pathVariable the name of the path variable
     * @return the path variable value
     */
    private String getPathVariable(final HttpServletRequest request, final String pathVariable) {
        final Map<String, String> uriTemplateVariables =
                (Map<String, String>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriTemplateVariables == null) {
            // This should not happen.
            LOGGER.error(PATH_VARIABLES_ERROR);
            throw new IllegalStateException(PATH_VARIABLES_ERROR);
        }
        return uriTemplateVariables.get(pathVariable);
    }

    /**
     * Retrieves the checkout identified by the ID from the checkout repository.
     * @param checkoutId the checkout ID
     * @return the checkout
     */
    private AbstractOrder retrieveCheckout(final String checkoutId) {
        return checkoutRepository.findById(checkoutId).orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Retrieves the order identified by the ID from the order repository.
     * @param orderId the order ID
     * @return the order
     */
    private AbstractOrder retrieveOrder(final String orderId) {
        return orderRepository.findById(orderId).orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Assuming the request contains ERIC headers representing an API client, this checks these to determine whether
     * the API client has an internal user role (aka "elevated privileges").
     * @param request the request to be checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean clientIsAuthorisedInternalApi(final HttpServletRequest request, final HttpServletResponse response)
    {
        // We know we are dealing with an API request here due to prior work carried out by the
        // UserAuthenticationInterceptor on this request.
        final boolean isAuthorisedInternalApi = AuthorisationUtil.hasInternalUserRole(request);
        if (!isAuthorisedInternalApi) {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client does not have required internal user role", null);
            response.setStatus(UNAUTHORIZED.value());
        } else {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client has required internal user role", null);
        }
        return isAuthorisedInternalApi;
    }
}
