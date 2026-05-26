package uk.gov.justice.hmpps.prison.service.enteringandleaving

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.service.TestClock
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@SpringBootTest
@WithMockAuthUser("ITAG_USER_ADM")
@ContextConfiguration(classes = [TestClock::class])
@ActiveProfiles("test")
@Transactional
class PrisonerTransferServiceTest {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @Autowired
  private lateinit var prisonerReleaseAndTransferService: TransferIntoPrisonService

  @Test
  @Sql(
    scripts = ["/sql/scheduledPrisonerReturnFromCourt_init.sql"],
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Sql(
    scripts = ["/sql/scheduledPrisonerReturnFromCourt_clean.sql"],
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  fun scheduledPrisonerReturnFromCourt() {
    val requestForCourtTransferIn = RequestForCourtTransferIn()
    requestForCourtTransferIn.agencyId = "BXI"
    prisonerReleaseAndTransferService.transferInViaCourt(OFFENDER_NO, requestForCourtTransferIn)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    val offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156")

    assertThat(offenderBookings[0]["IN_OUT_STATUS"].toString()).isEqualTo("IN")
    assertThat(offenderBookings[0]["AGENCY_IML_ID"]).isNull()
    val externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2")
    assertThat(externalMovements[0]["ACTIVE_FLAG"].toString()).isEqualTo("N")

    val nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3")
    assertThat(nextExternalMovements[0]["ACTIVE_FLAG"].toString()).isEqualTo("Y")
    assertThat(nextExternalMovements[0]["TO_AGY_LOC_ID"].toString()).isEqualTo("BXI")
    assertThat(nextExternalMovements[0]["FROM_AGY_LOC_ID"].toString()).isEqualTo("ABDRCT")
    assertThat(nextExternalMovements[0]["PARENT_EVENT_ID"].toString()).isEqualTo("455654697")
    assertThat(nextExternalMovements[0]["EVENT_ID"].toString()).isEqualTo("455654698")

    val courtEvents = jdbcTemplate.queryForList("select * from COURT_EVENTS where EVENT_ID=455654698")
    assertThat(courtEvents[0]["PARENT_EVENT_ID"].toString()).isEqualTo("455654697")
    assertThat(courtEvents[0]["EVENT_STATUS"].toString()).isEqualTo("COMP")
  }

  @Test
  @Sql(
    scripts = ["/sql/unscheduledPrisonerReturnFromCourt_init.sql"],
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Sql(
    scripts = ["/sql/unscheduledPrisonerReturnFromCourt_clean.sql"],
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  fun unscheduledPrisonerReturnFromCourt() {
    val requestForCourtTransferIn = RequestForCourtTransferIn()
    requestForCourtTransferIn.agencyId = "BXI"
    prisonerReleaseAndTransferService.transferInViaCourt(OFFENDER_NO, requestForCourtTransferIn)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    val offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156")

    assertThat(offenderBookings[0]["IN_OUT_STATUS"].toString()).isEqualTo("IN")
    assertThat(offenderBookings[0]["AGENCY_IML_ID"]).isNull()
    val externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2")
    assertThat(externalMovements[0]["ACTIVE_FLAG"].toString()).isEqualTo("N")

    val nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3")
    assertThat(nextExternalMovements[0]["ACTIVE_FLAG"].toString()).isEqualTo("Y")
    assertThat(nextExternalMovements[0]["TO_AGY_LOC_ID"].toString()).isEqualTo("BXI")
    assertThat(nextExternalMovements[0]["FROM_AGY_LOC_ID"].toString()).isEqualTo("ABDRCT")
    assertThat<Any?>(nextExternalMovements[0]["PARENT_EVENT_ID"]).isNull()
    assertThat<Any?>(nextExternalMovements[0]["EVENT_ID"]).isNull()
  }

  companion object {
    private const val OFFENDER_NO = "G6942UN"
  }
}
