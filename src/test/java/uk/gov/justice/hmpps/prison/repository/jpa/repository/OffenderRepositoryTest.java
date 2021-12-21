package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderRepositoryTest {

    @Autowired
    private OffenderRepository repository;

    @Test
    void findByOffenderNomsIdUnique() {
        final var offender = repository.findOffenderByNomsId("A1234AL").orElseThrow();

        assertThat(offender).extracting(Offender::getId, o -> o.getRootOffender().getId()).containsExactly(-1012L, -1012L);
        assertThat(offender.getBookings()).hasSize(2);
        assertThat(offender.getLatestBooking()).isPresent();
        assertThat(offender.getLatestBooking()).get().extracting(OffenderBooking::getBookingId).isEqualTo(-12L);
    }

    @Test
    void findByOffendersNomsId() {
        final var offenders = repository.findByNomsId("A1234AL");
        assertThat(offenders).isNotEmpty();
    }
}


