package uk.gov.companieshouse.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthenticationInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
class HealthcheckControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAuthenticationInterceptor interceptor;

    @Test
    @DisplayName("Successfully returns health status")
    public void returnHealthStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Healthcheck is excluded from authentication")
    public void excludedFromAuth() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk());

        verify(interceptor, never()).preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(Object.class));
    }

    @Test
    @DisplayName("Non-healthcheck requests not excluded from authentication")
    public void notExcludedFromAuth() throws Exception {
        mockMvc.perform(get("/non-healthcheck"))
                .andExpect(status().isOk());

        verify(interceptor).preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(Object.class));
    }
}
