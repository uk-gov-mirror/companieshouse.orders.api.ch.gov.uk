package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.*;

@Service
public class UserAuthorisationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final RequestMapper requestMapper;

    public UserAuthorisationInterceptor(final RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
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
                case GET_ORDER:
                    // TODO GCI-951: Authorisation.
                    return true;
                case PATCH_PAYMENT_DETAILS:
                    return clientIsAuthorisedInternalApi(request, response);
                default:
                    // This should not happen.
                    throw new IllegalArgumentException("Mapped request with no authoriser: " + match.getName());
            }
        }
        return true;
    }

    private boolean clientIsAuthorisedInternalApi(final HttpServletRequest request, final HttpServletResponse response) {
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
