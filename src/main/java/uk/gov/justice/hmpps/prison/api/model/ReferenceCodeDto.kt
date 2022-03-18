package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class ReferenceCodeDto(
  val domain: String?,
  val code: String?,
  val description: String?,
  val parentDomain: String?,
  val parentCode: String?,
  val activeFlag: String?,
  val listSeq: Int?,
  val systemDataFlag: String?,
  val expiredDate: LocalDate?,
) {
  fun toReferenceCode() = ReferenceCode(
    this.domain,
    this.code,
    listOf(),
    this.description,
    this.parentDomain,
    this.parentCode,
    this.activeFlag,
    this.listSeq,
    this.systemDataFlag,
    this.expiredDate,
  )
}
