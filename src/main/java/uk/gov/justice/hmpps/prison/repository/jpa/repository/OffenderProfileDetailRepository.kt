package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.jetbrains.annotations.NotNull
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import java.util.Optional

@Repository
interface OffenderProfileDetailRepository : CrudRepository<OffenderProfileDetail, OffenderProfileDetail.PK> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select opd from Offender o inner join o.bookings b inner join b.profileDetails opd where o.nomsId = :nomsId and b.bookingSequence = 1 and opd.id.type = :profileType")
  fun findLinkedToLatestBookingForUpdate(@NotNull nomsId: String, @NotNull profileType: ProfileType): Optional<OffenderProfileDetail>
}
