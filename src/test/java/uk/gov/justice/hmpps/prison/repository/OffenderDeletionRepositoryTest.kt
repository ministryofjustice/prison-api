package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Condition
import org.assertj.core.api.ListAssert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForList
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.util.function.Predicate

@JdbcTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class OffenderDeletionRepositoryTest(
  @Autowired private val repository: OffenderDeletionRepository,
  @Autowired private val jdbcTemplate: JdbcTemplate,
) {
  @Test
  @Transactional
  fun cleanseOffenderDataToBaseRecord() {
    assertOffenderDataExists()

    assertThat(repository.cleanseOffenderDataExcludingBaseRecord("A1234AA"))
      .containsExactly(-1001L)

    assertBaseRecordExists()
    assertNonBaseRecordOffenderDataDeleted()
    assertGlTransactionsAnonymised()
  }

  @Test
  @Transactional
  fun cleanseOffenderDataUsingUnknownOffenderThrows() {
    assertThatThrownBy {
      repository.cleanseOffenderDataExcludingBaseRecord(
        "unknown",
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Resource with id [unknown] not found.")
  }

  @Test
  @Transactional
  fun deleteAllOffenderDataIncludingBaseRecord() {
    assertOffenderDataExists()

    assertThat(repository.deleteAllOffenderDataIncludingBaseRecord("A1234AA"))
      .containsExactly(-1001L)

    assertAllOffenderDataDeleted()
    assertGlTransactionsAnonymised()
  }

  @Test
  @Transactional
  fun deleteAllOffenderDataUsingUnknownOffenderThrows() {
    assertThatThrownBy {
      repository.deleteAllOffenderDataIncludingBaseRecord(
        "unknown",
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Resource with id [unknown] not found.")
  }

  private fun assertOffenderDataExists() {
    checkAllTables(
      Condition(
        Predicate { !it.isEmpty() },
        "Entry Found",
      ),
    )
  }

  private fun assertAllOffenderDataDeleted() {
    checkAllTables(
      Condition(
        Predicate { it.isEmpty() },
        "Entry Not Found",
      ),
    )
  }

  private fun assertNonBaseRecordOffenderDataDeleted() {
    checkNonBaseRecordTables(
      Condition(
        Predicate { it.isEmpty() },
        "Entry Not Found",
      ),
    )
  }

  private fun assertBaseRecordExists() {
    checkBaseRecord(
      Condition(
        Predicate { !it.isEmpty() },
        "Entry is Found",
      ),
    )
  }

  private fun checkAllTables(condition: Condition<in MutableList<out String>>) {
    checkBaseRecord(condition)
    checkNonBaseRecordTables(condition)
  }

  private fun checkNonBaseRecordTables(condition: Condition<in MutableList<out String>>) {
    queryForCourtEventCharges().`is`(condition)

    queryByHealthProblemId("OFFENDER_MEDICAL_TREATMENTS").`is`(condition)
    queryByHealthProblemId("OFFENDER_HEALTH_PROBLEMS").`is`(condition)

    queryByProgramId("OFFENDER_COURSE_ATTENDANCES").`is`(condition)
    queryByProgramId("OFFENDER_PRG_PRF_PAY_BANDS").`is`(condition)
    queryByProgramId("OFFENDER_PROGRAM_PROFILES").`is`(condition)

    queryByAgencyIncidentId("AGENCY_INCIDENT_REPAIRS").`is`(condition)
    queryByAgencyIncidentId("AGENCY_INCIDENT_CHARGES").`is`(condition)
    queryByAgencyIncidentId("AGENCY_INCIDENT_PARTIES").`is`(condition)
    queryByAgencyIncidentId("AGENCY_INCIDENTS").`is`(condition)

    queryByIncidentCaseId("INCIDENT_CASES").`is`(condition)
    queryByIncidentCaseId("INCIDENT_CASE_QUESTIONS").`is`(condition)
    queryByIncidentCaseId("INCIDENT_CASE_RESPONSES").`is`(condition)
    queryByIncidentCaseId("INCIDENT_CASE_REQUIREMENTS").`is`(condition)

    queryByOffenderBookId("INCIDENT_CASE_PARTIES").`is`(condition)
    queryByOffenderBookId("BED_ASSIGNMENT_HISTORIES").`is`(condition)
    queryByOffenderBookId("COURT_EVENTS").`is`(condition)
    queryByOffenderBookId("OFFENDER_CASE_NOTES").`is`(condition)
    queryByOffenderBookId("OFFENDER_CASES").`is`(condition)
    queryByOffenderBookId("OFFENDER_CONTACT_PERSONS").`is`(condition)
    queryByOffenderBookId("OFFENDER_CURFEWS").`is`(condition)
    queryByOffenderBookId("OFFENDER_IEP_LEVELS").`is`(condition)
    queryByOffenderBookId("OFFENDER_IMPRISON_STATUSES").`is`(condition)
    queryByOffenderBookId("OFFENDER_IND_SCHEDULES").`is`(condition)
    queryByOffenderBookId("OFFENDER_KEY_DATE_ADJUSTS").`is`(condition)
    queryByOffenderBookId("OFFENDER_KEY_WORKERS").`is`(condition)
    queryByOffenderBookId("OFFENDER_LANGUAGES").`is`(condition)
    queryByOffenderBookId("OFFENDER_OIC_SANCTIONS").`is`(condition)
    queryByOffenderBookId("OFFENDER_PRG_OBLIGATIONS").`is`(condition)
    queryByOffenderBookId("OFFENDER_RELEASE_DETAILS").`is`(condition)
    queryByOffenderBookId("OFFENDER_VISIT_VISITORS").`is`(condition)
    queryByOffenderBookId("OFFENDER_VISITS").`is`(condition)
    queryByOffenderBookId("OFFENDER_VISIT_BALANCES").`is`(condition)
    queryByOffenderBookId("OFFENDER_CHARGES").`is`(condition)
    queryByOffenderBookId("OFFENDER_SENTENCE_TERMS").`is`(condition)
    queryByOffenderBookId("OFFENDER_SENTENCES").`is`(condition)
    queryByOffenderBookId("ORDERS").`is`(condition)
    queryByOffenderBookId("OFFENDER_BELIEFS").`is`(condition)

    queryByRootOffenderId("OFFENDER_IMMIGRATION_APPEALS").`is`(condition)

    queryByOffenderId("GL_TRANSACTIONS").`is`(condition)
    queryByOffenderId("OFFENDER_SUB_ACCOUNTS").`is`(condition)
    queryByOffenderId("OFFENDER_TRANSACTIONS").`is`(condition)
    queryByOffenderId("OFFENDER_TRUST_ACCOUNTS").`is`(condition)
  }

  private fun checkBaseRecord(condition: Condition<in MutableList<out String>>) {
    // Identity
    queryByOffenderBookId("OFFENDER_IMAGES").`is`(condition)
    queryByOffenderBookId("OFFENDER_PHYSICAL_ATTRIBUTES").`is`(condition)
    queryByOffenderBookId("OFFENDER_PROFILE_DETAILS").`is`(condition)
    queryByOffenderBookId("OFFENDER_IDENTIFYING_MARKS").`is`(condition)

    // Security
    queryByOffenderBookId("OFFENDER_ALERTS").`is`(condition)
    queryByOffenderBookId("OFFENDER_BOOKING_DETAILS").`is`(condition)
    queryByOffenderBookId("OFFENDER_ASSESSMENTS").`is`(condition)
    queryByOffenderBookId("OFFENDER_ASSESSMENT_ITEMS").`is`(condition)

    // Movements
    queryByOffenderBookId("OFFENDER_EXTERNAL_MOVEMENTS").`is`(condition)
    queryByOffenderBookId("OFFENDER_SENT_CALCULATIONS").`is`(condition)
    queryByOffenderBookId("HDC_CALC_EXCLUSION_REASONS").`is`(condition)

    queryByOffenderId("OFFENDER_IDENTIFIERS").`is`(condition)
    queryByOffenderId("OFFENDER_BOOKINGS").`is`(condition)
    queryByOffenderId("OFFENDERS").`is`(condition)
  }

  private fun assertGlTransactionsAnonymised() {
    assertThat(
      jdbcTemplate.queryForList<String>(
        "SELECT txn_id FROM gl_transactions WHERE txn_id = 301826802 and gl_entry_seq = 1",
      ),
    )
      .isNotEmpty()
  }

  private fun queryByAgencyIncidentId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT agency_incident_id FROM $tableName WHERE agency_incident_id IN (-6)",
    ),
  )

  private fun queryByHealthProblemId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT offender_health_problem_id FROM $tableName WHERE offender_health_problem_id IN (-201, -205, -206)",
    ),
  )

  private fun queryByProgramId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT off_prgref_id FROM $tableName WHERE off_prgref_id IN (-1, -2, -3, -4)",
    ),
  )

  private fun queryForCourtEventCharges(): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT event_id FROM court_event_charges WHERE event_id = -201 AND offender_charge_id = -1",
    ),
  )

  private fun queryByIncidentCaseId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT incident_case_id FROM $tableName WHERE incident_case_id IN (-1, -2, -3)",
    ),
  )

  private fun queryByOffenderBookId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT offender_book_id FROM $tableName WHERE offender_book_id = -1",
    ),
  )

  private fun queryByOffenderId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT offender_id FROM $tableName WHERE offender_id = -1001",
    ),
  )

  private fun queryByRootOffenderId(tableName: String): ListAssert<String> = assertThat(
    jdbcTemplate.queryForList<String>(
      "SELECT root_offender_id FROM $tableName WHERE root_offender_id = -1001",
    ),
  )
}
