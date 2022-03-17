package uk.gov.justice.hmpps.prison.service
import java.time.LocalDateTime

data class OffenderIepReviewDto(
  val bookingId: Long,
  val positiveIeps: Int,
  val negativeIeps: Int,
  val provenAdjudications: Int,
  val lastReviewTime: LocalDateTime?,
  val currentLevel: String?,
  val firstName: String?,
  val middleName: String?,
  val lastName: String?,
  val cellLocation: String?,
  val offenderNo: String?,
) {
  fun toOffenderIepReview() = OffenderIepReview(
    this.bookingId,
    this.positiveIeps,
    this.negativeIeps,
    this.provenAdjudications,
    this.lastReviewTime,
    this.currentLevel,
    this.firstName,
    this.middleName,
    this.lastName,
    this.cellLocation,
    this.offenderNo,
  )
}
