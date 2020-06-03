package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.ADD_ITEM;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.BASKET;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.CHECKOUT_BASKET;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.GET_ORDER;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.GET_PAYMENT_DETAILS;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.PATCH_PAYMENT_DETAILS;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

@Service
public class UserAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final RequestMapper requestMapper;

    public UserAuthenticationInterceptor(final RequestMapper requestMapper) {
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
                case BASKET:
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
     * Checks whether the request contains the required ERIC headers representing an OAuth2 signed in user.
     * @param request the request to be checked
     * @param response the response in which the status code is set to 401 Unauthorised by this where required tokens
     *                 are missing from request
     * @return whether the request contains the required authentication tokens (<code>true</code>), or not
     * (<code>false</code>)
     */
    private boolean hasSignedInUser(final HttpServletRequest request,
                                    final HttpServletResponse response) {
        return hasRequiredIdentity(request, response, singletonList(OAUTH2_IDENTITY_TYPE));
    }

    /**
     * Checks whether the request contains the required ERIC headers representing a CHS internal API.
     * @param request the request to be checked
     * @param response the response in which the status code is set to 401 Unauthorised by this where required tokens
     *                 are missing from request
     * @return whether the request contains the required authentication tokens (<code>true</code>), or not
     * (<code>false</code>)
     */
    private boolean hasAuthenticatedApi(final HttpServletRequest request,
                                        final HttpServletResponse response) {
        return hasRequiredIdentity(request, response, singletonList(API_KEY_IDENTITY_TYPE));
    }

    /**
     * Checks whether the request contains the required ERIC headers representing either an OAuth2 signed in user,
     * or a CHS internal API.
     * @param request the request to be checked
     * @param response the response in which the status code is set to 401 Unauthorised by this where required tokens
     *                 are missing from request
     * @return whether the request contains the required authentication tokens (<code>true</code>), or not
     * (<code>false</code>)
     */
    private boolean hasAuthenticatedClient(final HttpServletRequest request,
                                           final HttpServletResponse response) {
        return hasRequiredIdentity(request, response, asList(API_KEY_IDENTITY_TYPE, OAUTH2_IDENTITY_TYPE));
    }

    /**
     * Gets the value of the <code>ERIC-Identity-Type</code> header on the request.
     * @param request the request to be inspected
     * @param response the response in which the status code is set to 401 Unauthorised by this where there is no value
     *                 for the header in the request
     * @return the value of the header, which may be <code>null</code>
     */
    private String getAuthorisedIdentityType(final HttpServletRequest request,
                                             final HttpServletResponse response) {
        final String identityType = EricHeaderHelper.getIdentityType(request);
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        logMap.put(LoggingUtils.IDENTITY_TYPE, identityType);
        if (identityType == null) {
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity type provided", logMap);
            response.setStatus(UNAUTHORIZED.value());
        }
        return identityType;
    }

    /**
     * Checks whether the request contains a required <code>ERIC-Identity-Type</code> header value, and if so, whether
     * it contains a non-blank value for the <code>ERIC-Identity</code> header.
     * @param request the request to be checked
     * @param response the response in which the status code is set to 401 Unauthorised by this where required tokens
     *                 are missing from request
     * @param requiredIdentityTypes the required ERIC identity types
     * @return whether the request contains the required authentication tokens (<code>true</code>), or not
     * (<code>false</code>)
     */
    private boolean hasRequiredIdentity(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final List<String> requiredIdentityTypes) {
        final String identityType = getAuthorisedIdentityType(request, response);
        if (identityType == null) {
            return false;
        }
        return hasRequiredIdentity(request, response, identityType, requiredIdentityTypes);
    }

    /**
     * Checks whether the request contains a required <code>ERIC-Identity-Type</code> header value, and if so, whether
     * it contains a non-blank value for the <code>ERIC-Identity</code> header.
     * @param request the request to be checked
     * @param response the response in which the status code is set to 401 Unauthorised by this where required tokens
     *                 are missing from request
     * @param actualIdentityType the actual ERIC identity type
     * @param requiredIdentityTypes the required ERIC identity types
     * @return whether the request contains the required authentication tokens (<code>true</code>), or not
     * (<code>false</code>)
     */
    private boolean hasRequiredIdentity(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final String actualIdentityType,
                                        final List<String> requiredIdentityTypes) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        logMap.put(LoggingUtils.IDENTITY_TYPE, actualIdentityType);
        final String identity = EricHeaderHelper.getIdentity(request);
        if (!requiredIdentityTypes.contains(actualIdentityType)|| identity == null) {
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request,
                    "UserAuthenticationInterceptor error: no authorised identity (provided identity type: " +
                            actualIdentityType + ", required (any of): " + requiredIdentityTypes + ")", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }
        return true;
    }

}
