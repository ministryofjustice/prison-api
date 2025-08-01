package uk.gov.justice.hmpps.prison.repository

import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
  HmppsAuthenticationHolder::class,
  AuditorAwareImpl::class,
  PersistenceConfigs::class,
)
@Slf4j
@DisplayName("OffenderBeliefRepository")
class OffenderBeliefRepositoryTest {
  /*
   * Test data added to migration SQL files R__8_8__OFFENDER_BELIEFS.sql, R__3_6_1__OFFENDER_BOOKINGS.sql, R__1_15__OFFENDERS.sql
   * offender B1101BB
   * has 5 beliefs across 3 bookings (-101, -102 and -103) and 2 aliases (offenders -101 and -102), one having a non-existent booking id which can happen but should not cause any problems
   */
  @Autowired
  private lateinit var repository: OffenderBeliefRepository

  @Test
  @DisplayName("can get belief history")
  fun canGetBeliefHistory() {
    val beliefs = repository.getOffenderBeliefHistory("B1101BB", null)
    Assertions.assertThat(beliefs).hasSize(4)
      .extracting<ProfileCode, RuntimeException>(OffenderBelief::beliefCode)
      .extracting<ProfileCode.PK, RuntimeException> { obj: ProfileCode -> obj.id }
      .extracting<String, RuntimeException> { obj: ProfileCode.PK -> obj.code }
      .containsExactly("BUDD", "MORM", "SCIE", "RC")
  }

  @Test
  @DisplayName("can get belief history for one booking")
  fun canGetBeliefHistoryForOneBooking() {
    val beliefs = repository.getOffenderBeliefHistory("B1101BB", "-101")
    Assertions.assertThat(beliefs).hasSize(2)
      .extracting<ProfileCode, RuntimeException>(OffenderBelief::beliefCode)
      .extracting<ProfileCode.PK, RuntimeException> { obj: ProfileCode -> obj.id }
      .extracting<String, RuntimeException> { obj: ProfileCode.PK -> obj.code }
      .containsExactly("MORM", "SCIE")
  }
}
