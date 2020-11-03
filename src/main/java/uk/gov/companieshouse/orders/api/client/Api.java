package uk.gov.companieshouse.orders.api.client;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.io.IOException;

@Component
public class Api {

    public InternalApiClient getInternalApiClient(String passthroughHeader) throws IOException {
        return ApiSdkManager.getPrivateSDK(passthroughHeader);
    }

    public ApiClient getPublicApiClient(String passthroughHeader) throws IOException {
        return ApiSdkManager.getSDK(passthroughHeader);
    }
}
