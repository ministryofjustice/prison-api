package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class CaseNoteUsageDto(
  val offenderNo: String?,
  val caseNoteType: String?,
  val caseNoteSubType: String?,
  val numCaseNotes: Int?,
  val latestCaseNote: LocalDateTime?,
) {
  fun toCaseNoteUsage() = CaseNoteUsage(
    this.offenderNo,
    this.caseNoteType,
    this.caseNoteSubType,
    this.numCaseNotes,
    this.latestCaseNote,
  )
}
