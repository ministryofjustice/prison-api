package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.service.DataLoaderRepository

enum class ProfileType {
  YOUTH
}

class OffenderProfileDetailsBuilder(
  private val code: String,
  private val type: ProfileType,
) {

  // use SQL to save because the JPA mapping may not be correct
  fun save(
    offenderBookingId: Long,
    dataLoader: DataLoaderRepository,
  ) = dataLoader.jdbcTemplate.update(
    "INSERT INTO offender_profile_details (offender_book_id, profile_seq, profile_type, profile_code, list_seq) VALUES (?, ?, ?, ?, ?)"
  ) { ps ->
    ps.setLong(1, offenderBookingId)
    ps.setLong(2, 1L)
    ps.setString(
      3,
      when (type) {
        ProfileType.YOUTH -> "YOUTH"
      }
    )
    ps.setString(4, code)
    ps.setLong(5, 99L)
  }
}
