package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
                case PATCH_PAYMENT_DETAILS:
                    // TODO GCI-951: Authorisation.
                    LOGGER.error("Authoriser currently blocking all requests requiring authorisation!");
//                    response.setStatus(UNAUTHORIZED.value());
//                    return false;
                    return true;
                default:
                    // This should not happen.
                    throw new IllegalArgumentException("Mapped request with no authenticator: " + match.getName());
            }
        }
        return true;
    }
}
