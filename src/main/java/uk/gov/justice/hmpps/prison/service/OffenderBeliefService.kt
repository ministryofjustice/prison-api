package uk.gov.justice.hmpps.prison.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class OffenderBeliefService(
  private val offenderBeliefRepository: OffenderBeliefRepository,
) {
  fun getOffenderBeliefHistory(prisonerNumber: String): List<Belief> = offenderBeliefRepository.getOffenderBeliefHistory(prisonerNumber).map(this::transformBelief)

  private fun transformBelief(offenderBelief: OffenderBelief): Belief = Belief(
    beliefCode = offenderBelief.beliefCode.id.code,
    beliefDescription = offenderBelief.beliefCode.description,
    startDate = offenderBelief.startDate,
    endDate = offenderBelief.endDate,
    changeReason = offenderBelief.changeReason,
    comments = offenderBelief.comments,
    addedByFirstName = offenderBelief.createdByUser.staff.firstName,
    addedByLastName = offenderBelief.createdByUser.staff.lastName,
    addedByUsername = offenderBelief.createdByUser.username,
    updatedByFirstName = offenderBelief.modifiedByUser?.staff?.firstName,
    updatedByLastName = offenderBelief.modifiedByUser?.staff?.lastName,
    updatedByUsername = offenderBelief.modifiedByUser?.username,
    updatedDate = offenderBelief.modifyDatetime?.toLocalDate(),
    verified = offenderBelief.verified,
  )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Belief(
  @Schema(description = "Belief Code", example = "SCIE")
  val beliefCode: String,

  @Schema(description = "Description associated with the belief code", example = "Scientologist")
  val beliefDescription: String,

  @Schema(description = "Date the belief started", example = "2024-01-01")
  val startDate: LocalDate,

  @Schema(description = "Date the belief ended", example = "2024-12-12")
  val endDate: LocalDate? = null,

  @Schema(description = "Was a reason given for change of belief?")
  val changeReason: Boolean? = null,

  @Schema(description = "Comments describing reason for change of belief")
  val comments: String? = null,

  @Schema(description = "First name of staff member that added belief")
  val addedByFirstName: String,

  @Schema(description = "Last name of staff member that added belief")
  val addedByLastName: String,

  @Schema(description = "Username of staff member that added belief")
  val addedByUsername: String,

  @Schema(description = "First name of staff member that updated belief")
  val updatedByFirstName: String? = null,

  @Schema(description = "Last name of staff member that updated belief")
  val updatedByLastName: String? = null,

  @Schema(description = "Username of staff member that updated belief")
  val updatedByUsername: String? = null,

  @Schema(description = "Date belief was updated")
  val updatedDate: LocalDate? = null,

  @Schema(description = "Verified flag")
  val verified: Boolean? = null,
)
