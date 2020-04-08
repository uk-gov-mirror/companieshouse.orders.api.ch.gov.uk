package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
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
                case PATCH_BASKET:
                    return true; // no authorisation required
                case GET_PAYMENT_DETAILS:
                    return getPaymentDetailsClientIsAuthorised(request, response);
                case GET_ORDER:
                    return getOrderClientIsAuthorised(request, response);
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
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getPaymentDetailsClientIsAuthorised(final HttpServletRequest request,
                                                        final HttpServletResponse response) {
        final String identityType = EricHeaderHelper.getIdentityType(request);
        if (API_KEY_IDENTITY_TYPE.equals(identityType)) {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting an API key", null);
            return clientIsAuthorisedInternalApi(request, response);
        } else {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting signed in user credentials", null);
            return getPaymentDetailsUserIsResourceOwner(request, response);
        }
    }

    /**
     * Inspects ERIC populated headers to determine whether the request is authorised.
     * @param request the request checked
     * @param response the response, updated by this should the request be found to be unauthorised
     * @return whether the request is authorised (<code>true</code>), or not (<code>false</code>)
     */
    private boolean getOrderClientIsAuthorised(final HttpServletRequest request,
                                               final HttpServletResponse response) {
        final String identityType = EricHeaderHelper.getIdentityType(request);
        if (API_KEY_IDENTITY_TYPE.equals(identityType)) {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting an API key", null);
            return clientIsAuthorisedInternalApi(request, response);
        } else {
            LOGGER.infoRequest(request,
                    "UserAuthorisationInterceptor: client is presenting signed in user credentials", null);
            return getOrderUserIsResourceOwner(request, response);
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
        final String requestUserId = EricHeaderHelper.getIdentity(request);
        final String checkoutId = getPathVariable(request, CHECKOUT_ID_PATH_VARIABLE);
        final Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
        if (requestUserId.equals(checkout.getUserId())) {
            LOGGER.infoRequest(request, "UserAuthorisationInterceptor: user is resource owner", null);
            return true;
        } else {
            LOGGER.infoRequest(request, "UserAuthorisationInterceptor: user is not resource owner", null);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }
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
        final String requestUserId = EricHeaderHelper.getIdentity(request);
        final String orderId = getPathVariable(request, ORDER_ID_PATH_VARIABLE);
        final Order order = orderRepository.findById(orderId).orElseThrow(ResourceNotFoundException::new);
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
                    "UserAuthorisationInterceptor error: client does not have required internal user role", null);
            response.setStatus(UNAUTHORIZED.value());
        }
        return isAuthorisedInternalApi;
    }
}
