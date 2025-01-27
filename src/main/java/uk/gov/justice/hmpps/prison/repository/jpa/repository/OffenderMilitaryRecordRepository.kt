package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence

@Repository
interface OffenderMilitaryRecordRepository : CrudRepository<OffenderMilitaryRecord, BookingAndSequence> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(value = "SELECT omr FROM OffenderMilitaryRecord omr WHERE omr.bookingAndSequence.offenderBooking.bookingId = :bookingId AND omr.bookingAndSequence.sequence = :militarySeq")
  fun findByBookingIdAndMilitarySeqWithLock(
    bookingId: Long,
    militarySeq: Int,
  ): OffenderMilitaryRecord?

  @Query("SELECT omr FROM OffenderMilitaryRecord omr WHERE omr.bookingAndSequence.offenderBooking.bookingId = :bookingId")
  fun findAllByBookingId(bookingId: Long): List<OffenderMilitaryRecord>
}
