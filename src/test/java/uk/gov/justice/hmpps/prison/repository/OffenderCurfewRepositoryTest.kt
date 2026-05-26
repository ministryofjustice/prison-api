package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus
import uk.gov.justice.hmpps.prison.api.model.HdcChecks
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.lang.Boolean
import java.sql.Timestamp
import java.time.LocalDate

@ActiveProfiles("test")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class OffenderCurfewRepositoryTest(
  @Autowired private val repository: OffenderCurfewRepository,
  @Autowired private val jdbcTemplate: NamedParameterJdbcOperations,
) {
  @Test
  fun shouldReturnAllOffenderCurfewForAgencyLEI() {
    assertThat(repository.offenderCurfews(agencyIds("LEI"))).containsAll(CURFEWS_LEI)
  }

  @Test
  fun shouldReturnAllOffenderCurfewForAgencyBXI() {
    assertThat(repository.offenderCurfews(agencyIds("BXI"))).containsAll(CURFEWS_BXI)
  }

  @Test
  fun shouldReturnAllOffenderCurfewForAgencyBXIandLEI() {
    assertThat(repository.offenderCurfews(agencyIds("LEI", "BXI"))).containsAll(
      CURFEWS_LEI.union(CURFEWS_BXI),
    )
  }

  @Test
  fun shouldNotFindCurfewForUnknownBookingId() {
    val hdcOptional = repository.getLatestHomeDetentionCurfew(UNKNOWN_BOOKING_ID, TRACKING_CODES_TO_MATCH)
    assertThat(hdcOptional).isNotPresent()
  }

  @Test
  fun shouldNotFindCurfewForBookingThatHasNoCurfew() {
    val hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_WITHOUT_CURFEW_ID, TRACKING_CODES_TO_MATCH)
    assertThat(hdcOptional).isNotPresent()
  }

  @Test
  fun shouldFindCurfewForBookingThatHasCurfew() {
    val hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_1_ID, TRACKING_CODES_TO_MATCH)
    assertThat(hdcOptional).contains(
      HomeDetentionCurfew.builder().id(
        BOOKING_1_CURFEW_ID,
      ).build(),
    )
  }

  @Test
  fun shouldAddNewCurfewToBooking() {
    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    ).isEmpty()

    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .hasValueSatisfying {
        assertThat(it.id).isEqualTo(curfewId)
      }
  }

  @Test
  fun shouldSetHDCChecksPassedToY() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertCurfewHDCChecksPassedEqualTo(curfewId, null, null)

    val date = LocalDate.of(2018, 1, 31)

    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(date).build())

    assertCurfewHDCChecksPassedEqualTo(curfewId, "Y", date)
  }

  @Test
  fun shouldSetHDCChecksPassedToN() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertCurfewHDCChecksPassedEqualTo(curfewId, null, null)

    val date = LocalDate.of(2018, 1, 31)

    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date).build())

    assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date)
  }

  @Test
  fun shouldSetHdcChecksPassedDate() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    val date1 = LocalDate.of(2018, 1, 2)

    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date1).build())
    assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date1)

    val date2 = LocalDate.of(2019, 2, 3)

    repository.setHdcChecksPassedDate(curfewId, date2)
    assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date2)
  }

  @Test
  fun shouldSetApprovalStatusDate() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    val date1 = LocalDate.of(2018, 1, 2)

    repository.setApprovalStatus(curfewId, ApprovalStatus.builder().approvalStatus("XXX").date(date1).build())

    assertCurfewEqualTo(curfewId, null, null, "XXX", date1)

    val date2 = LocalDate.of(2019, 2, 3)

    repository.setApprovalStatusDate(curfewId, date2)
    assertCurfewEqualTo(curfewId, null, null, "XXX", date2)
  }

  @Test
  fun givenCurfewWithHdcCheckPassed_thenShouldRetrieveThatCurfewData() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .hasValue(HomeDetentionCurfew.builder().id(curfewId).build())

    val date = LocalDate.of(2018, 1, 31)

    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(date).build())

    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .hasValue(HomeDetentionCurfew.builder().id(curfewId).passed(Boolean.TRUE).checksPassedDate(date).build())
  }

  @Test
  fun givenCurfewWithHdcCheckNotPassed_thenShouldRetrieveThatCurfewData() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .hasValue(HomeDetentionCurfew.builder().id(curfewId).build())

    val date = LocalDate.of(2018, 1, 30)

    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date).build())

    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .hasValue(HomeDetentionCurfew.builder().id(curfewId).passed(Boolean.FALSE).checksPassedDate(date).build())
  }

  @Test
  fun shouldCreateHdcStatusTracking() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    val statusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)
    assertCurfewHasStatusTracking(curfewId, statusTrackingId, STATUS_TRACKING_CODE_REFUSED)
  }

  @Test
  fun shouldCreateHdcStatusReason() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    val hdcStatusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)
    repository.createHdcStatusReason(hdcStatusTrackingId, "XXXX")
    assertStatusTrackingHasStatusReason(hdcStatusTrackingId, "XXXX")
  }

  @Test
  fun shouldFindHdcStatusTracking() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    assertThat(repository.findHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)).isEmpty()

    val hdcStatusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)

    assertThat(repository.findHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED))
      .hasValue(hdcStatusTrackingId)
  }

  @Test
  fun givenHdcChecksSetTrue_thenTriggersShouldFire() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(LocalDate.now()).build())
    val manCkPass = repository.findHdcStatusTracking(curfewId, "MAN_CK_PASS")
    assertThat(manCkPass).isNotEmpty()
    assertStatusTrackingHasStatusReason(manCkPass.getAsLong(), "MAN_CK")

    val eligible = repository.findHdcStatusTracking(curfewId, "ELIGIBLE")
    assertThat(eligible).isNotEmpty()
    assertStatusTrackingHasStatusReason(eligible.getAsLong(), "PASS_ALL_CK")
  }

  @Test
  fun givenHdcChecksSetFalse_thenTriggersShouldFire() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(LocalDate.now()).build())
    val manCkFail = repository.findHdcStatusTracking(curfewId, "MAN_CK_FAIL")
    assertThat(manCkFail).isNotEmpty()

    val ineligible = repository.findHdcStatusTracking(curfewId, "INELIGIBLE")
    assertThat(ineligible).isNotEmpty()
    assertStatusTrackingHasStatusReason(ineligible.getAsLong(), "MAN_CK_FAIL")
  }

  @Test
  fun shouldSetApprovalStatus() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)

    val date = LocalDate.of(2018, 1, 2)
    repository.setApprovalStatus(
      curfewId,
      ApprovalStatus.builder()
        .approvalStatus("APPROVED")
        .date(date)
        .build(),
    )

    assertCurfewEqualTo(curfewId, null, null, "APPROVED", date)
  }

  @Test
  fun givenRefusedTrackingStatusAndReason_thenReasonShouldBeRetrieved() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)

    val trackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)
    repository.createHdcStatusReason(trackingId, "YYY")

    assertThat(
      repository.getLatestHomeDetentionCurfew(
        BOOKING_2_ID,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .contains(HomeDetentionCurfew.builder().id(curfewId).refusedReason("YYY").build())
  }

  @Test
  fun getBatchOfCurfews_shouldReturnAnEmptyList() {
    val listOfBookingIds = mutableListOf(1L, 2L)
    assertThat(
      repository.getBatchLatestHomeDetentionCurfew(
        listOfBookingIds,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .isEmpty()
  }

  @Test
  fun getBatchOfCurfews_shouldRetrieveLatestStatusForMatchingBookingIds() {
    val listOfBookingIds = listOf(-1L)

    val curfewId1 = createNewCurfewForBookingId(-1L, jdbcTemplate)
    val trackingId1 = repository.createHdcStatusTracking(curfewId1, STATUS_TRACKING_CODE_REFUSED)
    repository.createHdcStatusReason(trackingId1, "CHECKING")

    val curfewId2 = createNewCurfewForBookingId(-1L, jdbcTemplate)
    val trackingId2 = repository.createHdcStatusTracking(curfewId2, STATUS_TRACKING_CODE_REFUSED)
    repository.createHdcStatusReason(trackingId2, "UNDER_14DAYS")

    // Should only retrieve the latest curfew status for a bookingId
    assertThat(
      repository.getBatchLatestHomeDetentionCurfew(
        listOfBookingIds,
        TRACKING_CODES_TO_MATCH,
      ),
    )
      .extracting("id", "refusedReason").containsOnly(Tuple.tuple(curfewId2, "UNDER_14DAYS"))
  }

  @Test
  fun shouldDeleteStatusTrackings() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)
    repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_MANUAL_FAIL)

    assertThat(statusTrackingCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(2)

    repository.deleteStatusTrackings(curfewId, TRACKING_CODES_TO_MATCH)
    assertThat(statusTrackingCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(0)
  }

  @Test
  fun shouldDeleteStatusReasons() {
    val curfewId = createNewCurfewForBookingId(BOOKING_2_ID, jdbcTemplate)
    val trackingId1 = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)
    val trackingId2 = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_MANUAL_FAIL)

    repository.createHdcStatusReason(trackingId1, "A")
    repository.createHdcStatusReason(trackingId2, "B")
    assertThat(statusReasonCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(2)

    repository.deleteStatusReasons(curfewId, TRACKING_CODES_TO_MATCH)
    assertThat(statusReasonCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(0)
  }

  private fun assertCurfewHDCChecksPassedEqualTo(bookingId: Long, passedFlag: String?, assessmentDate: LocalDate?) {
    assertCurfewEqualTo(bookingId, passedFlag, assessmentDate, null, null)
  }

  private fun assertCurfewEqualTo(
    curfewId: Long,
    passedFlag: String?,
    assessmentDate: LocalDate?,
    approvalStatus: String?,
    decisionDate: LocalDate?,
  ) {
    val results = jdbcTemplate.queryForMap(
      "SELECT PASSED_FLAG, ASSESSMENT_DATE, DECISION_DATE, APPROVAL_STATUS FROM OFFENDER_CURFEWS WHERE OFFENDER_CURFEW_ID = :curfewId",
      mapOf("curfewId" to curfewId),
    )
    assertThat(results["PASSED_FLAG"]).isEqualTo(passedFlag)
    assertThat(results["ASSESSMENT_DATE"])
      .isEqualTo(if (assessmentDate == null) null else Timestamp.valueOf(assessmentDate.atStartOfDay()))
    assertThat(results["APPROVAL_STATUS"]).isEqualTo(approvalStatus)
    assertThat(results["DECISION_DATE"])
      .isEqualTo(if (decisionDate == null) null else Timestamp.valueOf(decisionDate.atStartOfDay()))
  }

  private fun statusTrackingCount(curfewId: Long, codesToMatch: Set<String>): Int {
    val count = jdbcTemplate.queryForObject(
      "SELECT count(*) from HDC_STATUS_TRACKINGS WHERE OFFENDER_CURFEW_ID = :curfewId AND STATUS_CODE IN (:codesToMatch)",
      mapOf(
        "curfewId" to curfewId,
        "codesToMatch" to codesToMatch,
      ),
      Int::class.java,
    )
    if (count != null) {
      return count
    }
    fail<String>("No count value. This shouldn't happen!")
    return -1 // Unreachable!
  }

  private fun statusReasonCount(curfewId: Long, codesToMatch: Set<String>): Int {
    val count = jdbcTemplate.queryForObject(
      "SELECT count(*) from HDC_STATUS_REASONS SR JOIN HDC_STATUS_TRACKINGS ST ON SR.HDC_STATUS_TRACKING_ID = ST.HDC_STATUS_TRACKING_ID WHERE ST.OFFENDER_CURFEW_ID = :curfewId AND ST.STATUS_CODE IN (:codesToMatch)",
      mapOf(
        "curfewId" to curfewId,
        "codesToMatch" to codesToMatch,
      ),
      Int::class.java,
    )
    if (count != null) {
      return count
    }
    fail<String>("No count value. This shouldn't happen!")
    return -1 // Unreachable!
  }

  private fun assertCurfewHasStatusTracking(curfewId: Long, statusTrackingId: Long, statusCode: String) {
    assertThat(
      jdbcTemplate.queryForObject(
        "SELECT STATUS_CODE FROM HDC_STATUS_TRACKINGS WHERE HDC_STATUS_TRACKING_ID = :trackingId AND OFFENDER_CURFEW_ID = :curfewId",
        mapOf(
          "curfewId" to curfewId,
          "trackingId" to statusTrackingId,
        ),
        String::class.java,
      ),
    )
      .isEqualTo(statusCode)
  }

  private fun assertStatusTrackingHasStatusReason(hdcTrackingId: Long, statusReasonCode: String?) {
    assertThat(
      jdbcTemplate.queryForList(
        "SELECT STATUS_REASON_CODE FROM  HDC_STATUS_REASONS WHERE HDC_STATUS_TRACKING_ID = :trackingId",
        mapOf("trackingId" to hdcTrackingId),
        String::class.java,
      ),
    )
      .containsExactly(statusReasonCode)
  }

  companion object {
    private const val STATUS_TRACKING_CODE_REFUSED = "REFUSED"
    private const val STATUS_TRACKING_CODE_MANUAL_FAIL = "MAN_CK_FAIL"
    private val TRACKING_CODES_TO_MATCH = setOf(STATUS_TRACKING_CODE_REFUSED, STATUS_TRACKING_CODE_MANUAL_FAIL)
    private val BOOKING_1_ID: Long = -46
    private const val BOOKING_1_CURFEW_ID = 43L
    private val BOOKING_WITHOUT_CURFEW_ID = -15L
    private const val UNKNOWN_BOOKING_ID = 99999L
    private val BOOKING_2_ID = -52L

    private val CURFEWS_LEI = HashSet(
      listOf(
        offenderCurfew(1, -1, "2018-01-01", null, null),
        offenderCurfew(2, -1, "2018-02-01", null, null),
        offenderCurfew(3, -1, "2018-02-01", null, null),
        offenderCurfew(4, -2, "2018-01-01", null, null),
        offenderCurfew(5, -2, "2018-02-01", null, null),
        offenderCurfew(6, -2, "2018-02-01", null, null),
        offenderCurfew(7, -3, "2018-01-01", null, null),
        offenderCurfew(8, -3, "2018-02-01", null, null),
        offenderCurfew(9, -3, "2018-02-01", null, null),
        offenderCurfew(10, -4, "2018-01-01", null, null),
        offenderCurfew(11, -5, "2018-01-01", null, null),
        offenderCurfew(12, -6, "2018-01-01", null, null),
        offenderCurfew(13, -6, null, null, null),
        offenderCurfew(14, -6, "2018-02-01", null, null),
        offenderCurfew(15, -7, null, "REJECTED", null),
        offenderCurfew(16, -7, "2018-01-01", "APPROVED", null),
        offenderCurfew(17, -7, "2018-01-01", "APPROVED", "2018-04-01"),
        offenderCurfew(18, -7, null, null, null),
        offenderCurfew(19, -7, "2018-02-01", null, null),
      ),
    )

    private val CURFEWS_BXI = setOf(
      offenderCurfew(30, -36, null, null, null),
    )

    private fun offenderCurfew(
      offenderCurfewId: Long,
      offenderBookId: Long,
      assessmentDate: String?,
      approvalStatus: String?,
      ardCrdDate: String?,
    ): OffenderCurfew = OffenderCurfew
      .builder()
      .offenderCurfewId(offenderCurfewId)
      .offenderBookId(offenderBookId)
      .assessmentDate(toLocalDate(assessmentDate))
      .approvalStatus(approvalStatus)
      .ardCrdDate(toLocalDate(ardCrdDate))
      .build()

    private fun toLocalDate(string: String?): LocalDate? {
      if (string == null) return null
      return LocalDate.parse(string)
    }

    private fun agencyIds(vararg agencyIds: String): Set<String> = setOf(*agencyIds)

    fun createNewCurfewForBookingId(
      bookingId: Long,
      jdbcTemplate: NamedParameterJdbcOperations,
    ): Long {
      val keyHolder = GeneratedKeyHolder()

      jdbcTemplate.update(
        "INSERT INTO OFFENDER_CURFEWS (" +
          "    OFFENDER_CURFEW_ID, " +
          "    OFFENDER_BOOK_ID, " +
          "    ELIGIBILITY_DATE " +
          ") VALUES (" +
          "    OFFENDER_CURFEW_ID.NEXTVAL, " +
          "    :bookingId," +
          "    sysdate)",
        MapSqlParameterSource("bookingId", bookingId),
        keyHolder,
        arrayOf("OFFENDER_CURFEW_ID"),
      )

      return keyHolder.key!!.toLong()
    }
  }
}
