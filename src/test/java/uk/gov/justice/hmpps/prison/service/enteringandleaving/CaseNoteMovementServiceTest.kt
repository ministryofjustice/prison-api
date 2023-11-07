package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

internal class CaseNoteMovementServiceTest {
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
        },
      ),
    )
    whenever(caseNoteRepository.save(any())).thenAnswer { it.getArgument<OffenderCaseNote>(0) }
  }

  private val service = CaseNoteMovementService(
    caseNoteRepository = caseNoteRepository,
    caseNoteTypeReferenceCodeRepository = caseNoteTypeReferenceCodeRepository,
    caseNoteSubTypeReferenceCodeRepository = caseNoteSubTypeReferenceCodeRepository,
    staffUserAccountRepository = staffUserAccountRepository,
    authenticationFacade = authenticationFacade,
  )

  @Nested
  inner class CreateGenerateAdmissionNote {
    private lateinit var fromPrison: AgencyLocation
    private lateinit var toPrison: AgencyLocation
    private lateinit var booking: OffenderBooking
    private lateinit var movement: ExternalMovement

    @BeforeEach
    internal fun setUp() {
      fromPrison = AgencyLocation().apply { description = "HMP Brixton" }
      toPrison = AgencyLocation().apply { description = "HMP Wandsworth" }
      booking = OffenderBooking().apply { bookingId = 99L; location = toPrison }
      movement = ExternalMovement().apply {
        offenderBooking = booking
        movementTime = LocalDateTime.parse("2020-01-01T00:00:00")
        this.fromAgency = fromPrison
        this.toAgency = toPrison
        movementReason = MovementReason().apply { code = "TRANSFERRED"; description = "Transferred" }
      }
    }

    @Test
    internal fun `will create case note with transfer type`() {
      whenever(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("TRANSFER"))).thenReturn(
        Optional.of(
          CaseNoteType().apply { code = "TRANSFER"; description = "Transfer" },
        ),
      )
      whenever(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("FROMTOL"))).thenReturn(
        Optional.of(
          CaseNoteSubType().apply { code = "FROMTOL"; description = "From Transfer" },
        ),
      )

      service.createGenerateAdmissionNote(booking, movement)

      verify(caseNoteRepository).save(
        check {
          assertThat(it.type.code).isEqualTo("TRANSFER")
          assertThat(it.subType.code).isEqualTo("FROMTOL")
          assertThat(it.offenderBooking).isEqualTo(booking)
          assertThat(it.agencyLocation).isEqualTo(toPrison)
          assertThat(it.noteSourceCode).isEqualTo("AUTO")
          assertThat(it.author).isEqualTo(loggedInStaff)
          assertThat(it.occurrenceDate).isEqualTo(LocalDate.parse("2020-01-01"))
          assertThat(it.occurrenceDateTime).isEqualTo(LocalDateTime.parse("2020-01-01T00:00:00"))
          assertThat(it.caseNoteText).isEqualTo("Offender admitted to HMP Wandsworth for reason: Transferred from HMP Brixton.")
        },
      )
    }

    @Test
    internal fun `will throw exception when case note type not found`() {
      whenever(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("TRANSFER"))).thenReturn(
        Optional.empty(),
      )

      assertThrows<EntityNotFoundException> {
        service.createGenerateAdmissionNote(booking, movement)
      }
    }

    @Test
    internal fun `will throw exception when case note sub type not found`() {
      whenever(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("FROMTOL"))).thenReturn(
        Optional.empty(),
      )

      assertThrows<EntityNotFoundException> {
        service.createGenerateAdmissionNote(booking, movement)
      }
    }
  }

  @Nested
  inner class CreateReleaseNote {
    private lateinit var fromPrison: AgencyLocation
    private lateinit var out: AgencyLocation
    private lateinit var booking: OffenderBooking
    private lateinit var movement: ExternalMovement

    @BeforeEach
    internal fun setUp() {
      fromPrison = AgencyLocation().apply { description = "HMP Brixton" }
      out = AgencyLocation().apply { id = "OUT"; description = "Out" }
      booking = OffenderBooking().apply { bookingId = 99L; location = out }
      movement = ExternalMovement().apply {
        offenderBooking = booking
        movementTime = LocalDateTime.parse("2020-01-01T00:00:00")
        this.fromAgency = fromPrison
        this.toAgency = out
        movementReason = MovementReason().apply { code = "TRANSFERRED"; description = "Transferred" }
      }
    }

    @Test
    internal fun `will create case note for release`() {
      booking = OffenderBooking().apply { bookingId = 99L; location = out }
      movement = ExternalMovement().apply {
        offenderBooking = booking
        movementTime = LocalDateTime.parse("2020-01-01T00:00:00")
        this.fromAgency = fromPrison
        this.toAgency = out
        movementReason = MovementReason().apply { code = "CR"; description = "Conditional Release" }
      }
      whenever(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk("PRISON"))).thenReturn(
        Optional.of(CaseNoteType().apply { code = "PRISON"; description = "Prison" }),
      )
      whenever(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk("RELEASE"))).thenReturn(
        Optional.of(CaseNoteSubType().apply { code = "RELEASE"; description = "Release" }),
      )

      service.createReleaseNote(booking, movement)

      verify(caseNoteRepository).save(
        check {
          assertThat(it.type.code).isEqualTo("PRISON")
          assertThat(it.subType.code).isEqualTo("RELEASE")
          assertThat(it.offenderBooking).isEqualTo(booking)
          assertThat(it.agencyLocation).isEqualTo(out)
          assertThat(it.noteSourceCode).isEqualTo("AUTO")
          assertThat(it.author).isEqualTo(loggedInStaff)
          assertThat(it.occurrenceDate).isEqualTo(LocalDate.parse("2020-01-01"))
          assertThat(it.occurrenceDateTime).isEqualTo(LocalDateTime.parse("2020-01-01T00:00:00"))
          assertThat(it.caseNoteText).isEqualTo("Released from HMP Brixton for reason: Conditional Release.")
        },
      )
    }
  }
}
