package uk.gov.companieshouse.orders.api.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class EricHeaderHelper {

    public static final String OAUTH2_IDENTITY_TYPE         = "oauth2";
    public static final String API_KEY_IDENTITY_TYPE        = "key";

    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    public static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";

    private EricHeaderHelper() { }

    /**
     * Gets the `ERIC-Identity` header value from the request.
     * @param request the HTTP request
     * @return a non-blank header value, or <code>null</code>
     */
    public static String getIdentity(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY);
    }

    /**
     * Gets the `ERIC-Identity-Type` header value from the request.
     * @param request the HTTP request
     * @return a non-blank header value, or <code>null</code>
     */
    public static String getIdentityType(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY_TYPE);
    }

    /**
     * Gets the `ERIC-Authorised-User` header value from the request.
     * @param request the HTTP request
     * @return a non-blank header value, or <code>null</code>
     */
    public static String getAuthorisedUser(HttpServletRequest request) {
        return getHeader(request, ERIC_AUTHORISED_USER);
    }

    private static String getHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (StringUtils.isNotBlank(headerValue)) {
            return headerValue;
        } else {
            return null;
        }
    }

}

