package uk.gov.justice.hmpps.prison.service.transfer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

internal class CaseNoteTransferServiceTest {
  private val caseNoteRepository: OffenderCaseNoteRepository = mock()
  private val caseNoteTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteType> = mock()
  private val caseNoteSubTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteSubType> = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val loggedInStaff = Staff().apply { staffId = 1L }

  @BeforeEach
  internal fun setUp() {
    whenever(authenticationFacade.currentUsername).thenReturn("TEST_USER")
    whenever(staffUserAccountRepository.findById("TEST_USER")).thenReturn(
      Optional.of(
        StaffUserAccount().apply {
          this.staff = loggedInStaff
          username = "TEST_USER"
        }
      )
    )
    whenever(caseNoteRepository.save(any())).thenAnswer { it.getArgument<OffenderCaseNote>(0) }
  }

  private val service = CaseNoteTransferService(
    caseNoteRepository = caseNoteRepository,
    caseNoteTypeReferenceCodeRepository = caseNoteTypeReferenceCodeRepository,
    caseNoteSubTypeReferenceCodeRepository = caseNoteSubTypeReferenceCodeRepository,
    staffUserAccountRepository = staffUserAccountRepository,
    authenticationFacade = authenticationFacade
  )

  @Nested
  inner class CreateGenerateAdmissionNote {
    @Test
    internal fun `will create case note with transfer type`() {
      val fromAgency = AgencyLocation().apply { description = "HMP Brixton" }
      val toAgency = AgencyLocation().apply { description = "HMP Wandsworth" }
      whenever(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("TRANSFER"))).thenReturn(
        Optional.of(
          CaseNoteType().apply { code = "TRANSFER"; description = "Transfer" }
        )
      )
      whenever(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("FROMTOL"))).thenReturn(
        Optional.of(
          CaseNoteSubType().apply { code = "FROMTOL"; description = "From Transfer" }
        )
      )
      val booking = OffenderBooking().apply { bookingId = 99L; location = toAgency }
      val movement = ExternalMovement().apply {
        offenderBooking = booking
        movementTime = LocalDateTime.parse("2020-01-01T00:00:00")
        this.fromAgency = fromAgency
        this.toAgency = toAgency
        movementReason = MovementReason().apply { code = "TRANSFERRED"; description = "Transferred" }
      }

      service.createGenerateAdmissionNote(booking, movement)

      verify(caseNoteRepository).save(
        check {
          assertThat(it.type.code).isEqualTo("TRANSFER")
          assertThat(it.subType.code).isEqualTo("FROMTOL")
          assertThat(it.offenderBooking).isEqualTo(booking)
          assertThat(it.agencyLocation).isEqualTo(toAgency)
          assertThat(it.noteSourceCode).isEqualTo("AUTO")
          assertThat(it.author).isEqualTo(loggedInStaff)
          assertThat(it.occurrenceDate).isEqualTo(LocalDate.parse("2020-01-01"))
          assertThat(it.occurrenceDateTime).isEqualTo(LocalDateTime.parse("2020-01-01T00:00:00"))
          assertThat(it.caseNoteText).isEqualTo("Offender admitted to HMP Wandsworth for reason: Transferred from HMP Brixton.")
        }
      )
    }
  }
}
