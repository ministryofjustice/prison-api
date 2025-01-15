package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief

@Repository
interface OffenderBeliefRepository : CrudRepository<OffenderBelief, Long> {
  @Query(
    value =
    """
    SELECT
    belief
    FROM OffenderBelief belief
    WHERE belief.booking.offender.nomsId = :prisonerNumber
    AND (:bookingId IS NULL OR belief.booking.bookingId = :bookingId)
    ORDER BY belief.startDate DESC, belief.createDatetime DESC
  """,
  )
  fun getOffenderBeliefHistory(prisonerNumber: String, bookingId: String?): List<OffenderBelief>
}
