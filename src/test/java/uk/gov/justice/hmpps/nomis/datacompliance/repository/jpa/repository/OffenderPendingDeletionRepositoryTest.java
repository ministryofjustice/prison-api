package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PrisonApiServer.class, RepositoryConfiguration.class})
class OffenderPendingDeletionRepositoryTest {

    private static final LocalDate SENTENCE_END_DATE = LocalDate.of(2020, 3, 24);
    private static final LocalDate DELETION_DUE_DATE = SENTENCE_END_DATE.plusYears(7);
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 1);

    @Autowired
    private OffenderPendingDeletionRepository repository;

    @Test
    void getOffendersDueForDeletionUsesToDateExclusively() {

        var offenders = repository.getOffendersDueForDeletionBetween(
                DELETION_DUE_DATE,
                DELETION_DUE_DATE,
                PAGE_REQUEST);

        assertThat(offenders).hasSize(0);
    }

    @Test
    void getOffendersDueForDeletionReturnsEmpty() {
        assertThat(repository.getOffendersDueForDeletionBetween(
                LocalDate.of(1970, 1, 1),
                LocalDate.of(1970, 1, 2),
                PAGE_REQUEST))
                .isEmpty();
    }

    @Test
    void getOffendersDueForDeletionReturnsEmptyForDatesWrongWayRound() {
        assertThat(repository.getOffendersDueForDeletionBetween(
                DELETION_DUE_DATE.plusDays(1),
                DELETION_DUE_DATE.minusDays(1),
                PAGE_REQUEST))
                .isEmpty();
    }

    @Test
    @Sql("add_iwp_document.sql")
    @Sql(value = "remove_iwp_document.sql", executionPhase = AFTER_TEST_METHOD)
    void getOffendersDueForDeletionFiltersOutThoseWithDocuments() {
        assertThat(repository.getOffendersDueForDeletionBetween(
                DELETION_DUE_DATE,
                DELETION_DUE_DATE.plusDays(1),
                PAGE_REQUEST)).isEmpty();
    }

    @Test
    @Sql("add_offender_non_association.sql")
    @Sql(value = "remove_offender_non_association.sql", executionPhase = AFTER_TEST_METHOD)
    void getOffendersDueForDeletionFiltersOutThoseWithNonAssociations() {
        assertThat(repository.getOffendersDueForDeletionBetween(
                DELETION_DUE_DATE,
                DELETION_DUE_DATE.plusDays(1),
                PAGE_REQUEST)).isEmpty();
    }

    @Test
    void findOffenderPendingDeletionReturnsEmptyIfDeletionNotDue() {
        assertThat(repository.findOffenderPendingDeletion("Z0020ZZ", DELETION_DUE_DATE.minusDays(1)))
                .isEmpty();
    }

    @Test
    void findOffenderPendingDeletionReturnsEmptyIfOffenderNotFound() {
        assertThat(repository.findOffenderPendingDeletion("UNKNOWN", DELETION_DUE_DATE.plusDays(1)))
                .isEmpty();
    }
}
