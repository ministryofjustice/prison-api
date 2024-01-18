package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief

interface OffenderBeliefRepository : CrudRepository<OffenderBelief, Long> {
  @Query(
    value =
    """
    SELECT
    belief
    FROM OffenderBelief belief
    WHERE belief.booking.offender.nomsId = :prisonerNumber
    AND (:bookingId IS NULL OR belief.booking.bookingId = :bookingId)
    ORDER BY belief.startDate DESC
  """,
  )
  fun getOffenderBeliefHistory(prisonerNumber: String, bookingId: String?): List<OffenderBelief>
}
