package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class CaseNoteStaffUsageDto(
  val staffId: Int?,
  val caseNoteType: String?,
  val caseNoteSubType: String?,
  val numCaseNotes: Int?,
  val latestCaseNote: LocalDateTime?,
) {
  fun toCaseNoteStaffUsage() = CaseNoteStaffUsage(
    this.staffId,
    this.caseNoteType,
    this.caseNoteSubType,
    this.numCaseNotes,
    this.latestCaseNote,
  )
}
