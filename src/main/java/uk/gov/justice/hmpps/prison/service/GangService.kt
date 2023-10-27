package uk.gov.justice.hmpps.prison.service

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.GangMemberRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor

@Service
@Transactional(readOnly = true)
class GangService(
  private val gangMemberRepository: GangMemberRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
) {

  fun getNonAssociatesInGangs(offenderNo: String): GangMemberSummary {
    val currentBooking = offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
      .orElseThrow(EntityNotFoundException("No booking found for offender $offenderNo"))

    val gangsInvolved = gangMemberRepository.findAllByBookingOffenderNomsIdAndGangActiveIsTrue(offenderNo)
    if (gangsInvolved.isEmpty()) {
      throw EntityNotFoundException("No gangs found for offender $offenderNo")
    }

    val gangs = gangsInvolved.map { it.gang }.filter { it.active }.distinct()

    return GangMemberSummary(
      member = gangMemberDetail(currentBooking),
      currentGangs = gangsInvolved.map { gangMember ->
        GangSummary(
          code = gangMember.gang.code,
          name = gangMember.gang.name,
          comment = gangMember.commentText,
          numberOfMembers = gangMember.gang.members.size.toLong(),
        )
      }.distinct(),

      gangNonAssociations = gangs.map { gang ->
        gang.getNonAssociations()
          .filter { (naGang, _) -> naGang.active }
          .map { (naGang, reason) ->
          GangNonAssociationSummary(
            code = naGang.code,
            name = naGang.name,
            reason = reason.description,
            members = naGang.members.filter { it.booking.isActive }.map { member -> gangMemberDetail(member.booking) },
          )
        }.distinct()
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

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GangSummary

    return code == other.code
  }

  override fun hashCode(): Int {
    return code.hashCode()
  }
}

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
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GangNonAssociationSummary

    return code == other.code
  }

  override fun hashCode(): Int {
    return code.hashCode()
  }
}

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
