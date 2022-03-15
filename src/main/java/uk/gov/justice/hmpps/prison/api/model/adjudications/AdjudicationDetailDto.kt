package uk.gov.justice.hmpps.prison.api.model.adjudications

import java.time.LocalDateTime

data class AdjudicationDetailDto(
  val adjudicationNumber: Long?,
  val incidentTime: LocalDateTime?,
  val establishment: String?,
  val agencyId: String?,
  val interiorLocation: String?,
  val internalLocationId: Long,
  val incidentDetails: String?,
  val reportNumber: Long?,
  val reportType: String?,
  val reporterFirstName: String?,
  val reporterLastName: String?,
  val reportTime: LocalDateTime?,
) {
  fun toAdjudicationDetail() = AdjudicationDetail(
    this.adjudicationNumber,
    this.incidentTime,
    this.establishment,
    this.agencyId,
    this.interiorLocation,
    this.internalLocationId,
    this.incidentDetails,
    this.reportNumber,
    this.reportType,
    this.reporterFirstName,
    this.reporterLastName,
    this.reportTime,
    null,
  )
}
