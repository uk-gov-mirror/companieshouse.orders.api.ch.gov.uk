package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.repository.BasketItemRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

@ExtendWith(MockitoExtension.class)
public class BasketItemServiceTest {

    @InjectMocks
    private BasketItemService service;

    @Mock
    private BasketItemRepository repository;

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 01, 12, 9, 1);

    @Test
    public void saveBasketItemPopulatesCreatedAtAndUpdatedAtAndSavesItem() {
        final BasketItem basketItem = new BasketItem();
        basketItem.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasketItem(basketItem);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        assertThat(basketItem.getCreatedAt().isAfter(intervalStart) ||
                basketItem.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(basketItem.getCreatedAt().isBefore(intervalEnd) ||
                basketItem.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(basketItem.getUpdatedAt().isAfter(intervalStart) ||
                basketItem.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(basketItem.getUpdatedAt().isBefore(intervalEnd) ||
                basketItem.getUpdatedAt().isEqual(intervalEnd), is(true));
        verify(repository).save(basketItem);
    }

    @Test
    public void saveBasketItemPopulatesUpdatedAtAndSavesItem() {
        final BasketItem basketItem = new BasketItem();
        basketItem.setCreatedAt(CREATED_AT);
        basketItem.setId(ERIC_IDENTITY_VALUE);

        final LocalDateTime intervalStart = LocalDateTime.now();

        service.saveBasketItem(basketItem);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        assertThat(basketItem.getCreatedAt(), is(CREATED_AT));
        assertThat(basketItem.getUpdatedAt().isAfter(intervalStart) ||
                basketItem.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(basketItem.getUpdatedAt().isBefore(intervalEnd) ||
                basketItem.getUpdatedAt().isEqual(intervalEnd), is(true));
        verify(repository).save(basketItem);
    }

    @Test
    public void createBasketThrowsExceptionIfIdNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> {
            final BasketItem basketItem = new BasketItem();
            service.saveBasketItem(basketItem);
        });
    }

    /**
     * Verifies that the item created at and updated at timestamps are within the expected interval
     * for item creation.
     * @param itemCreated the item created
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    private void verifyCreationTimestampsWithinExecutionInterval(final BasketItem itemCreated,
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
