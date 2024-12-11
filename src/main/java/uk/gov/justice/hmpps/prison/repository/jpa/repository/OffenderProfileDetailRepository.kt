package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import jakarta.validation.constraints.NotNull
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import java.util.*


@Repository
interface OffenderProfileDetailRepository : CrudRepository<OffenderProfileDetail, OffenderProfileDetail.PK> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select opd from OffenderProfileDetail opd inner join opd.id.offenderBooking b inner join b.offender o where o.nomsId = :nomsId and b.bookingSequence = 1 and opd.id.type.type = 'NAT'")
  fun findNationalityLinkedToLatestBookingForUpdate(@NotNull nomsId: String): Optional<OffenderProfileDetail>
}
