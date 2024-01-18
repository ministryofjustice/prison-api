package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class, PersistenceConfigs.class})
@WithMockUser
@Slf4j
@DisplayName("OffenderBeliefRepository")
public class OffenderBeliefRepositoryTest {
    /*
    Test data added to migration SQL files R__8_8__OFFENDER_BELIEFS.sql, R__3_6_1__OFFENDER_BOOKINGS.sql, R__1_15__OFFENDERS.sql
    offender B1101BB
    has 3 beliefs across 2 bookings (-101 and -102)
    */

    @Autowired
    private OffenderBeliefRepository repository;

    @Test
    @DisplayName("can get belief history")
    void canGetBeliefHistory() {
        final var beliefs = repository.getOffenderBeliefHistory("B1101BB", null);
        assertThat(beliefs).hasSize(3)
            .extracting(OffenderBelief::getBeliefCode)
            .extracting(ProfileCode::getId)
            .extracting(PK::getCode)
            .containsExactly("MORM", "SCIE", "RC");
    }

    @Test
    @DisplayName("can get belief history for one booking")
    void canGetBeliefHistoryForOneBooking() {
        final var beliefs = repository.getOffenderBeliefHistory("B1101BB", "-101");
        assertThat(beliefs).hasSize(2)
            .extracting(OffenderBelief::getBeliefCode)
            .extracting(ProfileCode::getId)
            .extracting(PK::getCode)
            .containsExactly("MORM", "SCIE");
    }
}
