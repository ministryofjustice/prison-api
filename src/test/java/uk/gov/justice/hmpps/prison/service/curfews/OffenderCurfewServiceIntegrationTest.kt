package uk.gov.justice.hmpps.prison.service.curfews

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus
import uk.gov.justice.hmpps.prison.api.model.HdcChecks
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepositoryTest.Companion.createNewCurfewForBookingId
import uk.gov.justice.hmpps.prison.service.BookingService
import uk.gov.justice.hmpps.prison.service.CaseloadToAgencyMappingService
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.time.LocalDate

/**
 * Integration tests for the OffenderCurfewServiceImpl + OffenderCurfewRepositoryImpl combination. These tests
 * seem necessary because the desired service behaviour relies upon interactions between the service and database triggers.
 */
@ActiveProfiles("test")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class OffenderCurfewServiceIntegrationTest(
  @Autowired private val offenderCurfewRepository: OffenderCurfewRepository,
  @Autowired private val jdbcTemplate: NamedParameterJdbcOperations,
) {
  private val bookingService: BookingService = mock()
  private val caseloadToAgencyMappingService: CaseloadToAgencyMappingService = mock()
  private val referenceDomainService: ReferenceDomainService = mock()

  private val offenderCurfewService: OffenderCurfewService = OffenderCurfewService(
    offenderCurfewRepository,
    caseloadToAgencyMappingService,
    bookingService,
    referenceDomainService,
  )
  private var curfewId: Long = 0

  @BeforeEach
  fun configureService() {
    whenever(
      referenceDomainService.isReferenceCodeActive(
        anyString(),
        anyString(),
      ),
    ).thenReturn(true)

    curfewId = createNewCurfewForBookingId(OFFENDER_BOOKING_ID, jdbcTemplate)
  }

  @Test
  fun givenInitialState() {
    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
    assertHomeDetentionCurfew(null, null, null, null, null)
  }

  @Test
  fun givenInitialState_whenSetChecksPassed() {
    setHdcCheck(true, date1)
    assertOffenderCurfew(true, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenInitialState_whenSetChecksFailed() {
    setHdcCheck(false, date1)
    assertOffenderCurfew(false, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenInitialState_whenDeleteHdcCheck() {
    deleteHdcCheck()
    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
    assertHomeDetentionCurfew(null, null, null, null, null)
  }

  @Test
  fun givenInitialState_whenDeleteApprovalStatus() {
    deleteApprovalStatus()
    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
    assertHomeDetentionCurfew(null, null, null, null, null)
  }

  @Test
  fun givenInitialState_whenSetApprovalStatus() {
    assertThatThrownBy { setApprovedStatus(date1) }.isInstanceOf(
      IllegalStateException::class.java,
    )
  }

  @Test
  fun givenChecksPassedState_whenSetChecksPassedWithSameDate() {
    setHdcCheck(true, date1)
    setHdcCheck(true, date1)
    assertOffenderCurfew(true, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksPassedState_whenSetChecksPassedWithDifferentDate() {
    setHdcCheck(true, date1)
    setHdcCheck(true, date2)
    assertOffenderCurfew(true, date2, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksPassedState_whenSetChecksFailed() {
    setHdcCheck(true, date1)
    setHdcCheck(false, date2)
    assertOffenderCurfew(false, date2, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksPassedState_whenDeleteChecksPassed() {
    setHdcCheck(true, date1)
    deleteHdcCheck()
    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
  }

  @Test
  fun givenChecksPassedState_whenDeleteApprovalStatus() {
    setHdcCheck(true, date1)
    deleteApprovalStatus()
    assertOffenderCurfew(true, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksPassedState_whenSetApprovalStatusApproved() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    assertOffenderCurfew(true, date1, "APPROVED", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedState_whenSetApprovalStatusNotApproved() {
    setHdcCheck(true, date1)
    setApprovalStatus("REFUSED", "BREACH", date2)
    assertOffenderCurfew(true, date1, "REFUSED", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "BREACH"),
    )
    assertHomeDetentionCurfew(true, date1, "REFUSED", date2, "BREACH")
  }

  @Test
  fun givenChecksFailedState_whenSetChecksFailed() {
    setHdcCheck(false, date1)
    setHdcCheck(false, date1)
    assertOffenderCurfew(false, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedState_whenSetChecksFailedDifferentDate() {
    setHdcCheck(false, date1)
    setHdcCheck(false, date2)
    assertOffenderCurfew(false, date2, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedState_whenSetChecksPassed() {
    setHdcCheck(false, date1)
    setHdcCheck(true, date2)
    assertOffenderCurfew(true, date2, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksFailedState_whenDeleteHdcChecks() {
    setHdcCheck(false, date1)
    deleteHdcCheck()
    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
  }

  @Test
  fun givenChecksFailedState_whenDeleteApprovalStatus() {
    setHdcCheck(false, date1)
    deleteApprovalStatus()
    assertOffenderCurfew(false, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedState_whenSetApprovalStatusApproved() {
    setHdcCheck(false, date1)
    assertThatThrownBy { setApprovedStatus(date1) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
  }

  @Test
  fun givenChecksFailedState_whenSetApprovalStatusNotApproved() {
    setHdcCheck(false, date1)
    setApprovalStatus("INELIGIBLE", "ADDRESS", date2)

    assertOffenderCurfew(false, date1, "INELIGIBLE", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", "ADDRESS"),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
    assertHomeDetentionCurfew(false, date1, "INELIGIBLE", date2, "ADDRESS")
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetApprovedSameDate() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setApprovedStatus(date2)

    assertOffenderCurfew(true, date1, "APPROVED", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetApprovedDifferentDate() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setApprovedStatus(date3)

    assertOffenderCurfew(true, date1, "APPROVED", date3)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetApprovalStatusNotApproved() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setApprovalStatus("OPT_OUT", "HDC_RECALL", date3)

    assertOffenderCurfew(true, date1, "OPT_OUT", date3)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "HDC_RECALL"),
    )
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetHdcCheckSameDate() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setHdcCheck(true, date1)

    assertOffenderCurfew(true, date1, "APPROVED", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetHdcCheckDifferentDate() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setHdcCheck(true, date3)

    assertOffenderCurfew(true, date3, "APPROVED", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedAndApproved_whenSetHdcCheckFailed() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    setHdcCheck(false, date3)

    assertOffenderCurfew(false, date3, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
    assertHomeDetentionCurfew(false, date3, null, null, null)
  }

  @Test
  fun givenChecksPassedAndApproved_whenDeleteApprovalStatus() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    deleteApprovalStatus()

    assertOffenderCurfew(true, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
    assertHomeDetentionCurfew(true, date1, null, null, null)
  }

  @Test
  fun givenChecksPassedAndApproved_whenDeleteHdcChecks() {
    setHdcCheck(true, date1)
    setApprovedStatus(date2)
    deleteHdcCheck()

    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
    assertHomeDetentionCurfew(null, null, null, null, null)
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetChecksPassedSameDate() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setHdcCheck(true, date1)

    assertOffenderCurfew(true, date1, "PRES UNSUIT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "OUTSTANDING"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetChecksPassedDifferentDate() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setHdcCheck(true, date3)

    assertOffenderCurfew(true, date3, "PRES UNSUIT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "OUTSTANDING"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetChecksFailed() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setHdcCheck(false, date3)

    assertOffenderCurfew(false, date3, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDate() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)

    assertOffenderCurfew(true, date1, "PRES UNSUIT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "OUTSTANDING"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDifferentDate() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date3)

    assertOffenderCurfew(true, date1, "PRES UNSUIT", date3)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "OUTSTANDING"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDifferentReason() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setApprovalStatus("PRES UNSUIT", "OTHER", date2)

    assertOffenderCurfew(true, date1, "PRES UNSUIT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "OTHER"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetDifferentApprovalStatusAndDifferentReason() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setApprovalStatus("PP INVEST", "FINE", date2)

    assertOffenderCurfew(true, date1, "PP INVEST", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("REFUSED", "FINE"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenSetApproved() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    setApprovedStatus(date3)

    assertOffenderCurfew(true, date1, "APPROVED", date3)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
      statusAndReason("GRANTED", "AFTER_ENH"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenDeleteApprovalStatus() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    deleteApprovalStatus()

    assertOffenderCurfew(true, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksPassedRefusedState_whenDeleteHdcCheck() {
    setHdcCheck(true, date1)
    setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2)
    deleteHdcCheck()

    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetChecksRefusedSameDate() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    setHdcCheck(false, date1)

    assertOffenderCurfew(false, date1, "OPT_OUT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", "ADDRESS"),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetChecksRefusedDifferentDate() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    setHdcCheck(false, date3)

    assertOffenderCurfew(false, date3, "OPT_OUT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", "ADDRESS"),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetChecksPassed() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    setHdcCheck(true, date3)

    assertOffenderCurfew(true, date3, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_PASS", "MAN_CK"),
      statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetSameApprovalStatusAndDate() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)

    assertOffenderCurfew(false, date1, "OPT_OUT", date2)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", "ADDRESS"),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetDifferentApprovalStatusAndDate() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    setApprovalStatus("PP INVEST", "CJ/CS_2000", date3)

    assertOffenderCurfew(false, date1, "PP INVEST", date3)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", "CJ/CS_2000"),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenSetApproved() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)

    assertThatThrownBy { setApprovedStatus(date3) }.isInstanceOf(
      IllegalStateException::class.java,
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenDeleteApprovalStatus() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    deleteApprovalStatus()

    assertOffenderCurfew(false, date1, null, null)
    assertStatusAndReasons(
      statusAndReason("MAN_CK_FAIL", null),
      statusAndReason("INELIGIBLE", "MAN_CK_FAIL"),
    )
  }

  @Test
  fun givenChecksFailedRefusedState_whenDeleteHdcChecks() {
    setHdcCheck(false, date1)
    setApprovalStatus("OPT_OUT", "ADDRESS", date2)
    deleteHdcCheck()

    assertOffenderCurfew(null, null, null, null)
    assertStatusAndReasons()
  }

  private fun setHdcCheck(passed: Boolean, date: LocalDate?) {
    offenderCurfewService.setHdcChecks(OFFENDER_BOOKING_ID, HdcChecks.builder().passed(passed).date(date).build())
  }

  private fun deleteHdcCheck() {
    offenderCurfewService.deleteHdcChecks(OFFENDER_BOOKING_ID)
  }

  private fun setApprovedStatus(date: LocalDate?) {
    offenderCurfewService.setApprovalStatus(
      OFFENDER_BOOKING_ID,
      ApprovalStatus.builder().approvalStatus("APPROVED").date(date).build(),
    )
  }

  private fun setApprovalStatus(approvalStatus: String?, refusedReason: String?, date: LocalDate?) {
    offenderCurfewService.setApprovalStatus(
      OFFENDER_BOOKING_ID,
      ApprovalStatus.builder().approvalStatus(approvalStatus).refusedReason(refusedReason).date(date).build(),
    )
  }

  private fun deleteApprovalStatus() {
    offenderCurfewService.deleteApprovalStatus(OFFENDER_BOOKING_ID)
  }

  private fun assertOffenderCurfew(
    passed: Boolean?,
    checksPassedDate: LocalDate?,
    approvalStatus: String?,
    approvalStatusDate: LocalDate?,
  ) {
    val actual = jdbcTemplate.queryForObject(
      "SELECT OFFENDER_CURFEW_ID, PASSED_FLAG, ASSESSMENT_DATE, APPROVAL_STATUS, DECISION_DATE " +
        "FROM OFFENDER_CURFEWS " +
        "WHERE OFFENDER_BOOK_ID = :bookingId AND " +
        "OFFENDER_CURFEW_ID = (SELECT MAX(OFFENDER_CURFEW_ID) FROM OFFENDER_CURFEWS WHERE OFFENDER_BOOK_ID = :bookingId)",
      mapOf("bookingId" to OFFENDER_BOOKING_ID),
      DataClassRowMapper(OffenderCurfew::class.java),
    )

    assertThat(actual).isEqualTo(
      OffenderCurfew(
        curfewId,
        fromBoolean(passed),
        checksPassedDate,
        approvalStatus,
        approvalStatusDate,
      ),
    )
  }

  private fun assertHomeDetentionCurfew(
    passed: Boolean?,
    checksPassedDate: LocalDate?,
    approvalStatus: String?,
    approvalStatusDate: LocalDate?,
    refusedReason: String?,
  ) {
    val actual = offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID)
    assertThat(actual).isEqualTo(
      HomeDetentionCurfew
        .builder()
        .id(curfewId)
        .passed(passed)
        .checksPassedDate(checksPassedDate)
        .approvalStatus(approvalStatus)
        .approvalStatusDate(approvalStatusDate)
        .refusedReason(refusedReason)
        .build(),
    )
  }

  private fun assertStatusAndReasons(vararg expectedStatusAndReasons: StatusAndReason?) {
    val actual = jdbcTemplate.query(
      "SELECT STATUS_CODE AS STATUS, STATUS_REASON_CODE AS REASON " +
        "FROM HDC_STATUS_TRACKINGS ST " +
        "LEFT JOIN HDC_STATUS_REASONS HSR on ST.HDC_STATUS_TRACKING_ID = HSR.HDC_STATUS_TRACKING_ID " +
        "JOIN OFFENDER_CURFEWS OC on ST.OFFENDER_CURFEW_ID = OC.OFFENDER_CURFEW_ID " +
        "WHERE OC.OFFENDER_BOOK_ID = :bookingId AND " +
        "OC.OFFENDER_CURFEW_ID = (SELECT MAX(OFFENDER_CURFEW_ID) FROM OFFENDER_CURFEWS WHERE OFFENDER_BOOK_ID = :bookingId)",
      mapOf("bookingId" to OFFENDER_BOOKING_ID),
      DataClassRowMapper(StatusAndReason::class.java),
    )

    assertThat(actual).containsExactlyInAnyOrder(*expectedStatusAndReasons)
  }

  data class OffenderCurfew(
    val offenderCurfewId: Long?,
    val passedFlag: String?,
    val assessmentDate: LocalDate?,
    val approvalStatus: String?,
    val decisionDate: LocalDate?,
  )

  data class StatusAndReason(
    val status: String?,
    val reason: String?,
  )

  companion object {
    private const val OFFENDER_BOOKING_ID = -51L

    private val date1: LocalDate = LocalDate.of(2019, 1, 2)
    private val date2: LocalDate = LocalDate.of(2019, 3, 4)
    private val date3: LocalDate = LocalDate.of(2019, 5, 6)

    fun fromBoolean(b: Boolean?): String? {
      if (b == null) return null
      return if (b) "Y" else "N"
    }

    fun statusAndReason(status: String, reason: String?): StatusAndReason = StatusAndReason(status, reason)
  }
}
