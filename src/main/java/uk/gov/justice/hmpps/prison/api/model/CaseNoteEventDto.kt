package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class CaseNoteEventDto(
  val nomsId: String?,
  val id: Long?,
  val content: String?,
  val contactTimestamp: LocalDateTime?,
  val notificationTimestamp: LocalDateTime?,
  val firstName: String?,
  val lastName: String?,
  val establishmentCode: String?,
  val mainNoteType: String?,
  val subNoteType: String?,
) {
  fun toCaseNoteEvent() = CaseNoteEvent(
    this.nomsId,
    this.id,
    this.content,
    this.contactTimestamp,
    this.notificationTimestamp,
    this.firstName,
    this.lastName,
    this.establishmentCode,
    this.mainNoteType,
    this.subNoteType,
  )
}
