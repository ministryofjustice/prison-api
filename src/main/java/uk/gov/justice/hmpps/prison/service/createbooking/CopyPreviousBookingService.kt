package uk.gov.justice.hmpps.prison.service.createbooking

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.storedprocs.CopyProcs.CopyBookData

interface CopyPreviousBookingService {
  fun copyKeyDataFromPreviousBooking(
    booking: OffenderBooking,
    previousBooking: OffenderBooking,
    movement: ExternalMovement,
  )
}

@Service
@Profile("nomis")
class CopyPreviousBookingSPService(val copyBookData: CopyBookData) : CopyPreviousBookingService {
  override fun copyKeyDataFromPreviousBooking(
    booking: OffenderBooking,
    previousBooking: OffenderBooking,
    movement: ExternalMovement,
  ) {
    val params = MapSqlParameterSource()
      .addValue("p_move_type", MovementType.ADM.code)
      .addValue("p_move_reason", movement.movementReason.code)
      .addValue("p_old_book_id", previousBooking.bookingId)
      .addValue("p_new_book_id", booking.bookingId)
    copyBookData.execute(params)
  }
}

@Service
@Profile("!nomis")
class CopyPreviousBookingBasicService(val jdbcTemplate: JdbcTemplate) : CopyPreviousBookingService {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  override fun copyKeyDataFromPreviousBooking(
    booking: OffenderBooking,
    previousBooking: OffenderBooking,
    movement: ExternalMovement,
  ) {
    // basic version of some of the functionality of the stored procedure - in this case just the offender_profile_details table is copied
    // this is minimal required to test create booking but could be extended
    log.warn("Not running against NOMIS database so will just copy offender_profile_details from ${previousBooking.bookingId} to ${booking.bookingId}")
    jdbcTemplate.update(
      """INSERT INTO offender_profile_details (offender_book_id, profile_seq, profile_type, profile_code, list_seq, comment_text, caseload_type) 
        |SELECT ?, profile_seq, profile_type, profile_code, list_seq, comment_text, caseload_type 
        |FROM offender_profile_details 
        |WHERE offender_book_id = ?
        |
      """.trimMargin(),
    ) { ps ->
      ps.setLong(1, booking.bookingId)
      ps.setLong(2, previousBooking.bookingId)
    }
  }
}
