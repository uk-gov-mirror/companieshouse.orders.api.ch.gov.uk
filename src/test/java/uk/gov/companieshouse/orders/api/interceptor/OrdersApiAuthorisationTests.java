package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
public class OrdersApiAuthorisationTests {

    private static final String ITEM_URI = "/orderable/certificates/12345678";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserAuthenticationInterceptor authenticator;

    @MockBean
    private UserAuthorisationInterceptor authoriser;

    @Test
    @DisplayName("User authentication takes place before user authorisation")
    void authenticationPrecedesAuthorisation() {

        // Given
        final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);
        when(authenticator
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class)))
                .thenReturn(true);

        // When and then
        webTestClient.post().uri("/basket/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .body(fromObject(addToBasketRequestDTO))
                .exchange()
                .expectStatus().isOk();

        final InOrder ordering = inOrder(authenticator, authoriser);
        ordering.verify(authenticator)
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class));
        ordering.verify(authoriser)
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class));
    }

    @Test
    @DisplayName("If user authentication fails, authorisation does not take place")
    void failedAuthenticationShortCircuitsAuthorisation() {

        // Given
        final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);
        when(authenticator
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class)))
                .thenReturn(false);

        // When and then
        webTestClient.post().uri("/basket/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .body(fromObject(addToBasketRequestDTO))
                .exchange()
                .expectStatus().isOk();

        verify(authenticator)
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class));
        verify(authoriser, never())
                .preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class));

    }

}
