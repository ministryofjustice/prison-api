package uk.gov.justice.hmpps.prison.service

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangMemberRepository
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

@Service
@Transactional(readOnly = true)
class GangService(
  private val gangMemberRepository: GangMemberRepository,
) {

  fun getNonAssociatesInGangs(offenderNo: String): GangMemberSummary {
    val gangsInvolved = gangMemberRepository.findAllByBookingOffenderNomsIdAndGangActiveIsTrue(offenderNo)
    if (gangsInvolved.isEmpty()) {
      throw EntityNotFoundException("No gangs found for offender $offenderNo")
    }

    val currentBooking = gangsInvolved[0].booking

    return GangMemberSummary(
      member = gangMemberDetail(currentBooking),
      currentGangs = gangsInvolved.map { gang ->
        GangSummary(
          code = gang.gang.code,
          name = gang.gang.name,
          comment = gang.commentText,
          numberOfMembers = gang.gang.members.size.toLong(),
        )
      },
      gangNonAssociations = gangsInvolved.map { gangMember ->
        gangMember.gang.getNonAssociations()
          .filter { (naGang, _) -> naGang.active }
          .map { (naGang, reason) ->
            GangNonAssociationSummary(
              code = naGang.code,
              name = naGang.name,
              reason = reason.description,
              members = naGang.members.map { member -> gangMemberDetail(member.booking) },
            )
          }
      }.flatten(),
    )
  }

  private fun gangMemberDetail(currentBooking: OffenderBooking) =
    GangMemberDetail(
      offenderNo = currentBooking.offender.nomsId,
      firstName = currentBooking.offender.firstName,
      lastName = currentBooking.offender.lastName,
      prisonId = currentBooking.location.id,
      prisonName = LocationProcessor.formatLocation(currentBooking.location.description),
      cellLocation = currentBooking.assignedLivingUnit?.description,
    )
}

@Schema(description = "Summary of Gangs for a specified prisoner")
data class GangMemberSummary(
  @Schema(description = "The details of the gang member")
  val member: GangMemberDetail?,
  @Schema(description = "Current gang involvement")
  val currentGangs: List<GangSummary>,
  @Schema(description = "Non associations with other gangs")
  val gangNonAssociations: List<GangNonAssociationSummary>,
)

@Schema(description = "Gang Summary")
data class GangSummary(
  @Schema(description = "The code assigned for the gang", example = "A_GANG")
  val code: String,
  @Schema(description = "The name of the gang", example = "A New Gang")
  val name: String,
  @Schema(description = "Information about this member within the gang", example = "Leader of gang")
  val comment: String?,
  @Schema(description = "Number of members in this gang", example = "15")
  val numberOfMembers: Long,
)

@Schema(description = "Non associations Gang information")
data class GangNonAssociationSummary(
  @Schema(description = "The code assigned for the gang", example = "A_GANG")
  val code: String,
  @Schema(description = "The name of the gang", example = "A New Gang")
  val name: String,
  @Schema(description = "Reason this gang should not be associated with other gang", example = "Rival Gang")
  val reason: String,
  @Schema(description = "List of members of this gang")
  val members: List<GangMemberDetail>,
)

@Schema(description = "Gang Member Detail")
data class GangMemberDetail(
  @Schema(description = "Prisoner Number of this gang member", example = "A1234AA")
  val offenderNo: String,
  @Schema(description = "First name of this gang member", example = "John")
  val firstName: String,
  @Schema(description = "Last name of this gang member", example = "Smith")
  val lastName: String,
  @Schema(description = "Prison ID if inside or OUT if not inside", example = "MDI")
  val prisonId: String?,
  @Schema(description = "Name of the prison or Outside if not inside", name = "Moorland")
  val prisonName: String?,
  @Schema(description = "Cell location of the gang member (if inside)", example = "MDI-A-1-001")
  val cellLocation: String?,
)
