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

    private final UserAuthenticationInterceptor authenticationInterceptor;
    private final UserAuthorisationInterceptor authorisationInterceptor;
    private final String healthcheckUri;
    private final String patchPaymentDetailsUri;

    public ApplicationConfig(final UserAuthenticationInterceptor authenticationInterceptor,
                             final UserAuthorisationInterceptor authorisationInterceptor,
                             @Value(HEALTHCHECK_URI) final String healthcheckUri,
                             @Value(PATCH_PAYMENT_DETAILS_URI)
                             final String patchPaymentDetailsUri) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.authorisationInterceptor = authorisationInterceptor;
        this.healthcheckUri = healthcheckUri;
        this.patchPaymentDetailsUri = patchPaymentDetailsUri;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
        registry.addInterceptor(authenticationInterceptor).excludePathPatterns(healthcheckUri);
        registry.addInterceptor(authorisationInterceptor).excludePathPatterns(healthcheckUri);
        registry.addInterceptor(crudPermissionInterceptor()).excludePathPatterns(patchPaymentDetailsUri, healthcheckUri);
        // Do not run the permission check for patch payment as it is API key only
        registry.addInterceptor(crudPermissionInterceptorSkipPatch()).addPathPatterns(patchPaymentDetailsUri);
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
        return new CRUDAuthenticationInterceptor(Permission.Key.USER_ORDERS);
    }

    @Bean
    CRUDAuthenticationInterceptor crudPermissionInterceptorSkipPatch() {
        return new CRUDAuthenticationInterceptor(Permission.Key.USER_ORDERS, "PATCH");
    }

}
