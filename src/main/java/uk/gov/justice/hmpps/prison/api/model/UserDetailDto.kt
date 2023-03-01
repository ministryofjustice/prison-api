package uk.gov.justice.hmpps.prison.api.model

data class UserDetailDto(
  var staffId: Long? = null,
  var username: String? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var thumbnailId: Long? = null,
  var activeCaseLoadId: String? = null,
  var accountStatus: String? = null,
) {
  fun toUserDetail() = UserDetail(
    this.staffId,
    this.username,
    this.firstName,
    this.lastName,
    this.thumbnailId,
    this.activeCaseLoadId,
    this.accountStatus,
    null,
    null,
    false,
    false,
  )
}
