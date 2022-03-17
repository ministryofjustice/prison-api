package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class CaseNoteUsageByBookingIdDto(
  val bookingId: Int?,
  val caseNoteType: String?,
  val caseNoteSubType: String?,
  val numCaseNotes: Int?,
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
