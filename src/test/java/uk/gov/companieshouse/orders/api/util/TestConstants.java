package uk.gov.companieshouse.orders.api.util;

import uk.gov.companieshouse.orders.api.model.FilingHistoryDocument;

public class TestConstants {

    /** The HTTP request ID header name. */
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String TOKEN_REQUEST_ID_VALUE = "f058ebd6-02f7-4d3f-942e-904344e8cde5";
    public static final String ERIC_IDENTITY_HEADER_NAME = "ERIC-Identity";
    public static final String ERIC_IDENTITY_VALUE = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";
    public static final String ERIC_ACCESS_TOKEN = "ericAccessToken";
    public static final String WRONG_ERIC_IDENTITY_VALUE = "Y1V1Z1V1M1h1Z1F1Y1M1M1Q1M1";
    public static final String ERIC_IDENTITY_TYPE_HEADER_NAME = "ERIC-Identity-Type";
    public static final String ERIC_IDENTITY_OAUTH2_TYPE_VALUE = "oauth2";
    public static final String ERIC_AUTHORISED_USER_HEADER_NAME = "ERIC-Authorised-User";
    public static final String ERIC_AUTHORISED_USER_VALUE = "demo@ch.gov.uk";
    public static final String ERIC_IDENTITY_API_KEY_TYPE_VALUE = "key";
    public static final String ERIC_IDENTITY_INVALID_TYPE_VALUE = "invalid identity type";
    public static final String VALID_CERTIFICATE_URI = "/orderable/certificates/CRT-283515-943657";
    public static final String VALID_CERTIFIED_COPY_URI = "/orderable/certified-copies/CCD-473815-935982";
    public static final String VALID_MISSING_IMAGE_DELIVERY_URI = "/orderable/certified-copies/CCD-473815-935982";
    public static final String CERTIFICATE_KIND = "item#certificate";
    public static final String CERTIFIED_COPY_KIND = "item#certified-copy";
    public static final String CERTIFIED_COPY_COST = "15";
    public static final String SAME_DAY_CERTIFIED_COPY_COST = "50";
    public static final String SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST = "100";

    public static final FilingHistoryDocument DOCUMENT = new FilingHistoryDocument(
        "1993-04-01",
        "memorandum-articles",
        null,
        "MDAxMTEyNzExOGFkaXF6a2N4",
        "MEM/ARTS",
        "£15"
    );
}
