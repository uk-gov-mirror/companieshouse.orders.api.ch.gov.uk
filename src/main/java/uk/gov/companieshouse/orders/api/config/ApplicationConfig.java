package uk.gov.companieshouse.orders.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthenticationInterceptor;

import static uk.gov.companieshouse.orders.api.controller.HealthcheckController.HEALTHCHECK_URI;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private final UserAuthenticationInterceptor authenticationInterceptor;
    private final String healthcheckUri;

    public ApplicationConfig(final UserAuthenticationInterceptor authenticationInterceptor,
                             @Value(HEALTHCHECK_URI) final String healthcheckUri) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.healthcheckUri = healthcheckUri;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
        registry.addInterceptor(authenticationInterceptor).excludePathPatterns(healthcheckUri);
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
}
