package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAlertPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderChargePendingDeletion;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PrisonApiServer.class, RepositoryConfiguration.class})
class OffenderAliasPendingDeletionRepositoryTest {

    @Autowired
    private OffenderAliasPendingDeletionRepository repository;

    @Test
    void findOffenderAliasPendingDeletion() {

        final var offenders = repository.findOffenderAliasPendingDeletionByOffenderNumber("A1234AA");

        assertThat(offenders).hasSize(1);
        final var offender = offenders.get(0);

        assertThat(offender.getOffenderNumber()).isEqualTo("A1234AA");
        assertThat(offender.getOffenderId()).isEqualTo(-1001L);
        assertThat(offender.getRootOffenderId()).isEqualTo(-1001L);
        assertThat(offender.getFirstName()).isEqualTo("ARTHUR");
        assertThat(offender.getMiddleName()).isEqualTo("BORIS");
        assertThat(offender.getLastName()).isEqualTo("ANDERSON");
        assertThat(offender.getBirthDate()).isEqualTo(LocalDate.of(1969, 12, 30));

        assertThat(offender.getOffenderBookings()).hasSize(1);
        assertThat(offender.getOffenderBookings().get(0).getBookingId()).isEqualTo(-1);
        assertThat(offender.getOffenderBookings().get(0).getOffenderCharges())
                .extracting(OffenderChargePendingDeletion::getOffenceCode)
                .containsExactlyInAnyOrder("RC86356", "RV98011");
        assertThat(offender.getOffenderBookings().get(0).getOffenderAlerts())
                .extracting(OffenderAlertPendingDeletion::getAlertCode)
                .containsExactlyInAnyOrder("XA", "HC", "RSS", "XTACT");
    }

    @Test
    void findOffenderAliasPendingDeletionReturnsEmpty() {
        assertThat(repository.findOffenderAliasPendingDeletionByOffenderNumber("DOES_NOT_EXIST")).isEmpty();
    }
}