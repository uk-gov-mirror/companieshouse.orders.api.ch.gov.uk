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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BasketItemServiceTest {

    @InjectMocks
    private BasketItemService service;

    @Mock
    private BasketItemRepository repository;

    @Test
    public void createBasketItemPopulatesAndSavesItem() {
        final BasketItem basketItem = new BasketItem();
        final LocalDateTime intervalStart = LocalDateTime.now();

        service.createBasketItem(basketItem);

        final LocalDateTime intervalEnd = LocalDateTime.now();
        verifyCreationTimestampsWithinExecutionInterval(basketItem, intervalStart, intervalEnd);
        verify(repository).save(basketItem);
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
