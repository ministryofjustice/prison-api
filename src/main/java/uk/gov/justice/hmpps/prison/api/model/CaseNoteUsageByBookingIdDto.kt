package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class CaseNoteUsageByBookingIdDto(
  val bookingId: Long?,
  val caseNoteType: String?,
  val caseNoteSubType: String?,
  val numCaseNotes: Long?,
  val latestCaseNote: LocalDateTime?,
) {
  fun toCaseNoteUsageByBookingId() = CaseNoteUsageByBookingId(
    this.bookingId,
    this.caseNoteType,
    this.caseNoteSubType,
    this.numCaseNotes,
    this.latestCaseNote,
  )
}
