package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

@ExtendWith(MockitoExtension.class)
public class BasketServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 1, 12, 9, 1);

    @InjectMocks
    private BasketService service;

    @Mock
    private BasketRepository repository;

    private TimestampedEntityVerifier timestamps = new TimestampedEntityVerifier();

    @Test
    public void saveBasketPopulatesCreatedAtAndUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasket(basket);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        timestamps.verifyCreationTimestampsWithinExecutionInterval(basket, intervalStart, intervalEnd);
        verify(repository).save(basket);
    }

    @Test
    public void saveBasketPopulatesUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setCreatedAt(CREATED_AT);
        basket.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasket(basket);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        assertThat(basket.getCreatedAt(), is(CREATED_AT));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(basket, intervalStart, intervalEnd);
        verify(repository).save(basket);
    }

    @Test
    public void createBasketThrowsExceptionIfIdNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Basket basket = new Basket();
            service.saveBasket(basket);
        });
    }

}
