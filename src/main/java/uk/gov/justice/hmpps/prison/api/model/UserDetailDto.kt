package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDateTime

data class UserDetailDto(
  var staffId: Long? = null,
  var username: String? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var thumbnailId: Long? = null,
  var activeCaseLoadId: String? = null,
  var accountStatus: String? = null,
  var lockDate: LocalDateTime? = null,
  var expiryDate: LocalDateTime? = null,
  var lockedFlag: Boolean? = null,
  var expiredFlag: Boolean? = null,
) {
  fun toUserDetail() = UserDetail(
    this.staffId,
    this.username,
    this.firstName,
    this.lastName,
    this.thumbnailId,
    this.activeCaseLoadId,
    this.accountStatus,
    this.lockDate,
    this.expiryDate,
    this.lockedFlag,
    this.expiredFlag,
  )
}
