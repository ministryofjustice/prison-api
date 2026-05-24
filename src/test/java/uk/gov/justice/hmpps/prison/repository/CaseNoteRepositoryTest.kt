package uk.gov.justice.hmpps.prison.repository

import jakarta.persistence.EntityManager
import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class, PersistenceConfigs::class)
@WithMockAuthUser
@Slf4j
class CaseNoteRepositoryTest(
  @Autowired private val offenderCaseNoteRepository: OffenderCaseNoteRepository,
  @Autowired private val offenderBookingRepository: OffenderBookingRepository,
  @Autowired private val caseNoteTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteType>,
  @Autowired private val caseNoteSubTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteSubType>,
  @Autowired private val staffUserAccountRepository: StaffUserAccountRepository,
  @Autowired private val jdbcTemplate: JdbcTemplate,
  @Autowired private val entityManager: EntityManager,
) {
  @Test
  fun testCreateCaseNote() {
    val startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    val bookingId: Long = -16
    val sourceCode = "source code"
    val caseNoteId = createCaseNote(bookingId, sourceCode)

    val map = jdbcTemplate.queryForMap(
      "select TIME_CREATION, CREATE_DATETIME from offender_case_notes where CASE_NOTE_ID = ?",
      caseNoteId,
    )

    val timeCreation = (map["TIME_CREATION"] as Timestamp).toLocalDateTime()
    val createDateTime = (map["CREATE_DATETIME"] as Timestamp).toLocalDateTime()

    assertThat(timeCreation).isBetween(startTime, startTime.plusSeconds(5))

    assertThat(timeCreation).isBetween(createDateTime.minusSeconds(2), createDateTime.plusSeconds(2))

    jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId)
  }

  private fun createCaseNote(bookingId: Long, sourceCode: String?): Long {
    val caseNote = OffenderCaseNote.builder()
      .caseNoteText("text")
      .type(
        caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("GEN")).orElseThrow(
          EntityNotFoundException.withId("GEN"),
        ),
      )
      .subType(
        caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("HIS"))
          .orElseThrow(EntityNotFoundException.withId("HIS")),
      )
      .noteSourceCode(sourceCode)
      .author(staffUserAccountRepository.findById("ITAG_USER").orElseThrow().getStaff())
      .occurrenceDateTime(LocalDateTime.now())
      .occurrenceDate(LocalDateTime.now().toLocalDate())
      .amendmentFlag(false)
      .offenderBooking(offenderBookingRepository.findById(bookingId).orElseThrow())
      .build()
    val id = offenderCaseNoteRepository.save<OffenderCaseNote>(caseNote).getId()
    entityManager.flush()
    return id
  }
}
