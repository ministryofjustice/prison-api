package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository
import java.time.LocalDateTime
import java.util.Optional

internal class TeamWorkflowNotificationServiceTest {
  private val offenderTeamAssignmentRepository: OffenderTeamAssignmentRepository = mock()
  private val workflowTaskService: WorkflowTaskService = mock()

  private val service = TeamWorkflowNotificationService(offenderTeamAssignmentRepository, workflowTaskService)

  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI"; }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }

  private val movement = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = "TRNCRT"; description = "Transfer via Court" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
  }

  private val booking = OffenderBooking().apply { bookingId = 99L }

  @Nested
  @DisplayName("sendTransferViaCourtNotification")
  inner class SendTransferViaCourtNotification {
    @Nested
    @DisplayName("when offender has no team assignment")
    inner class NoTeam {
      @BeforeEach
      internal fun setUp() {
        whenever(offenderTeamAssignmentRepository.findById(any())).thenReturn(Optional.empty())
      }

      @Test
      internal fun `will look for the team that deals with automatic transfer`() {
        service.sendTransferViaCourtNotification(booking) { movement }
        verify(offenderTeamAssignmentRepository).findById(
          check {
            assertThat(it.offenderBooking).isEqualTo(booking)
            assertThat(it.functionTypeCode).isEqualTo("AUTO_TRN")
          },
        )
      }

      @Test
      internal fun `will not send a notification`() {
        service.sendTransferViaCourtNotification(booking) { movement }
        verifyNoInteractions(workflowTaskService)
      }

      @Test
      internal fun `will return movement from lambda call`() {
        assertThat(service.sendTransferViaCourtNotification(booking) { movement }).isEqualTo(movement)
      }
    }

    @Nested
    @DisplayName("when offender has a team assignment")
    inner class WithTeam {
      private val team = Team(
        99L,
        "Transfer team",
        "T123",
        TeamCategory("MANAGE", "Senior Management"),
        1L,
        true,
        null,
        InstitutionArea("KENT", "Kent"),
        AgencyLocation().apply { id = "BXI"; description = "HMPS Brixton" },
        2L,
      )

      @BeforeEach
      internal fun setUp() {
        val teamAssignment: OffenderTeamAssignment = mock()
        whenever(teamAssignment.team).thenReturn(team)
        whenever(offenderTeamAssignmentRepository.findById(any())).thenReturn(Optional.of(teamAssignment))
      }

      @Test
      internal fun `will send a notification`() {
        service.sendTransferViaCourtNotification(booking) { movement }
        verify(workflowTaskService).createTaskAutomaticTransfer(booking, movement, team)
      }

      @Test
      internal fun `will return movement from lambda call`() {
        assertThat(service.sendTransferViaCourtNotification(booking) { movement }).isEqualTo(movement)
      }
    }
  }
}
