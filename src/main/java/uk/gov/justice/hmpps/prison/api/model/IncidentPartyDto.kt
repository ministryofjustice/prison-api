package uk.gov.justice.hmpps.prison.api.model

data class IncidentPartyDto(
  val bookingId: Long?,
  val partySeq: Long?,
  val staffId: Long?,
  val personId: Long?,
  val participationRole: String?,
  val outcomeCode: String?,
  val commentText: String?,
  val incidentCaseId: Long?,
) {
  fun toIncidentParty() = IncidentParty(
    this.bookingId,
    this.partySeq,
    this.staffId,
    this.personId,
    this.participationRole,
    this.outcomeCode,
    this.commentText,
    this.incidentCaseId,
  )
}
