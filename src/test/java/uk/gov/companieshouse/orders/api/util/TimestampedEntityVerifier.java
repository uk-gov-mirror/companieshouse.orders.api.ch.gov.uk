package uk.gov.companieshouse.orders.api.util;

import uk.gov.companieshouse.orders.api.model.TimestampedEntity;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test utility that verifies the timestamp fields on {@link TimestampedEntity} instances.
 */
public class TimestampedEntityVerifier {

    /**
     * Verifies that the entity created at and updated at timestamps are within the expected interval
     * for the creation.
     * @param createdEntity the created entity
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    public void verifyCreationTimestampsWithinExecutionInterval(final TimestampedEntity createdEntity,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {
        assertThat(createdEntity.getCreatedAt().isAfter(intervalStart) ||
                createdEntity.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(createdEntity.getCreatedAt().isBefore(intervalEnd) ||
                createdEntity.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(createdEntity.getUpdatedAt().isAfter(intervalStart) ||
                createdEntity.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(createdEntity.getUpdatedAt().isBefore(intervalEnd) ||
                createdEntity.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

    /**
     * Verifies that the updated entity updated at timestamp is within the expected interval
     * for the update.
     * @param updatedEntity the updated entity
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    public void verifyUpdatedAtTimestampWithinExecutionInterval(final TimestampedEntity updatedEntity,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {

        assertThat(updatedEntity.getUpdatedAt().isAfter(updatedEntity.getCreatedAt()) ||
                updatedEntity.getUpdatedAt().isEqual(updatedEntity.getCreatedAt()), is(true));

        assertThat(updatedEntity.getUpdatedAt().isAfter(intervalStart) ||
                updatedEntity.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(updatedEntity.getUpdatedAt().isBefore(intervalEnd) ||
                updatedEntity.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

}
