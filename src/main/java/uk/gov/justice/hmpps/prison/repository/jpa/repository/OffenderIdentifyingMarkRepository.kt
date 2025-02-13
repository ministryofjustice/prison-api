package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifyingMark

@Repository
interface OffenderIdentifyingMarkRepository : CrudRepository<OffenderIdentifyingMark, OffenderIdentifyingMark.PK> {
  @Query(value = "SELECT i FROM OffenderIdentifyingMark i join i.offenderBooking ob join ob.offender o where ob.bookingSequence = 1 and o.nomsId = :offenderNumber ORDER BY i.sequenceId")
  fun findAllMarksForLatestBooking(@Param("offenderNumber") offenderNumber: String): List<OffenderIdentifyingMark>

  @Query(value = "SELECT i FROM OffenderIdentifyingMark i join i.offenderBooking ob join ob.offender o where ob.bookingSequence = 1 and o.nomsId = :offenderNumber and i.sequenceId = :markId ORDER BY i.sequenceId")
  fun getMarkForLatestBookingByOffenderNumberAndSequenceId(
    @Param("offenderNumber") offenderNumber: String,
    @Param("markId") markId: Int,
  ): OffenderIdentifyingMark?
}
