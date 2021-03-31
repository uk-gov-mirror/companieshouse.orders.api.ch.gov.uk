package uk.gov.companieshouse.orders.api.config;

import static uk.gov.companieshouse.orders.api.controller.BasketController.PATCH_PAYMENT_DETAILS_URI;
import static uk.gov.companieshouse.orders.api.controller.HealthcheckController.HEALTHCHECK_URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthorisationInterceptor;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final UserAuthenticationInterceptor authenticationInterceptor;
    private final UserAuthorisationInterceptor authorisationInterceptor;
    private final String healthcheckUri;
    private final String paymentDetailsUri;

    public ApplicationConfig(final LoggingInterceptor loggingInterceptor,
                             final UserAuthenticationInterceptor authenticationInterceptor,
                             final UserAuthorisationInterceptor authorisationInterceptor,
                             @Value(HEALTHCHECK_URI) final String healthcheckUri,
                             @Value(PATCH_PAYMENT_DETAILS_URI)
                             final String paymentDetailsUri) {
        this.loggingInterceptor = loggingInterceptor;
        this.authenticationInterceptor = authenticationInterceptor;
        this.authorisationInterceptor = authorisationInterceptor;
        this.healthcheckUri = healthcheckUri;
        this.paymentDetailsUri = paymentDetailsUri;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
    	registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(crudPermissionInterceptor()).excludePathPatterns(paymentDetailsUri, healthcheckUri);
        // Different interceptor for payment details as API key traffic needs to be allowed:
        // - PATCH is always ignored since oauth2 is blocked for this function
        // - GET ignores API key requests to allow payments api to get costs but if oauth2 is used it still checks token permissions
        registry.addInterceptor(crudPermissionInterceptorPaymentDetails()).addPathPatterns(paymentDetailsUri);
        registry.addInterceptor(authenticationInterceptor).excludePathPatterns(healthcheckUri);
        registry.addInterceptor(authorisationInterceptor).excludePathPatterns(healthcheckUri);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .findAndRegisterModules();
    }

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }

    @Bean
    CRUDAuthenticationInterceptor crudPermissionInterceptor() {
        // true allows all api key traffic through but still checks for CRUD permissions when using oauth.
        // the UserAuthorisationInterceptor applies further checks to api key traffic to ensure where it is allowed it has the correct privileges.
        return new CRUDAuthenticationInterceptor(Permission.Key.USER_ORDERS, true);
    }

    @Bean
    CRUDAuthenticationInterceptor crudPermissionInterceptorPaymentDetails() {
        // true allows all api key traffic through but still checks for CRUD permissions when using oauth
        // PATCH added to ignoreHttpMethods means it will also be skipped for oauth crud functions since oauth is never allowed for this method
        return new CRUDAuthenticationInterceptor(Permission.Key.USER_ORDERS, true, "PATCH");
    }

}
