package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.jetbrains.annotations.NotNull
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import java.time.LocalDate
import java.util.Optional

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByLastNameAndFirstNameAndBirthDate(lastName: String, firstName: String, dob: LocalDate): List<Offender>

  /**
   * This method grabs the root offender, which is the first offender record created for the person.  This is not the
   * same as the current alias, which is determined by getting the offender record linked to the most recent booking
   * (bookingSequence of 1).
   */
  @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId and o.id = o.rootOffenderId")
  fun findRootOffenderByNomsId(nomsId: String): Optional<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Offender o where o.nomsId = :nomsId and o.id = o.rootOffenderId")
  fun findRootOffenderByNomsIdForUpdate(@NotNull nomsId: String): Optional<Offender>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select o from OffenderBooking b inner join Offender o on b.offender = o where o.nomsId = :nomsId and b.bookingSequence = 1")
  fun findLinkedToLatestBookingForUpdate(@NotNull nomsId: String): Optional<Offender>
}
