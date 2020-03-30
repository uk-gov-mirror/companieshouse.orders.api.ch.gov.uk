package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;

@Service
public class UserAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final String addItemUri;
    private final String checkoutBasketUri;
    private final String patchBasketUri;

    public UserAuthenticationInterceptor(
            @Value("${uk.gov.companieshouse.orders.api.basket.items}")     final String addItemUri,
            @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}") final String checkoutBasketUri,
            @Value("${uk.gov.companieshouse.orders.api.basket}")           final String patchBasketUri) {
        this.addItemUri = addItemUri;
        this.checkoutBasketUri = checkoutBasketUri;
        this.patchBasketUri = patchBasketUri;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        if (request.getMethod().equals(POST.name()) && request.getRequestURI().endsWith(addItemUri) /* add item */ ||
            request.getMethod().equals(POST.name()) && request.getRequestURI().endsWith(checkoutBasketUri) /* checkout basket */ ||
            request.getMethod().equals(PATCH.name()) && request.getRequestURI().endsWith(patchBasketUri) /* patch basket */
        ) {
            return hasSignedInUser(request, response);
        } else {
            return true;
        }
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
}
