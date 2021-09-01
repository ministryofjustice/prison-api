package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = {PrisonApiServer.class, RepositoryConfiguration.class})
class DeceasedOffenderPendingDeletionRepositoryTest {


    @Autowired
    private DeceasedOffenderPendingDeletionRepository repository;

    @Test
    void findDeceasedOffendersDueForDeletionWithPaging() {

        var offenders = repository.findDeceasedOffendersDueForDeletion(LocalDate.now(), PageRequest.of(0, 1));

        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting(OffenderPendingDeletion::getOffenderNumber).containsOnly("Z0023ZZ");
    }

    @Test
    void findDeceasedOffendersDueForDeletionUnpaged() {

        var offenders = repository.findDeceasedOffendersDueForDeletion(LocalDate.now(), Pageable.unpaged());

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(OffenderPendingDeletion::getOffenderNumber).containsExactly("Z0023ZZ", "Z0017ZZ");
    }


    @Test
    void findDeceasedOffendersWhenNoOffendersMeet22YearsCriteria() {

        var offenders = repository.findDeceasedOffendersDueForDeletion(LocalDate.now().minusYears(50), Pageable.unpaged());

        assertThat(offenders).hasSize(0);
    }

    @Test
    void findDeceasedOffendersDueForDeletionWithPagingOffset() {

        var offenders = repository.findDeceasedOffendersDueForDeletion(LocalDate.now(), PageRequest.of(1, 1));

        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting(OffenderPendingDeletion::getOffenderNumber).containsExactly("Z0017ZZ");
    }

}
