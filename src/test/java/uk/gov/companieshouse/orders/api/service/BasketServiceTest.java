package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

@ExtendWith(MockitoExtension.class)
public class BasketServiceTest {

    @InjectMocks
    private BasketService service;

    @Mock
    private BasketRepository repository;

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 01, 12, 9, 1);

    @Test
    public void saveBasketItemPopulatesCreatedAtAndUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasket(basket);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        assertThat(basket.getCreatedAt().isAfter(intervalStart) ||
                basket.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(basket.getCreatedAt().isBefore(intervalEnd) ||
                basket.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(basket.getUpdatedAt().isAfter(intervalStart) ||
                basket.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(basket.getUpdatedAt().isBefore(intervalEnd) ||
                basket.getUpdatedAt().isEqual(intervalEnd), is(true));
        verify(repository).save(basket);
    }

    @Test
    public void saveBasketItemPopulatesUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setCreatedAt(CREATED_AT);
        basket.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasket(basket);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        assertThat(basket.getCreatedAt(), is(CREATED_AT));
        assertThat(basket.getUpdatedAt().isAfter(intervalStart) ||
                basket.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(basket.getUpdatedAt().isBefore(intervalEnd) ||
                basket.getUpdatedAt().isEqual(intervalEnd), is(true));
        verify(repository).save(basket);
    }

    @Test
    public void createBasketThrowsExceptionIfIdNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Basket basket = new Basket();
            service.saveBasket(basket);
        });
    }

    /**
     * Verifies that the item created at and updated at timestamps are within the expected interval
     * for item creation.
     * @param itemCreated the item created
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    private void verifyCreationTimestampsWithinExecutionInterval(final Basket itemCreated,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {
        assertThat(itemCreated.getCreatedAt().isAfter(intervalStart) ||
                itemCreated.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getCreatedAt().isBefore(intervalEnd) ||
                itemCreated.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(itemCreated.getUpdatedAt().isAfter(intervalStart) ||
                itemCreated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getUpdatedAt().isBefore(intervalEnd) ||
                itemCreated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }
}
