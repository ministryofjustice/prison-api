package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
data class PersonalCareNeedDto(
  val problemType: String?,
  val problemCode: String?,
  val problemStatus: String?,
  val problemDescription: String?,
  val commentText: String?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val offenderNo: String?,
) {
  fun toPersonalCareNeed() = PersonalCareNeed(
    this.problemType,
    this.problemCode,
    this.problemStatus,
    this.problemDescription,
    this.commentText,
    this.startDate,
    this.endDate,
    this.offenderNo,
  )
}
